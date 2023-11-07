package com.hpplay.sdk.sink.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.hpplay.sdk.sink.api.CastInfo;
import com.hpplay.sdk.sink.api.ClientInfo;
import com.hpplay.sdk.sink.api.IAPI;
import com.hpplay.sdk.sink.api.IMiniProgramQRListener;
import com.hpplay.sdk.sink.api.IPreemptListener;
import com.hpplay.sdk.sink.api.PreemptCastInfo;
import com.hpplay.sdk.sink.api.IRedirectAppResultControl;
import com.hpplay.sdk.sink.api.IRedirectListener;
import com.hpplay.sdk.sink.api.IServerListener;
import com.hpplay.sdk.sink.api.LelinkCast;
import com.hpplay.sdk.sink.api.Option;
import com.hpplay.sdk.sink.api.PreemptInfo;
import com.hpplay.sdk.sink.api.RedirectBean;
import com.hpplay.sdk.sink.api.ServerInfo;
import com.hpplay.sdk.sink.api.UploadLogBean;
import com.hpplay.sdk.sink.dmp.DeviceBean;
import com.hpplay.sdk.sink.dmp.OnDMPListener;
import com.hpplay.sdk.sink.feature.IAuthCodeCallback;
import com.hpplay.sdk.sink.feature.IMediaPlayerCallback;
import com.hpplay.sdk.sink.feature.IPinCodeCallback;
import com.hpplay.sdk.sink.feature.PinCodeSetting;
import com.hpplay.sdk.sink.feature.PlayInfo;
import com.hpplay.sdk.sink.feature.PlayerconfigSetting;
import com.hpplay.sdk.sink.test.dialog.CountdownDialog;
import com.hpplay.sdk.sink.test.dialog.LeboDialog;
import com.hpplay.sdk.sink.feature.IFpsListener;
import com.hpplay.sdk.sink.api.BleAdvertiseListener;
import com.hpplay.sdk.sink.api.InitBean;

import com.hpplay.sdk.sink.test.dialog.PreemptDialog;
import com.hpplay.sdk.sink.util.ContextPath;

import static com.hpplay.sdk.sink.api.Option.LEBO_OPTION_108;
import static com.hpplay.sdk.sink.api.Option.LEBO_OPTION_109;
import static com.hpplay.sdk.sink.util.FilenameConstants.VERSION_BU;

import com.hpplay.sdk.sink.pass.ICloudMirrorCallback;
import com.hpplay.sdk.sink.pass.PassBean;


public class LelinkHelper {
    private final String TAG = "LelinkHelper";

    private static final int SDK_AUTH_FAILED = 1;
    private static final int SDK_AUTH_SERVER_FAILED = 2;
    private static final int SDK_DISCONNECT = 4;
    private static final int SDK_CAST_PREEMPT = 5;
    public static final String DEMO_KEY_SHOW_CONFIRM_WINDOW = "demo_key_show_confirm_window";
    public static final String DEMO_KEY_LOCALSERVER_STATUS = "demo_key_localserver_status";
    public static final String DEMO_KEY_PREEMPT_SHOW_STATUS = "demo_key_preempt_show_status";
    public static final String DEMO_KEY_AUDIOFOCUS_STATUS = "demo_key_audiofocus_status";
    public static final String DEMO_KEY_LOADING_SHOW_STATUS = "demo_key_loading_show_status";

    private static LelinkHelper mLelinkHelper;

    private Context mContext;
    private IUIUpdateListener mUIUpdate;
    private LelinkCast mLelinkCast;
    private LeboDialog mPwdDialog;
    private CastBean mCastBean = CastBean.getInstance();
    private ServerInfo mServerInfo = null;
    // 外部是否处理防骚扰弹窗
    private static boolean mPreemptOutside = false;

    public static void setPreemptOutside(boolean isOutside) {
        mPreemptOutside = isOutside;
    }

    private void startPreemptDialog(PreemptCastInfo info) {
        PreemptDialog dialog;
        DemoApplication demoApplication = DemoApplication.getApplication();
        try {
            // 弹窗逻辑具体由接入方实现，这里仅仅是一个简单实现参考（PreemptDialog）
            Activity currentActivity = demoApplication.getCurrentActivity();
            Logger.i(TAG, "startPreemptDialog currentActivity:" + currentActivity);
            if (currentActivity != null) {
                dialog = new PreemptDialog(currentActivity, info);
                dialog.show(15 * 1000);
            } else {
                Toast.makeText(demoApplication, "弹窗创建失败", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Logger.w(TAG, "startPreemptDialog fail", e);
            Toast.makeText(demoApplication, "弹窗创建失败", Toast.LENGTH_LONG).show();
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_AUTH_FAILED:
                    Toast.makeText(mContext, "认证失败", Toast.LENGTH_SHORT).show();
                    break;
                case SDK_AUTH_SERVER_FAILED:
                    Toast.makeText(mContext, "连接认证服务器失败", Toast.LENGTH_SHORT).show();
                    break;
                case SDK_DISCONNECT:
                    Toast.makeText(mContext, "连接断开了", Toast.LENGTH_SHORT).show();
                    break;
                case SDK_CAST_PREEMPT:
                    startPreemptDialog((PreemptCastInfo) msg.obj);
                    break;

            }
            return false;
        }

    });

    private IAuthCodeCallback mAuthCallback = new IAuthCodeCallback() {

        @Override
        public void onShowAuthCode(String authCode, int expiry) {
            String msg = "onShowAuthCode" + " authCode: " + authCode + "\nexpiry: " + expiry;
            Logger.i(TAG, msg);

            if (mPwdDialog == null) {
                mPwdDialog = new LeboDialog();
            }
            mPwdDialog.show(authCode, expiry);
        }

        @Override
        public void onDismissAuthCode() {
            Logger.i(TAG, "onDismissAuthCode");
            if (mPwdDialog != null) {
                mPwdDialog.dismiss();
            }
        }
    };

    private IMediaPlayerCallback mPlayerCallback = new IMediaPlayerCallback() {
        @Override
        public void onStart(String s) {
            Logger.i(TAG, "playerCallback onStart:" + s);
        }

        @Override
        public void onError(String s, int type, int what, int extra) {
            Logger.i(TAG, "playerCallback onError:" + type + ",what:" + what + "," + extra);
        }

        @Override
        public void onVideoSizeChange(String s, PlayInfo playInfo) {

        }

        @Override
        public void onStartBuffering(String s, int i) {

        }

        @Override
        public void onStopBuffering(String s, int i) {

        }

        @Override
        public void onSeekTo(String s, int i, int i1) {

        }

        @Override
        public void onSeekComplete(String s, int i) {

        }

        @Override
        public void onStop(String s, int i) {
            Logger.i(TAG, "playerCallback onStop:" + s);
        }

        @Override
        public void onInfo(String s, int i, int i1, int i2) {
            Log.i(TAG, "mediaPlayerCallback onInfo: " + s + ", playertype:" + i + ", what:" + i1 + ", extra:" + i2);
        }
    };

    private IFpsListener mIFpsListener = new IFpsListener() {
        @Override
        public void onFpsData(long[] fps) {

        }

        @Override
        public void onFps(Map<Integer, Integer> fps) {
            Logger.i(TAG, "mIFpsListener onFps decodeFps:" + fps.get(IFpsListener.KEY_FPS_OUT) + ", receiveFps:" + fps.get(IFpsListener.KEY_FPS_IN) + ", mirrorFps:" + fps.get(IFpsListener.KEY_FPS_MIRROR_NET) + ", sysFps:" + fps.get(IFpsListener.KEY_FPS_SYS_NET));
        }

        @Override
        public void onNetDelay(int netDelay) {
            Logger.i(TAG, "mIFpsListener netDelay:" + netDelay);
        }
    };

    private ICloudMirrorCallback mICloudMirrorCallback = new ICloudMirrorCallback() {

        @Override
        public void onReceiveMessage(PassBean passBean) {

            String msg = "onReceiveMessage" + " sourceAppID: " + passBean.in.sourceAppID + "\npassSessionID: " + passBean.passSessionID;
            Logger.i(TAG, msg);
        }
    };

    private IPreemptListener mIPreemptListener = new IPreemptListener() {

        @Override
        public void onRejectConnect(PreemptInfo preemptInfo) {
            Logger.i(TAG, "onRejectConnect preemptInfo: " + preemptInfo);
        }

        @Override
        public boolean onPreemptOutside(PreemptCastInfo preemptCastInfo) {
            Logger.i(TAG, "onPreemptOutside preemptCastInfo: " + preemptCastInfo);
            if (mPreemptOutside) {
                // sdk会根据这个回调的返回值去执行不同逻辑，返回true代表上层自定义弹窗，返回false，sdk实现的弹窗
                // 这里需要异步去实现弹窗，方法执行需要马上返回
                Message message = mHandler.obtainMessage(SDK_CAST_PREEMPT, 0, 0, preemptCastInfo);
                mHandler.sendMessage(message);
                return true;
            }
            return false;
        }
    };


    public static LelinkHelper getInstance() {
        if (mLelinkHelper == null) {
            mLelinkHelper = new LelinkHelper(DemoApplication.getApplication());
        }
        return mLelinkHelper;
    }

    private LelinkHelper(Context context) {
        mContext = context;
        String AppID = context.getString(R.string.app_id);
        String AppSecret = context.getString(R.string.app_secret);
//        mLelinkCast = new LelinkCast(context, AppID, AppSecret,"null","/sdcard/Android/data");
        mLelinkCast = LelinkCast.getInstance();
        InitBean bean = new InitBean();
        bean.appKey = AppID;
        bean.appSecret = AppSecret;
        try {
            bean.soPath = getSoPath();
        } catch (Exception e) {
            Logger.w(TAG, e);
        }
        mLelinkCast.initSDK(context, bean);

    }

    // 测试代码
    // buCode==2的情况。测试可以把buCode里bu的内容拷贝到/sdcard/test/leboData下，如果没有拷贝，就没办法加载
    // 真实使用场景，这个so目录，由集成开发者指定，保证指定目录下由文件，并且有权限加载
    private String getSoPath() throws IOException {
        // test path "/sdcard/Android/data"
        final String extendFile = "/sdcard/test/leboData";
        if (new File(extendFile, "bu.dat").exists()) {
            String targetDir = getBuSoPath();
            File targetFileDir = new File(targetDir);
            if (!targetFileDir.exists()) {
                // 拷贝到应用安装目录下，保证能加载。
                copyDirectory(extendFile, targetDir);
            }
            return targetDir;
        }
        return null;
    }

    private String getBuSoPath() {
        String buPathInData = ContextPath.jointPath(
                ContextPath.getPath(ContextPath.DATA_FILE), VERSION_BU);
        return ContextPath.jointPath(buPathInData, com.hpplay.happyplay.awxm.api.BuildConfig.BU_VERSION_CODE);
    }


    // 复制文件夹函数
    private void copyDirectory(String sourceDir, String targetDir) throws IOException {

        File aimfile = new File(targetDir);
        if (!(aimfile).exists()) { // 查看目录是否存在，不存在则新建
            aimfile.mkdirs();
        }
        // 获取源文件夹下的文件或目录
        File oldfile = new File(sourceDir);
        File[] file = oldfile.listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) // 如果是文件，传递给copyFile()函数进行复制
            {
                // 目标文件
                File aim = new File(targetDir);
                File targetFile = new File(ContextPath.jointPath(aim.getAbsolutePath(), file[i].getName()));
                Utils.copyFile(file[i], targetFile);
            }
            if (file[i].isDirectory()) // 如果是文件夹，则递归调用
            {
                // 要递归复制的源文件夹
                String soursefiles = ContextPath.jointPath(sourceDir, file[i].getName());

                // 要递归复制的目标文件夹
                String aimfiles = ContextPath.jointPath(targetDir, file[i].getName());

                copyDirectory(soursefiles, aimfiles);
            }
        }
    }


    void startServer() {
        if (mCastBean == null || TextUtils.isEmpty(mCastBean.currentName)) {
            return;
        }
        startServer(mCastBean.currentName);
    }

    public static final int LEBO_OPTION_21 = 0x60021;

    void startServer(String deviceName) {
        mAction = IDLE;
        Logger.i(TAG, "startServer mCurrentName: " + mCastBean.currentName + " newName: " + deviceName);
        ServerInfo info = mLelinkCast.getServerInfo();
        if (info == null || info.serviceStatus == ServerInfo.SERVER_IDLE) {// 服务未启动，则启动服务
            // 设置设备名称
            mLelinkCast.setDeviceName(deviceName);
            // 设置投屏码模式： 无密码     在服务启动前设置生效，服务启动后设置则下次生效
            if (mCastBean.authMode == IAPI.AUTH_MODE_FIXED) {
                mLelinkCast.setAuthMode(mCastBean.authMode, mCastBean.authPsd);
            } else {
                mLelinkCast.setAuthMode(mCastBean.authMode, "");
            }
            // 设置投屏码监听
            mLelinkCast.setAuthCodeCallback(mAuthCallback);
            // 设置显示帧率
            mLelinkCast.showFps(mCastBean.showMirrorFps);
            // 设置最大帧率
            mLelinkCast.setMaxFps(mCastBean.mirrorMaxFps);
            // 移除之前的监听
            mLelinkCast.setServerListener(null);
            // 设置服务发布监听
            mLelinkCast.setServerListener(mServerListener);
            // 设置帧率监听
            mLelinkCast.setFpsListener(mIFpsListener);
            // 设置播放器类型
            choosePlayer(mCastBean.videoPlayerType);
            mLelinkCast.setPlayerCallback(mPlayerCallback);
            mLelinkCast.setCloudMirrorCallback(mICloudMirrorCallback);
            // 设置Surface类型
            setMirrorSurfaceType(mCastBean.mirrorSurfaceType);
            // 设置音量类型
            setVolumeType(mCastBean.volumeType);

            // 参考:https://cloud.lebo.cn/document/6c3bd08f6ffb1890.html
            //设置授权模式为license认证授权，必须授权后才能使用投屏功能
            mLelinkCast.setOption(Option.LEBO_OPTION_12, Option.PERMISSION_MODE_CLOUD_LICENSE);
            //设置设备唯一标识tsn，可传入Android ID等
            String tsn = "";
            mLelinkCast.setOption(Option.LEBO_OPTION_14, tsn);

            LelinkCast.getInstance().setOption(Option.LEBO_OPTION_20, Option.PIN_ALL);
            mLelinkCast.setOption(LEBO_OPTION_21, new IPinCodeCallback() {
                @Override
                public void onSuccess(String s, int i) {
                    Log.i(TAG, "onSuccess: " + s + "  " + i);
                    if (mUIUpdate != null) {
                        mUIUpdate.onUpdateText("Pincode:" + s + "\r\n");
                    }
                }

                @Override
                public void onError(int i, int i1) {
                    Log.i(TAG, "onError: " + i1 + "  " + i);
                }
            });

            mLelinkCast.setPreemptListener(mIPreemptListener);
            //引流接口设置
            mLelinkCast.setOption(LEBO_OPTION_108, new IRedirectListener() {
                @Override
                public boolean onNotifyDownloadApp(RedirectBean redirectBean) {
                    Logger.i(TAG, "onNotifyDownloadApp,RedirectManager,redirectBean: " + redirectBean.toString());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mLelinkCast.getOption(LEBO_OPTION_109) != null) {
                                IRedirectAppResultControl iRedirectAppResultControl = (IRedirectAppResultControl) mLelinkCast.getOption(LEBO_OPTION_109);
                                iRedirectAppResultControl.setAppDownloadResult(redirectBean, RedirectBean.REDIRECT_APP_FAIL);
                            }
                        }
                    }, 10 * 1000);
                    return true;
                }

                @Override
                public boolean onNotifyPlay(RedirectBean redirectBean) {
                    Logger.i(TAG, "onNotifyPlay,RedirectManager,redirectBean: " + redirectBean.toString());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mLelinkCast.getOption(LEBO_OPTION_109) != null) {
                                IRedirectAppResultControl iRedirectAppResultControl = (IRedirectAppResultControl) mLelinkCast.getOption(LEBO_OPTION_109);
                                iRedirectAppResultControl.setAppOpenResult(redirectBean, RedirectBean.REDIRECT_APP_SUCCESS);
                            }
                        }
                    }, 3 * 1000);
                    return true;
                }
            });

            // 启动服务
            mLelinkCast.startServer();

            //测试用
            /*
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.hpplay.sdk.sink.test");
            mContext.registerReceiver(myReceiver,filter);
            */

            if (mUIUpdate != null) {
                mUIUpdate.onUpdateText("从初始化状态启动");
            }
        } else { // 服务已经启动，修改名称即可
            if (deviceName.equals(mCastBean.currentName)) {
                if (mUIUpdate != null) {
                    mUIUpdate.onUpdateText("相同的设备名，忽略本次启动请求");
                }
                return;
            }
            // 修改设备名称
            mLelinkCast.changeDeviceName(deviceName);
            if (mUIUpdate != null) {
                mUIUpdate.onUpdateText("以新的名字启动");
            }
        }
        mCastBean.currentName = deviceName;
    }

    public void stopServer() {
        mAction = IDLE;
        mLelinkCast.stopServer();
    }

    public ServerInfo getServerInfo() {
        return mLelinkCast.getServerInfo();
    }

    private static final int IDLE = 1; // 未初始化状态
    private static final int RESTARTING = 2; // 一键修复中
    private int mAction = IDLE; // Restarting

    public void restartServer() {
        mLelinkCast.stopServer();
        mLelinkCast.startServer();
        mAction = RESTARTING;
    }

    public void startAdvertise() {
        mLelinkCast.enableBLEPublish(1);
    }

    public void stopAdvertise() {
        mLelinkCast.enableBLEPublish(0);
    }

    public void setActivityOrientationStatus(int status) {
        mLelinkCast.setActivityOrientationStatus(status);
        Toast.makeText(mContext, "宽高变化时" + (status == 1 ? "设置" : "不设置") + "Activity方向", Toast.LENGTH_SHORT).show();
    }

    public void setMiniProgramQRListener(IMiniProgramQRListener miniProgramQRListener) {
        Logger.i(TAG, "setMiniProgramQRListener");
        mLelinkCast.setMiniProgramQRListener(miniProgramQRListener);
    }

    public void seUIUpdateListener(IUIUpdateListener castStateChangeListener) {
        this.mUIUpdate = castStateChangeListener;
    }

    public void setBleAdvertisePublishListener(BleAdvertiseListener bleAdvertiseListener) {
        mLelinkCast.setBLEPublishListener(bleAdvertiseListener);
    }

    public void setPreemptMode(int preemptMode, int netType) {
        mLelinkCast.setPreemptModel(preemptMode, netType);
    }

    public void changeAuthMode(int authMode, String pwd) {
        if (mServerInfo == null || mServerInfo.serviceStatus != ServerInfo.SERVER_STARTED) {
            return;
        }
        mCastBean.authMode = authMode;
        mCastBean.authPsd = pwd;
        if (authMode == IAPI.AUTH_MODE_FIXED) {
            if (!TextUtils.isEmpty(pwd)) {
                mLelinkCast.changeAuthMode(authMode, pwd);
            }
        } else {
            mLelinkCast.changeAuthMode(authMode, null);
        }
    }

    public void showDeviceList(int netType) {
        mLelinkCast.showPreemptDeviceList(netType);
    }

    public void setShowFps(boolean show) {
        mCastBean.showMirrorFps = show;
        mLelinkCast.showFps(show);
    }

    /**
     * @param maxFps 30 60 0
     */
    public void setMaxFps(int maxFps) {
        mCastBean.mirrorMaxFps = maxFps;
        mLelinkCast.setMaxFps(maxFps);
    }

    public void choosePlayer(int type) {
        mCastBean.videoPlayerType = type;
        mLelinkCast.choosePlayer(type);
    }

    public void setVolumeType(int type) {
        mCastBean.volumeType = type;
        mLelinkCast.setOption(IAPI.OPTION_SET_VOLUME_TYPE, type);
    }

    public void setMirrorSurfaceType(int surfaceType) {
        mCastBean.mirrorSurfaceType = surfaceType;
        mLelinkCast.setMirrorSurfaceType(surfaceType);
    }

    public void setAudioProtocol(int protocol) {
        mCastBean.audioProtocol = protocol;
        mLelinkCast.setAudioProtocol(protocol);
    }

    public void setMirrorSmooth(int type) {
        mCastBean.mirrorSmoothMode = type;
        mLelinkCast.setMirrorSmoothMode(type);
    }

    public void setMirrorFrameInsert(int type) {
        PlayerconfigSetting setting = new PlayerconfigSetting();
        setting.frameInsertType = type;
        mLelinkCast.setPlayerconfigSetting(setting);
    }

    public void resetMirrorPlayer(int mode) {
        mCastBean.mirrorResetMode = mode;
        mLelinkCast.resetPlayerWhenMirrorRotate(mode);
    }

    public void setMultiMirrorMode(int mode) {
        mCastBean.multiMirror = mode;
        mLelinkCast.setMultiMirrorMode(mode);
    }

    public void setLelinkFpAssistant(int status) {
        mLelinkCast.setLelinkFPAssistant(status);
    }

    public void setDisplayMode(int mode) {
        mLelinkCast.setDisplayMode(mode);
    }

    public void userPreemptOption(PreemptCastInfo info) {
        mLelinkCast.userPreemptOption(info);
    }

    private IServerListener mServerListener = new IServerListener() {
        @Override
        public void onStart(int id, ServerInfo info) {
            mServerInfo = mLelinkCast.getServerInfo();
            mCastBean.currentName = info.deviceName;
            String deviceName = "onStart service: " + id + " deviceName: " + info.deviceName + "\ncurrentBUVersion: " + com.hpplay.sdk.sink.util.BuildConfig.sBUVersion;
            if (mUIUpdate != null) {
                mUIUpdate.onUpdateText(deviceName);
                mUIUpdate.onUpdateNetwork();
                mUIUpdate.onServerStart();
            }
            mLelinkCast.vodInit();
        }

        @Override
        public void onStop(int id) {
            String info = "onStop service: " + id;
            if (mUIUpdate != null) {
                mUIUpdate.onUpdateText(info);
                mUIUpdate.onUpdateNetwork();
            }
            if (mAction == RESTARTING) {
                mAction = IDLE;
                startServer(mCastBean.currentName);
            } else {
                mServerInfo = null;
            }
        }

        @Override
        public void onError(int id, int what, int extra) {
            String info = "onError service: " + id + " what: " + what + " extra: " + extra;
            if (mUIUpdate != null) {
                mUIUpdate.onUpdateText(info);
            }
            mServerInfo = null;
        }

        @Override
        public void onAuthSDK(int id, int status) {
            Logger.i(TAG, "onAuthSDK status: " + status);

            switch (status) {
                case IServerListener.SDK_AUTH_SUCCESS:
//                    Toast.makeText(mContext,"认证成功",Toast.LENGTH_SHORT).show();
                    break;
                case IServerListener.SDK_AUTH_FAILED:
                    mHandler.sendEmptyMessage(SDK_AUTH_FAILED);
                    break;
                case IServerListener.SDK_AUTH_SERVER_FAILED:
                    mHandler.sendEmptyMessage(SDK_AUTH_SERVER_FAILED);
                    break;
            }
        }

        @Override
        public void onCast(int id, CastInfo info) {
            StringBuilder msg = new StringBuilder("onCast service: ");
            msg.append("\nSDK版本号: ").append(info.sdkVersion);
            msg.append("\n事件类型: ").append(CastInfoTransform.getInfoType(info.infoType));
            msg.append("\nkey: ").append(info.key);
            msg.append("\nurl: ").append(info.url);
            msg.append("\n内容来源: ").append(CastInfoTransform.getCastType(info.castType));
            msg.append("\n媒体类型: ").append(info.mimeType);
            msg.append("\n协议: ").append(CastInfoTransform.getProtocol(info.protocol));
            msg.append("\n起播时间: ").append(info.castCost);
            msg.append("\n投屏时长: ").append(info.castDuration);
            msg.append("\n后贴数据: ").append(info.postInfo);
            if (info.stopInfo != null && info.infoType == CastInfo.TYPE_STOP) {
                msg.append("\n结束原因: ").append(CastInfoTransform.getStopReason(info.stopInfo.stopReason));
                msg.append("\n结束详细类型: ").append(info.stopInfo.stopDetail);
            }
            if (info.errorInfo != null && info.infoType == CastInfo.TYPE_ONERROR) {
                //错误码说明：https://doc.hpplay.com.cn/web/#/27/1015
                msg.append("\n错误类型: ").append(info.errorInfo.errorType);
                msg.append("\n错误详细码: ").append(info.errorInfo.errorCode);
            }
            Logger.i(TAG, msg.toString());
        }

        @Override
        public void onAuthConnect(int id, String authCode, int expiry) {

        }

        @Override
        public void onConnect(int id, ClientInfo info) {
            String msg = "onConnect name " + info.name + "  id: " + info.clientID;
            Logger.i(TAG, msg);
        }

        @Override
        public void onDisconnect(int i, ClientInfo clientInfo) {
            mHandler.sendEmptyMessage(SDK_DISCONNECT);

        }

    };

    public void startDMP() {
        mLelinkCast.startDMPServer();
    }

    public void stopDMP() {
        mLelinkCast.stopDMPServer();
    }

    public void searchDMP() {
        mLelinkCast.searchDMPDevices();
    }

    public void browseDevice(DeviceBean deviceBean) {
        mLelinkCast.browseDMPDeviceDir(deviceBean);
    }

    public void browseFolder(String actionUrl, String folderId) {
        mLelinkCast.browseDMPFolder(actionUrl, folderId);
    }

    public void setDMPListener(OnDMPListener dmpListener) {
        mLelinkCast.setDMPListener(dmpListener);
    }

    public void uploadLog(UploadLogBean logBean) {
        mLelinkCast.uploadLog(logBean);
    }

    public void enableShowFavoriteWindow(boolean show) {
        PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplication().getApplicationContext())
                .edit()
                .putBoolean(DEMO_KEY_SHOW_CONFIRM_WINDOW, show)
                .apply();
        mLelinkCast.enableShowFavoriteWindow(show ? 1 : 0);
    }

    public void setLocalServerStatus(boolean status) {
        PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplication().getApplicationContext())
                .edit()
                .putBoolean(DEMO_KEY_LOCALSERVER_STATUS, status)
                .apply();
        mLelinkCast.setHttpserverPort(status ? 0 : IAPI.HTTPSERVER_DISABLE_PORT);
    }

    public void setAudiofocus(boolean status) {
        PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplication().getApplicationContext())
                .edit()
                .putBoolean(DEMO_KEY_AUDIOFOCUS_STATUS, status)
                .apply();
        mLelinkCast.setAudiofocus(status ? IAPI.AUDIOFOCUS_ON : IAPI.AUDIOFOCUS_OFF);
    }

    public void setLoadingViewState(boolean status) {
        PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplication().getApplicationContext())
                .edit()
                .putBoolean(DEMO_KEY_LOADING_SHOW_STATUS, status)
                .apply();
        mLelinkCast.setLoadingViewState(status ? IAPI.LOAD_VIEW_ON : IAPI.LOAD_VIEW_OFF);
    }

    public String getLogUrl() {
        if (mServerInfo == null) {
            return null;
        }
        return "http://" + Utils.getIP(mContext) + ":" + mServerInfo.remotePort + "/log";
    }

    public void setRTCProtocol(int protocol) {
        mLelinkCast.setRTCProtocol(protocol);
    }

    public int getRTCProtocol() {
        return mLelinkCast.getRTCProtocol();
    }

    public int setOption(int option, Object... values) {
        Object result = mLelinkCast.setOption(option, values);
        int callResult = Integer.parseInt(result.toString());
        if (callResult == IAPI.INVALID_CALL) {
            Logger.w(TAG, "setOption invalid call, option: " + option);
        }
        return callResult;
    }

    public int performAction(int action, Object... values) {
        Object result = mLelinkCast.performAction(action, values);
        int callResult = Integer.parseInt(result.toString());
        if (callResult == IAPI.INVALID_CALL) {
            Logger.w(TAG, "performAction invalid call, action: " + action);
        }
        return callResult;
    }

    public <T> T getOption(int option, Class<T> classOfT) {
        if (mLelinkCast != null) {
            Object object = mLelinkCast.getOption(option);
            try {
                return classOfT.cast(object);
            } catch (Exception e) {
                Logger.w(TAG, e);
            }
        }
        return null;
    }

    //测试用
    //private MyReceiver myReceiver = new MyReceiver();

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logger.i(TAG, "onReceive action: " + action);
            int t = intent.getIntExtra("value", 0);
            Logger.i(TAG, "onReceive value: " + t);
            //有些api接口测试，需要在播放界面时候调用，可以通过shell发广播形式
            //adb shell am broadcast -a com.hpplay.sdk.sink.test --es test_string "this is test string" --ei test_int 100 --ez test_boolean true
            //setDisplayMode(t);
        }
    }
}