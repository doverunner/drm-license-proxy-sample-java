package com.pallycon.sample.token.model;

import org.json.simple.JSONObject;

/**
 * Created by Brown on 2019-12-11.
 */
public class TokenRequest {
    String accessKey;
    String drmType;
    String siteId;
    String userId;
    String cid;
    JSONObject token;
    String encToken;
    String currentTime;
    String siteKey;

    public TokenRequest(){}

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public TokenRequest withAccessKey(String accessKey){
        this.setAccessKey(accessKey);
        return this;
    }

    public String getDrmType() {
        return drmType;
    }

    public void setDrmType(String drmType) {
        this.drmType = drmType;
    }

    public TokenRequest withDrmType(String drmType){
        this.setDrmType(drmType);
        return this;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public TokenRequest withSiteId(String siteId){
        this.setSiteId(siteId);
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TokenRequest withUserId(String userId){
        this.setUserId(userId);
        return this;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public TokenRequest withCid(String cid){
        this.setCid(cid);
        return this;
    }

    public JSONObject getToken() {
        return token;
    }

    public void setToken(JSONObject token) {
        this.token = token;
    }

    public TokenRequest withToken(JSONObject token){
        this.setToken(token);
        return this;
    }

    public String getEncToken() {
        return encToken;
    }

    public void setEncToken(String encToken) {
        this.encToken = encToken;
    }

    public TokenRequest withEncToken(String encToken){
        this.setEncToken(encToken);
        return this;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public TokenRequest withCurrentTime(String currentTime){
        this.setCurrentTime(currentTime);
        return this;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public TokenRequest withSiteKey(String siteKey){
        this.setSiteKey(siteKey);
        return this;
    }
}
