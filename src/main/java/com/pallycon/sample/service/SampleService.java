package com.pallycon.sample.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.pallycon.sample.model.DeviceInfo;
import com.pallycon.sample.model.LicenseResponse;
import com.pallycon.sample.service.dto.RequestDto;
import com.pallycon.sample.token.PallyConDrmTokenClient;
import com.pallycon.sample.token.PallyConDrmTokenPolicy;
import com.pallycon.sample.token.policy.PlaybackPolicy;
import com.pallycon.sample.token.policy.common.ResponseFormat;
import com.pallycon.sample.util.AESUtil;
import com.pallycon.sample.util.JSONUtil;
import com.pallycon.sample.util.SHAUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
import java.security.NoSuchAlgorithmException;
import java.util.*;

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

    public byte[] getLicenseData(String pallyconClientMeta, String hashCid, String hashUid, byte[] requestBody, RequestDto requestDto, String drmType) throws Exception {
        byte[] responseData;
        String type = DrmType.getDrm(drmType.toLowerCase());
        logger.debug("DrmType : {}", type);

        List<String> allowedUserIdList = Arrays.asList("test-user", "testUser"); // This assumes that the value is fetched from a database.
        String uid = findElementByHash(allowedUserIdList, hashUid);

        List<String> allowedContentIdList = Arrays.asList("multitracks", "bigbuckbunny"); // This assumes that the value is fetched from a database.
        String cid = findElementByHash(allowedContentIdList, hashCid);

        logger.debug("uid :: {}", uid);
        logger.debug("cid :: {}", cid);

        String pallyconCustomData = createPallyConCustomdata(cid, uid, requestBody, requestDto, type);
        logger.debug("pallycon-customdata-v2 : {}", pallyconCustomData);

        String modeParam = "";
        String method = HttpMethod.POST.name();
        if (requestDto.getMode() != null && "getserverinfo".equals(requestDto.getMode())) {
            modeParam = "?mode=" + requestDto.getMode();
            method = HttpMethod.GET.name();
        }

        String url = env.getProperty("pallycon.url.license");

        if (DrmType.WIDEVINE.getName().equalsIgnoreCase(drmType) && pallyconClientMeta != null) {
            // for Android SDK
            String key = Objects.requireNonNull(env.getProperty("pallycon.sdk.aes.key"));
            String iv = Objects.requireNonNull(env.getProperty("pallycon.sdk.aes.iv"));
            requestBody = AESUtil.decryptAES256(requestBody, key, iv);
            byte[] licenseResponse = callLicenseServer(url + modeParam, requestBody, pallyconCustomData, type, method, pallyconClientMeta);
            responseData = checkResponseData(licenseResponse, drmType, pallyconClientMeta);
            responseData = AESUtil.encryptAES256(responseData, key, iv);
        } else {
            byte[] licenseResponse = callLicenseServer(url + modeParam, requestBody, pallyconCustomData, type, method, pallyconClientMeta);
            responseData = checkResponseData(licenseResponse, drmType, pallyconClientMeta);
        }

        logger.debug("responseData :: {}", new String(responseData));

        return responseData;
    }

    // TODO 1. Instead of the code below, create the actual logic to validate the content ID.
    private String findElementByHash(List<String> allowList, String hash) {
        return allowList.stream()
                .filter(uid -> {
                    try {
                        return SHAUtil.toSha512(uid).equalsIgnoreCase(hash);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                })
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Not found with hash: " + hash));
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
    private String createPallyConCustomdata(String cid, String uid, byte[] requestBody, RequestDto requestDto, String drmType) throws Exception {
        String siteKey = env.getProperty("pallycon.sitekey");
        String accessKey = env.getProperty("pallycon.accesskey");
        String siteId = env.getProperty("pallycon.siteid");

        logger.debug("siteId :: {}", siteId);
        logger.debug("siteKey :: {}", siteKey);
        logger.debug("accessKey :: {}", accessKey);

        String tokenResponseFormat = env.getProperty("pallycon.token.response.format", RESPONSE_FORMAT_ORIGINAL).toUpperCase();

        //create pallycon drm token client
        PallyConDrmTokenClient pallyConDrmTokenClient = new PallyConDrmTokenClient()
                .siteKey(siteKey)
                .accessKey(accessKey)
                .siteId(siteId)
                .responseFormat(ResponseFormat.valueOf(tokenResponseFormat));


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

        pallyConDrmTokenClient.userId(uid);
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

    private byte[] checkResponseData(byte[] byteLicenseResponse, String drmType, String pallyconClientMeta) {
        String tokenResponseFormat = env.getProperty("pallycon.token.response.format", RESPONSE_FORMAT_ORIGINAL).toUpperCase();
        String responseFormat = env.getProperty("pallycon.response.format", RESPONSE_FORMAT_ORIGINAL).toUpperCase();

        if (RESPONSE_FORMAT_JSON.equals(tokenResponseFormat)) {
            ObjectMapper mapper = new ObjectMapper();
            LicenseResponse licenseResponse;
            /*-------------------------------------------------
            //TODO 4. If you want to control ResponseData, do it here.
             -------------------------------------------------*/
            try {
                licenseResponse = mapper.readValue(new String(byteLicenseResponse), LicenseResponse.class);
            } catch (IOException e) {
                logger.error("Error parsing JSON response", e);
                throw new RuntimeException(e);
            }

            DeviceInfo deviceInfo = licenseResponse.deviceInfo();

            String deviceId = deviceInfo.deviceId();
            boolean isChromeCdm = deviceInfo.isChromecdm();

            logger.debug("Device ID :: {} ", deviceId);
            logger.debug("Chrome CDM :: {} ", isChromeCdm);

            if (!"".equals(pallyconClientMeta) && "widevine".equalsIgnoreCase(drmType) && isChromeCdm) {
                throw new RuntimeException("Found contradictory data in the response.");
            }

            if (RESPONSE_FORMAT_ORIGINAL.equals(responseFormat)) {
                byteLicenseResponse = convertResponseDate(byteLicenseResponse, licenseResponse.license(), drmType);
            }
        }

        return byteLicenseResponse;
    }

    private byte[] convertResponseDate(byte[] licenseResponse, String license, String drmType) {
        byte[] responseData;

        if (null != license) {
            if ("widevine".equalsIgnoreCase(drmType)) {
                responseData = DatatypeConverter.parseHexBinary(license);
            } else {
                responseData = license.getBytes();
            }
        } else {
            responseData = licenseResponse;
        }
        return responseData;
    }

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

    @Override
    public String getFPSCertificate(HttpServletResponse servletResponse) {
        try {
            String result = publicKeyRequest();
            if (JSONUtil.isJSONValid(result)) {
                return result;
            }
            byte[] certStrByte = Base64.getDecoder().decode(result);
            try (BufferedOutputStream outs = new BufferedOutputStream(servletResponse.getOutputStream())) {
                servletResponse.setHeader("Content-Type","application/x-x509-ca-cert");
                servletResponse.setHeader("Content-Disposition", "attachment;filename=cert_license_fps_com.der;");
                outs.write(certStrByte);
                outs.flush();
            }
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        return "0000";
    }

    private String publicKeyRequest() throws IOException, InterruptedException {
        String url = env.getProperty("pallycon.url.fpsKeyManager");
        String siteId = env.getProperty("pallycon.siteid");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s?siteId=%s", url, siteId)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
