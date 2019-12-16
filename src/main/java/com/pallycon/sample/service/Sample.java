package com.pallycon.sample.service;

/**
 * Created by Brown on 2019-12-11.
 */
public interface Sample {
    byte[] getLicenseData(String sampleData, byte[] requestBody, String drmType);
}
