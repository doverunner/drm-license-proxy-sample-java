package com.pallycon.sample.token;

import com.pallycon.sample.token.model.TokenRequest;
import com.pallycon.sample.util.DateUtil;
import com.pallycon.sample.util.SHAUtil;
import com.pallycon.sample.util.StringEncrypter;
import org.json.simple.JSONObject;

import java.util.Base64;

/**
 * Created by Brown on 2019-12-11.
 */
public class PallyConToken {
    private static final String AES_IV = "0123456789abcdef";


    public String createEncrypterTokenData(JSONObject jsonObj, String siteKey) throws Exception{
        StringEncrypter stringEncrypter = new StringEncrypter(siteKey, AES_IV);
        return stringEncrypter.encrypt(jsonObj.toJSONString());
    }

    public String createHash(TokenRequest tokenRequest) {
        StringBuffer bf = new StringBuffer();
        bf.append(tokenRequest.getAccessKey());
        bf.append(tokenRequest.getDrmType());
        bf.append(tokenRequest.getSiteId());
        bf.append(tokenRequest.getUserId());
        bf.append(tokenRequest.getCid());
        bf.append(tokenRequest.getEncToken());
        bf.append(tokenRequest.getCurrentTime());

        return SHAUtil.encrypt(bf.toString());
    }

    public String createApiData(TokenRequest tokenRequest) throws Exception{
        //encrypt token
        String encToken = createEncrypterTokenData(tokenRequest.getToken(), tokenRequest.getSiteKey());
        tokenRequest.setEncToken(encToken);
        //-----------------------------

        //create currentTimeStamp
        tokenRequest.setCurrentTime(DateUtil.getGMTTimeStampString());
        //-----------------------------

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("drm_type", tokenRequest.getDrmType());
        jsonObject.put("site_id", tokenRequest.getSiteId());
        jsonObject.put("user_id", tokenRequest.getUserId());
        jsonObject.put("cid", tokenRequest.getCid());
        jsonObject.put("token", encToken);
        jsonObject.put("timestamp", tokenRequest.getCurrentTime());
        jsonObject.put("hash", createHash(tokenRequest));

        return Base64.getEncoder().encodeToString(jsonObject.toJSONString().getBytes());
    }



}
