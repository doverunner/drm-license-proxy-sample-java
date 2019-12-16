package com.pallycon.sample.controller;

import com.pallycon.sample.service.Sample;
import com.pallycon.sample.token.DrmType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Brown on 2019-12-11.
 */
@RestController
public class SampleController {

    @Resource(name = "sampleService")
    Sample sampleService;


    @PostMapping(value= "/drm/{drmType}")
    public ResponseEntity drmProxy( @RequestBody(required = false) byte[] requestBody
                                    , @PathVariable(value = "drmType") String drmType
                                    , @RequestParam(value = "spc", required = false) String spc
            , @RequestHeader(value = "sample-data", required = false) String sampleData, HttpServletRequest req
    ){
        System.out.println(req.getContentType());
        if(DrmType.FAIRPLAY.getName().equals(drmType.toLowerCase())){
            requestBody = spc.getBytes();
        }
        byte[] responseData = sampleService.getLicenseData(sampleData, requestBody, drmType);
        return ResponseEntity.status(HttpStatus.valueOf(200)).body(responseData);
    }

}

