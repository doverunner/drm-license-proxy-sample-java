package com.pallycon.sample.service;

import com.pallycon.sample.service.dto.RequestDto;

/**
 * Created by Brown on 2019-12-11.
 */
public interface Sample {
    byte[] getLicenseData(String pallyconClientMeta, byte[] requestBody, RequestDto requestDto, String drmType);
}
