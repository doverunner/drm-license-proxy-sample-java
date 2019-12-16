package com.pallycon.sample.service;

import com.google.common.io.ByteStreams;
import com.pallycon.sample.token.model.TokenRequest;
import com.pallycon.sample.token.DrmType;
import com.pallycon.sample.token.PallyConToken;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

/**
 * Created by Brown on 2019-12-11.
 */
@Service("sampleService")
public class SampleService implements Sample{

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected Environment env;

    public byte[] getLicenseData(String sampleData, byte[] requestBody, String drmType){
        byte[] responseData = null;
        try {
            String type = DrmType.getDrm(drmType.toLowerCase());
            String pallyconCustomData = createPallyConCustomdata(sampleData, type);
            responseData = callLicenseServer(env.getProperty("pallycon.url.license"), requestBody, pallyconCustomData, type);
            logger.debug("responseData :: " + new String(responseData));
        }catch (Exception e){
            e.printStackTrace();
        }
        return responseData;
    }


    private String createPallyConCustomdata(String sampleData, String drmTYpe) throws Exception {
        String siteKey = env.getProperty("pallycon.sitekey");
        String accessKey = env.getProperty("pallycon.accesskey");
        String siteId = env.getProperty("pallycon.siteid");
        PallyConToken pallyConToken = new PallyConToken();

        //TODO 1.
        // Add sample data processing
        // ....
        // cid, userId is required
        String cid = "test";
        String userId = "proxySample";
        //-----------------------------


        //TODO 2.
        // Create license rule
        // https://pallycon.com/docs/en/multidrm/license/license-token/#token-rule-json
        // this sample rule : limit 3600 seconds license.
        JSONObject tokenRule = new JSONObject();
        JSONObject playbackPolicy = new JSONObject();

        playbackPolicy.put("limit", true);
        playbackPolicy.put("persistent", true);
        playbackPolicy.put("duration", 3600);

        tokenRule.put("playback_policy", playbackPolicy);
        //-----------------------------

        //set TokenRequest
        TokenRequest tokenRequest = new TokenRequest().withDrmType(drmTYpe)
                .withAccessKey(accessKey)
                .withSiteKey(siteKey)
                .withCid(cid)
                .withUserId(userId)
                .withSiteId(siteId)
                .withToken(tokenRule);
        //-----------------------------

        return pallyConToken.createApiData(tokenRequest);

    }

    byte[] callLicenseServer(String url,byte[] body, String header, String drmType) throws Exception{
        byte[] targetArray= null;
        InputStream in = null;
        logger.debug("request body :: " + Base64.getEncoder().encodeToString(body));
        try {
            URL targetURL = new URL(url);
            URLConnection urlConnection = targetURL.openConnection();
            HttpURLConnection hurlConn = (HttpURLConnection) urlConnection;

            if(drmType.equals(DrmType.FAIRPLAY.getDrm())){
                hurlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                body = ("spc="+ new String(body)).getBytes();
            }else{
                hurlConn.setRequestProperty("Content-Type", "application/octet-stream");
            }

            hurlConn.setRequestProperty("pallycon-customdata-v2", header);
            hurlConn.setRequestMethod("POST");
            hurlConn.setDoOutput(true);
            hurlConn.setUseCaches(false);
            hurlConn.setDefaultUseCaches(false);

            OutputStream op = hurlConn.getOutputStream();
            op.write(body);
            op.flush();
            op.close();

            in = hurlConn.getInputStream();

            targetArray = ByteStreams.toByteArray(in);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null !=in){
                in.close();
            }

        }
        return targetArray;
    }
}
