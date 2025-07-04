package com.doverunner.sample.service;

import com.doverunner.sample.service.dto.RequestDto;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Created by Brown on 2019-12-11.
 */
public interface Sample {
    byte[] getLicenseData(String PallyconClientMeta, byte[] requestBody, RequestDto requestDto, String drmType);

    String getFPSPublicKey();

    String getFPSCertificate(HttpServletResponse servletResponse);

    byte[] getClearKeyLicense(String drmType);
}
