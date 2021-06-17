package com.pallycon.sample.service;

/**
 * Created by Brown on 2019-12-13.
 */
public enum DrmType{
    WIDEVINE("widevine", "Widevine")
    , PLAYREADY("playready", "PlayReady")
    , FAIRPLAY("fairplay", "FairPlay");

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

        for(int i = 0; i < regionSize; ++i) {
            DrmType drm = drmType[i];
            if(drm.getName().equals(name)) {
                return drm.getDrm();
            }
        }
        throw new IllegalArgumentException("Cannot find enum : " + name + "!");
    }
}
