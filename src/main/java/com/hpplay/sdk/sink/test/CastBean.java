package com.hpplay.sdk.sink.test;

import com.hpplay.sdk.sink.api.IAPI;

/**
 * Created by tcc on 2018/8/17.
 */

public class CastBean {
    private static final CastBean mInstance = new CastBean();

    public String currentName = "";
    public int authMode = IAPI.AUTH_MODE_FREE;
    public String authPsd = "";
    public int mirrorSurfaceType = IAPI.SURFACE_AUTO;
    public int mirrorResetMode = IAPI.MIRROR_RESET_AUTO;
    public boolean showMirrorFps = true;
    public int mirrorMaxFps = 60;
    public int mirrorSmoothMode = IAPI.MIRROR_PREFER_REAL_TIME;
    // 多通道镜像功能，涉及到收费功能，仅对乐播APP生效
    public int multiMirror = IAPI.MULTI_MIRROR_OPEN;
    public int videoPlayerType = IAPI.PLAYER_DEFAULT;
    public int volumeType = IAPI.VOLUME_AUTO;
    public int audioProtocol = IAPI.OPTION_AUDIO_PROTOCOL_UDP;

    private CastBean() {

    }

    public static CastBean getInstance() {
        return mInstance;
    }

}
