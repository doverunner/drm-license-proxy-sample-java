package com.doverunner.sample.service;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by Brown on 2019-12-13.
 */
public enum DrmType{
    WIDEVINE("widevine", "Widevine")
    , PLAYREADY("playready", "PlayReady")
    , FAIRPLAY("fairplay", "FairPlay")
    , NCG("ncg", "NCG")
    , WISEPLAY("wiseplay", "WisePlay")
    , CLEARKEY("clearkey", "ClearKey");

    private final String name;
    private final String drm;

    DrmType(String name, String drm){
        this.name = name;
        this.drm = drm;
    }

    public String getName() {
        return name;
    }

    public String getDrm() {
        return drm;
    }

    public static String getDrm(String name){
        DrmType[] drmType = values();
        int regionSize = drmType.length;

        return Arrays.stream(drmType)
                .filter(d -> d.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find enum : " + name + "!"))
                .getDrm();
    }
}
