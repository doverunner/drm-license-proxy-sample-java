package com.pallycon.sample.service;

import com.google.common.io.ByteStreams;
import com.pallycon.sample.service.dto.RequestDto;
import com.pallycon.sample.token.PallyConDrmTokenClient;
import com.pallycon.sample.token.PallyConDrmTokenPolicy;
import com.pallycon.sample.token.policy.PlaybackPolicy;
import com.pallycon.sample.token.policy.common.ResponseFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

/**
 * Created by Brown on 2019-12-11.
 * Updated by jsnoh on 2022-06-10.
 * Updated by jsnoh on 2023-01-30.
 *
 */
@Service("sampleService")
public class SampleService implements Sample{
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String RESPONSE_FORMAT_ORIGINAL = "ORIGINAL";
    private static final String RESPONSE_FORMAT_JSON = "JSON";
    private static final String RESPONSE_FORMAT_CUSTOM = "CUSTOM";

    @Autowired
    protected Environment env;

    public byte[] getLicenseData(String pallyconClientMeta, byte[] requestBody, RequestDto requestDto, String drmType){
        byte[] responseData = null;
        try {
            String type = DrmType.getDrm(drmType.toLowerCase());
            logger.debug("DrmType : {}",  type);

            String pallyconCustomData = createPallyConCustomdata(requestBody, requestDto, type);
            logger.debug("pallycon-customdata-v2 : {}", pallyconCustomData);

            String modeParam = "";
            String method = HttpMethod.POST.name();
            if ( requestDto.getMode() != null && "getserverinfo".equals(requestDto.getMode()) ){
                modeParam = "?mode=" + requestDto.getMode();
                method = HttpMethod.GET.name();
            }
            byte[] licenseResponse = callLicenseServer(env.getProperty("pallycon.url.license") + modeParam, requestBody, pallyconCustomData, type, method, pallyconClientMeta);
            responseData = checkResponseData(licenseResponse, drmType);
            logger.debug("responseData :: {}", new String(responseData));
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        return responseData;
    }

    /**
     * Create pallycon-customdata-v2 using the received paramter value.
     * It is created using pallycon-token-sample.jar provided by Pallycon.
     *
     * @param requestBody requestBody
     * @param requestDto requestDto
     * @param drmType
     * @return
     * @throws Exception
     */
    private String createPallyConCustomdata(byte[] requestBody, RequestDto requestDto, String drmType) throws Exception {
        String siteKey = env.getProperty("pallycon.sitekey");
        String accessKey = env.getProperty("pallycon.accesskey");
        String siteId = env.getProperty("pallycon.siteid");

        logger.debug("siteId :: {}", siteId);
        logger.debug("siteKey :: {}", siteKey);
        logger.debug("accessKey :: {}", accessKey);

        String toeknResponseFormat = env.getProperty("pallycon.token.response.format", RESPONSE_FORMAT_ORIGINAL).toUpperCase();

        //create pallycon drm token client
        PallyConDrmTokenClient pallyConDrmTokenClient = new PallyConDrmTokenClient()
                .siteKey(siteKey)
                .accessKey(accessKey)
                .siteId(siteId)
                .responseFormat(ResponseFormat.valueOf(toeknResponseFormat));


        switch (drmType.toLowerCase()){
            case "ncg":
                pallyConDrmTokenClient.ncg();
                break;
            case "fairplay":
                pallyConDrmTokenClient.fairplay();
                break;
            case "widevine":
                pallyConDrmTokenClient.widevine();
                break;
            case "playready": default:
                pallyConDrmTokenClient.playready();
                break;
        }

        //TODO 1.
        // Add sample data processing
        // ....
        // cid, userId is required
        String cid = "proxySample";
        String userId = "proxy_sample_test";

        //-----------------------------

        pallyConDrmTokenClient.userId(userId);
        pallyConDrmTokenClient.cId(cid);

        //TODO 2.
        // Create license rule
        // https://pallycon.com/docs/en/multidrm/license/license-token/#license-policy-json
        // this sample rule : limit 3600 seconds license.
        PlaybackPolicy playbackPolicy = new PlaybackPolicy();
        playbackPolicy.licenseDuration(3600);
        playbackPolicy.persistent(true);


        //TODO 3.
        // create PallyConDrmTokenPolicy
        // Set the created playbackpolicy, securitypolicy, and externalkey.
        PallyConDrmTokenPolicy pallyConDrmTokenPolicy= new PallyConDrmTokenPolicy.PolicyBuilder()
                .playbackPolicy(playbackPolicy)
                .build();

        // Token is created with the created token policy.
        return pallyConDrmTokenClient.policy(pallyConDrmTokenPolicy).execute();

    }

    /**
     * Make a request to the Pallycon license server.
     * @param url
     * @param body
     * @param pallyconCustomData
     * @param drmType
     * @param method
     * @param pallyconClientMeta
     * @return
     * @throws Exception
     */
    byte[] callLicenseServer(String url, byte[] body, String pallyconCustomData, String drmType, String method, String pallyconClientMeta) throws Exception{
        byte[] targetArray= null;
        InputStream in = null;

        try {
            URL targetURL = new URL(url);
            URLConnection urlConnection = targetURL.openConnection();
            HttpURLConnection hurlConn = (HttpURLConnection) urlConnection;

            if ( HttpMethod.POST.name().equals(method)) {
                if ( body != null ) {
                    logger.debug("request body :: {}", Base64.getEncoder().encodeToString(body));
                }

                if(drmType.equals(DrmType.FAIRPLAY.getDrm())) {
                    hurlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    body = ("spc=" + new String(body)).getBytes();
                }else if (drmType.equals(DrmType.NCG.getDrm())){
                    hurlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                }else{
                    hurlConn.setRequestProperty("Content-Type", "application/octet-stream");
                }

                if (pallyconCustomData != null && !"".equals(pallyconCustomData)) {
                    hurlConn.setRequestProperty("pallycon-customdata-v2", pallyconCustomData);
                }
                if (pallyconClientMeta != null && !"".equals(pallyconClientMeta)) {
                    hurlConn.setRequestProperty("pallycon-client-meta", pallyconClientMeta);
                }
                hurlConn.setRequestMethod(method);
                hurlConn.setDoOutput(true);

            }else{
                hurlConn.setRequestMethod(method);
                hurlConn.setDoOutput(false);
            }
            hurlConn.setUseCaches(false);
            hurlConn.setDefaultUseCaches(false);

            if ( body != null ) {
                OutputStream op = hurlConn.getOutputStream();
                op.write(body);
                op.flush();
                op.close();
            }

            in = hurlConn.getInputStream();

            targetArray = ByteStreams.toByteArray(in);
            logger.debug("license :: {}", new String(targetArray));

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null !=in){
                in.close();
            }

        }
        return targetArray;
    }

    private byte[] checkResponseData(byte[] licenseResponse, String drmType) throws Exception{
        JSONParser jsonParser = new JSONParser();

        String tokenResponseFormat = env.getProperty("pallycon.token.response.format", RESPONSE_FORMAT_ORIGINAL).toUpperCase();
        String responseFormat = env.getProperty("pallycon.response.format", RESPONSE_FORMAT_ORIGINAL).toUpperCase();

        if(RESPONSE_FORMAT_JSON.equals(tokenResponseFormat)){
            JSONObject responseJson = (JSONObject)jsonParser.parse(new String(licenseResponse));
            /*-------------------------------------------------
            //TODO 4. If you want to control ResponseData, do it here.
             -------------------------------------------------*/
            JSONObject deviceInfo = (JSONObject) responseJson.get("device_info");
            String deviceId = null;
            if(null != deviceInfo){
                deviceId = (String) deviceInfo.get("device_id");
            }
            logger.debug("Device ID :: {} ", deviceId);

            if(RESPONSE_FORMAT_ORIGINAL.equals(responseFormat)){
                licenseResponse = convertResponseDate(licenseResponse, responseJson, drmType);
            }
        }

        return licenseResponse;

    }

    private byte[] convertResponseDate(byte[] licenseResponse, JSONObject responseJson, String drmType) throws Exception {
        byte[] responseData;

        String license = (String) responseJson.get("license");

        if(null != license){
            if( "widevine".equalsIgnoreCase(drmType) ){
                responseData = DatatypeConverter.parseHexBinary(license);
            }else{
                responseData = license.getBytes();
            }
        }else{
            responseData = licenseResponse;
        }
        return responseData;
    }
}
