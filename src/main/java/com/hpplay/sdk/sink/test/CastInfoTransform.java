package com.hpplay.sdk.sink.test;

import com.hpplay.sdk.sink.api.IAPI;
import com.hpplay.sdk.sink.api.CastInfo;

public class CastInfoTransform {

    public static String getInfoType(int infoType) {
        String result = String.valueOf(infoType);
        switch (infoType) {
            case CastInfo.TYPE_START:
                result = "接收到投屏消息";
                break;
            case CastInfo.TYPE_CAST:
                result = "投屏开始,准备起播";
                break;
            case CastInfo.TYPE_STOP:
                result = "结束投屏";
                break;
            case CastInfo.TYPE_ONPREPARED:
                result = "播放成功";
                break;
            case CastInfo.TYPE_ONERROR:
                result = "播放失败";
                break;
            case CastInfo.TYPE_PLAYER_LAG:
                result = "播放器卡顿";
                break;
            case CastInfo.TYPE_SDK_LOADING_START:
                result = "加载开始";
                break;
            case CastInfo.TYPE_SDK_LOADING_COMPLETE:
                result = "加载完成";
                break;
        }
        return result;
    }

    public static String getProtocol(int protocol) {
        String result = String.valueOf(protocol);
        switch (protocol) {
            case IAPI.PROTOCOL_LELINK_FP:// 乐联-FP
                result = "AirPlay";
                break;
            case IAPI.PROTOCOL_DLNA:
                result = "DLNA";
                break;
            case IAPI.PROTOCOL_LELINK:// 乐联
                result = "Lelink";
                break;
            case IAPI.PROTOCOL_NET_CAST:// 公网投屏
                result = "公网投屏";
                break;
            case IAPI.PROTOCOL_CLOUD_MIRROR:
                result = "云镜像";
        }
        return result;
    }

    public static String getCastType(int castType) {
        String result = String.valueOf(castType);
        switch (castType) {
            case IAPI.CASTTYPE_MIRROR:
                result = "镜像";
                break;
            case IAPI.CASTTYPE_URL:
                result = "推送";
                break;
        }
        return result;
    }

    public static String getStopReason(int stopReason) {
        String result = String.valueOf(stopReason);
        switch (stopReason) {
            case CastInfo.STOP_NORMAL:
                result = "正常退出";
                break;
            case CastInfo.STOP_TIMEOUT:
                result = "接收端检测到读数据超时且断开时";
                break;
            case CastInfo.STOP_ERROR:
                result = "异常退出";
                break;
            case CastInfo.STOP_PREEMPTED:
                result = "接收端检测到被抢占";
                break;
            case CastInfo.STOP_REMOTE_CONTROL:
                result = "接收端用户按返回键back/主页键home退出";
                break;
            case CastInfo.STOP_USER_EXIT:
                result = "会议室项目iOS退出";
                break;
            case CastInfo.STOP_BY_SERVER:
                result = "会议室项目HttpServer命令退出";
                break;
            case CastInfo.STOP_BY_BACKKEY_IN_AD:
                result = "前贴广告按返回键";
                break;
            case CastInfo.STOP_BY_PREEMPT:
                result = "防骚扰导致退出（拒绝、黑名单、超时）";
                break;
            case CastInfo.STOP_BY_PLAYER_ERROR:
                result = "播放器onError导致退出";
                break;
            case CastInfo.STOP_BY_PLAYER_COMPLETE:
                result = "播放器播放完成退出";
                break;
            case CastInfo.STOP_BY_OTHER_ACTIVITY:
                result = "其他应用抢占导致退出（如home键启动luncher）";
                break;
            case CastInfo.STOP_BY_INTERNAL_CHANGE:
                result = "协议内抢占（如乐播app本地相册切换图片）";
                break;
        }
        return result;
    }
}
