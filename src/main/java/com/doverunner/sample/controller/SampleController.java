package com.doverunner.sample.controller;

import com.doverunner.sample.service.DrmType;
import com.doverunner.sample.service.Sample;
import com.doverunner.sample.service.dto.RequestDto;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Created by Brown on 2019-12-11.
 * Updated by jsnoh on 2022-06-10.
 * Updated by jsnoh on 2023-01-30.
 */
@RestController
public class SampleController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "sampleService")
    Sample sampleService;


    /**
     * Muti-DRM ( FairPlay, PlayReady, Widevine , NCG )
     *
     * @param requestBody
     * @param drmType
     * @param spc
     * @param requestDto
     * @param request
     * @return
     */
    @PostMapping(value = "/drm/{drmType}")
    public ResponseEntity drmProxyPost( @RequestHeader(required=false, value="pallycon-client-meta") String PallyconClientMeta
            , @RequestBody(required = false) byte[] requestBody
            , @PathVariable(value = "drmType") String drmType
            , @RequestParam(value = "spc", required = false) String spc
            , RequestDto requestDto
            , HttpServletRequest request
    ){
        logger.debug("header PallyconClientMeta :: {}", PallyconClientMeta);
        logger.debug("request content-type :: {}", request.getContentType());
        logger.debug("request Method :: {}", request.getMethod());

        if(DrmType.FAIRPLAY.getName().equals(drmType.toLowerCase())){
            if ( spc != null ) {
                requestBody = spc.getBytes();
            }
        }

        if ( "getserverinfo".equals(requestDto.getMode())){
            if ( !HttpMethod.GET.name().equalsIgnoreCase(request.getMethod())){
                logger.error("not Method GET");
                return new ResponseEntity(HttpStatus.METHOD_NOT_ALLOWED);
            }
        }

        if ("clearkey".equalsIgnoreCase(drmType)){
            byte[] responseData = sampleService.getClearKeyLicense(drmType);
            return ResponseEntity.ok().body(responseData);
        }

        byte[] responseData = sampleService.getLicenseData(PallyconClientMeta, requestBody, requestDto, drmType);
        return ResponseEntity.status(HttpStatus.OK.value()).body(responseData);
    }


    /**
     * NCG - getserverinfo only
     *  -> The first URL to connection with the server in the NCG SDK
     *
     * @param requestDto
     * @return
     */
    @GetMapping(value = "/drm/ncg")
    public ResponseEntity ncgGetServerInfo(RequestDto requestDto){
        logger.debug("request getserverinfo !!");

        if ( !"getserverinfo".equals(requestDto.getMode())){
            logger.error("not getserverinfo");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        byte[] responseData = sampleService.getLicenseData( null, null, requestDto, DrmType.NCG.getName());
        return ResponseEntity.status(HttpStatus.OK.value()).body(responseData);
    }

    @RequestMapping(value = "/fpsKeyManager", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> fpsKeyManager() {
        return ResponseEntity.ok(sampleService.getFPSPublicKey());
    }

    @RequestMapping(value = "/fpsCert", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> fpsCertificate(HttpServletResponse response) {
        return ResponseEntity.ok(sampleService.getFPSCertificate(response));
    }

}
