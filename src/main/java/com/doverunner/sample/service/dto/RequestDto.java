package com.doverunner.sample.service.dto;

/**
 * For NCG
 *
 */
public class RequestDto {

    private String mode;
    private String encryptedKey;
    private String encryptedData;
    private String signature;

    public String getMode() {
        return mode;
    }
    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }
    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public String getEncryptedData() {
        return encryptedData;
    }
    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }

}
