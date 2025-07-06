package com.doverunner.sample.service;

import com.google.common.io.ByteStreams;
import com.doverunner.sample.service.dto.RequestDto;
import com.doverunner.sample.token.DoverunnerDrmTokenClient;
import com.doverunner.sample.token.DoverunnerDrmTokenPolicy;
import com.doverunner.sample.token.policy.PlaybackPolicy;
import com.doverunner.sample.token.policy.common.ResponseFormat;
import com.doverunner.sample.util.JSONUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.DatatypeConverter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

            String pallyconCustomData = createPallyconCustomData(requestBody, requestDto, type);
            logger.debug("pallycon-customdata-v2 : {}", pallyconCustomData);

            String modeParam = "";
            String method = HttpMethod.POST.name();
            if ( requestDto.getMode() != null && "getserverinfo".equals(requestDto.getMode()) ){
                modeParam = "?mode=" + requestDto.getMode();
                method = HttpMethod.GET.name();
            }
            byte[] licenseResponse = callLicenseServer(env.getProperty("doverunner.url.license") + modeParam, requestBody, pallyconCustomData, type, method, pallyconClientMeta);
            responseData = checkResponseData(licenseResponse, drmType);
            logger.debug("responseData :: {}", new String(responseData));
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        return responseData;
    }

    /**
     * Create pallycon-customdata-v2 using the received paramter value.
     * It is created using doverunner-token-sample.jar provided by Doverunner.
     *
     * @param requestBody requestBody
     * @param requestDto requestDto
     * @param drmType
     * @return
     * @throws Exception
     */
    private String createPallyconCustomData(byte[] requestBody, RequestDto requestDto, String drmType) throws Exception {
        String siteKey = env.getProperty("doverunner.sitekey");
        String accessKey = env.getProperty("doverunner.accesskey");
        String siteId = env.getProperty("doverunner.siteid");

        logger.debug("siteId :: {}", siteId);
        logger.debug("siteKey :: {}", siteKey);
        logger.debug("accessKey :: {}", accessKey);

        String tokenResponseFormat = env.getProperty("doverunner.token.response.format", RESPONSE_FORMAT_ORIGINAL).toUpperCase();

        //create doverunner drm token client
        DoverunnerDrmTokenClient doverunnerDrmTokenClient = new DoverunnerDrmTokenClient()
                .siteKey(siteKey)
                .accessKey(accessKey)
                .siteId(siteId)
                .responseFormat(ResponseFormat.valueOf(tokenResponseFormat));


        switch (drmType.toLowerCase()){
            case "wiseplay":
                doverunnerDrmTokenClient.wiseplay();
                break;
            case "ncg":
                doverunnerDrmTokenClient.ncg();
                break;
            case "fairplay":
                doverunnerDrmTokenClient.fairplay();
                break;
            case "widevine":
                doverunnerDrmTokenClient.widevine();
                break;
            case "playready": default:
                doverunnerDrmTokenClient.playready();
                break;
        }

        //TODO 1.
        // Add sample data processing
        // ....
        // cid, userId is required
        String cid = "palmulti";
        String userId = "utest";

        //-----------------------------

        doverunnerDrmTokenClient.userId(userId);
        doverunnerDrmTokenClient.cId(cid);

        //TODO 2.
        // Create license rule
        // https://doverunner.com/docs/en/multidrm/license/license-token/#license-policy-json
        // this sample rule : limit 3600 seconds license.
        PlaybackPolicy playbackPolicy = new PlaybackPolicy();
        playbackPolicy.licenseDuration(3600);
        playbackPolicy.persistent(true);


        //TODO 3.
        // create DoverunnerDrmTokenPolicy
        // Set the created playbackpolicy, securitypolicy, and externalkey.
        DoverunnerDrmTokenPolicy doverunnerDrmTokenPolicy= new DoverunnerDrmTokenPolicy.PolicyBuilder()
                .playbackPolicy(playbackPolicy)
                .build();

        // Token is created with the created token policy.
        return doverunnerDrmTokenClient.policy(doverunnerDrmTokenPolicy).execute();

    }

    /**
     * Make a request to the Doverunner license server.
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
                }else if (drmType.equals(DrmType.NCG.getDrm())){
                    hurlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                }else if (drmType.equals(DrmType.WISEPLAY.getDrm())){
                    hurlConn.setRequestProperty("Content-Type", "application/json");
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

        String tokenResponseFormat = env.getProperty("doverunner.token.response.format", RESPONSE_FORMAT_ORIGINAL).toUpperCase();
        String responseFormat = env.getProperty("doverunner.response.format", RESPONSE_FORMAT_ORIGINAL).toUpperCase();

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

    /**
     * Base64 FPS Cert
     */
    @Override
    public String getFPSPublicKey() {
        String result = null;
        try {
            result = publicKeyRequest();
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Binary FPS Cert
     */
    @Override
    public String getFPSCertificate(HttpServletResponse servletResponse) {
        try {
            String result = publicKeyRequest();
            if (JSONUtil.isJSONValid(result)) {
                return result;
            }
            byte[] certStrByte = Base64.getDecoder().decode(result);
            try (BufferedOutputStream outs = new BufferedOutputStream(servletResponse.getOutputStream())) {
                servletResponse.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-x509-ca-cert");
                servletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=cert_license_fps_com.der;");
                outs.write(certStrByte);
                outs.flush();
            }
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        return "0000";
    }

    private String publicKeyRequest() throws IOException, InterruptedException {
        String url = env.getProperty("doverunner.url.fpsKeyManager");
        String siteId = env.getProperty("doverunner.siteid");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s?siteId=%s", url, siteId)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }


    public byte[] getClearKeyLicense(String drmType) {
        String licenseServerUrl = env.getProperty("doverunner.url.clearkey");
        String siteId = env.getProperty("doverunner.siteid");
        String cid = "palmulti"; // 필요시 파라미터로 변경 가능

        String url = String.format("%s?siteId=%s&cid=%s", licenseServerUrl, siteId, cid);

        try {
            return callLicenseServer(
                    url,
                    null,
                    null,
                    drmType.toLowerCase(),
                    HttpMethod.GET.name(),
                    null
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

}
