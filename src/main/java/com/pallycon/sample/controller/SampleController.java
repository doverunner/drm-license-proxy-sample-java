package com.pallycon.sample.controller;

import com.pallycon.sample.service.DrmType;
import com.pallycon.sample.service.Sample;
import com.pallycon.sample.service.dto.RequestDto;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * @param pallyconClientMeta Pallycon SDK Metadata
     * @param requestBody
     * @param drmType
     * @param spc                for FairPlay
     * @param requestDto         for NCG
     * @param request
     * @return
     */
    @PostMapping(value = "/drm/{drmType}")
    public ResponseEntity drmProxyPost(
            @RequestHeader(required = false, value = "pallycon-client-meta") String pallyconClientMeta,
            @RequestHeader(value = "cid") String hashCid,
            @RequestHeader(value = "uid") String hashUid,
            @RequestBody(required = false) byte[] requestBody,
            @PathVariable(value = "drmType") String drmType,
            @RequestParam(value = "spc", required = false) String spc,
            RequestDto requestDto,
            HttpServletRequest request
    ) {
        logger.debug("header cid :: {}", hashCid);
        logger.debug("header uid :: {}", hashUid);
        logger.debug("header pallyconClientMeta :: {}", pallyconClientMeta);
        logger.debug("request content-type :: {}", request.getContentType());
        logger.debug("request Method :: {}", request.getMethod());

        if (DrmType.FAIRPLAY.getName().equals(drmType.toLowerCase())) {
            if (spc != null) {
                requestBody = spc.getBytes();
            }
        }

        try {
            byte[] responseData = sampleService.getLicenseData(pallyconClientMeta, hashCid, hashUid, requestBody, requestDto, drmType);
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    /**
     * NCG - getserverinfo only
     * -> The first URL to connection with the server in the NCG SDK
     *
     * @param requestDto
     * @return
     */
    @GetMapping(value = "/drm/ncg")
    public ResponseEntity ncgGetServerInfo(RequestDto requestDto) {
        logger.debug("request getserverinfo !!");

        if (!"getserverinfo".equals(requestDto.getMode())) {
            logger.error("not getserverinfo");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        try {
            byte[] responseData = sampleService.getLicenseData(null, null, null, null, requestDto, DrmType.NCG.getName());
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
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
