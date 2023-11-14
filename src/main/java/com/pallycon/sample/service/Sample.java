package com.pallycon.sample.service;

import com.pallycon.sample.service.dto.RequestDto;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Created by Brown on 2019-12-11.
 */
public interface Sample {
    byte[] getLicenseData(String pallyconClientMeta, byte[] requestBody, RequestDto requestDto, String drmType);

    String getFPSPublicKey();

    String getFPSCertificate(HttpServletResponse servletResponse);
}
