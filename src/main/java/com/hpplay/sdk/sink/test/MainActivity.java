package com.hpplay.sdk.sink.test;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hpplay.happyplay.awxm.api.BuildConfig;
import com.hpplay.sdk.sink.api.IAPI;
import com.hpplay.sdk.sink.api.ServerInfo;
import com.hpplay.sdk.sink.test.adapter.CastInfoAdapter;
import com.hpplay.sdk.sink.test.dmp.DMPActivity;
import com.hpplay.sdk.sink.api.IMiniProgramQRListener;
import com.hpplay.sdk.sink.api.BleAdvertiseListener;
import com.hpplay.sdk.sink.api.BleAdvertisePublishState;
//import com.hpplay.sdk.sink.vod.VodManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener {
    private final String TAG = "MainActivity";
    private TextView mWifiTv;
    private Button mVersionBtn;
    private Button mDeviceNameBtn1;
    private Button mDeviceNameBtn2;
    private ListView mListView;
    private CastInfoAdapter mAdapter;
    private List<String> mCastInfoList = new ArrayList<>();
    private LelinkHelper mHelper;
    private QRFragment mQRFragment = new QRFragment();
    private EditText mAuthPsdEdit;
    private Spinner mCloudPreemptSpinner, mLocalPreemptSpinner, mAuthSpinner;
    private Spinner mFpsSpinner, mMaxFpsSpinner, mMirrorSurfaceSpinner, mAudioProtocolSpinner;
    private Spinner mMirrorResetSpinner, mMirrorSmoothSpinner, mMirrorFrameInsertSpinner;
    private Spinner mPlayerSpinner, mVolumeSpinner;
    private NetworkReceiver mNetworkReceiver;
    private CastBean mCastBean = CastBean.getInstance();
    private String mMiniProgramQrStr;
    private DateFormat mDateFormat;
    private RadioButton rbRtcDef, rbRtcTencent, rbRtcZego, rbRtcNe;
    private Switch mSwitchShowFavoriteWindow, mSwitchLocalServer, mPreemptShow;
    private Switch mSwitchEnableAudiofocus, mSwitchLoadingview;

    private String getDeviceName1() {
        return Utils.createDeviceName(this) + "-1";
    }

    private String getDeviceName2() {
        return Utils.createDeviceName(this) + "-2";
    }

    private IMiniProgramQRListener mMiniProgramQRListener = new IMiniProgramQRListener() {
        @Override
        public void onMiniProgramQRReady(String s) {
            Logger.i(TAG, "onMiniProgramQRReady onQRReady " + s);
            mMiniProgramQrStr = s;
        }
    };

    private IUIUpdateListener mUIUpdateListener = new IUIUpdateListener() {
        @Override
        public void onUpdateText(String msg) {
            addMsg(msg);
        }

        @Override
        public void onUpdateNetwork() {
            if (mWifiTv != null) {
                mWifiTv.setText("WiFi:" + Utils.getNetWorkName(MainActivity.this)
                        + "\n" + Utils.getIP(MainActivity.this));
            }
        }

        @Override
        public void onServerStart() {
            if (null != mVersionBtn) {
                mVersionBtn.setText("SDK:" + BuildConfig.FLAVOR
                        + "-" + BuildConfig.BUILD_TYPE
                        + "-" + BuildConfig.VERSION_CODE
                        + "\n" + com.hpplay.sdk.sink.util.BuildConfig.sBUVersionName);
            }
            // 启动防骚扰模式
            mCloudPreemptSpinner.setSelection(1);
            // 公共场合，局域网建议开启防骚扰：mLocalPreemptSpinner.setSelection(1)
            mLocalPreemptSpinner.setSelection(0);
            initRtcRadioButtonValue();
        }
    };

    private BleAdvertiseListener mBleAdvertiseListener = new BleAdvertiseListener() {
        @Override
        public void onPublishResult(final BleAdvertisePublishState bleAdvertisePublishState) {
            Logger.i(TAG, "onPublishResult: " + bleAdvertisePublishState);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "result: " + bleAdvertisePublishState, Toast.LENGTH_LONG).show();
                }
            });

        }
    };

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_device_name_1:
                    mHelper.startServer(getDeviceName1());
                    break;
                case R.id.btn_device_name_2:
                    mHelper.startServer(getDeviceName2());
                    break;
                case R.id.btn_server_stop:
                    mHelper.stopServer();
                    break;
                case R.id.btn_log:
                    (MainActivity.this).getFragmentManager().beginTransaction()
                            .add(R.id.container, new LogFragment())
                            .addToBackStack("miniProgramQr")
                            .commitAllowingStateLoss();
                    break;
                case R.id.miniProgramQrBtn:
                    if (mQRFragment.isAdded()) {
                        return;
                    }
                    mQRFragment.setQRStr(mMiniProgramQrStr);
                    try {
                        getFragmentManager().beginTransaction()
                                .add(R.id.container, mQRFragment)
                                .addToBackStack("miniProgramQr")
                                .commitAllowingStateLoss();
                    } catch (Exception e) {
                        Logger.w(TAG, "miniProgramQr error:" + e.getMessage());
                    }
                    break;
                case R.id.dmpBtn:
                    startActivity(new Intent(MainActivity.this, DMPActivity.class));
                    break;
                case R.id.cloudDeviceListBtn:
                    mHelper.showDeviceList(IAPI.PREEMPT_CLOUD);
                    break;
                case R.id.localDeviceListBtn:
                    mHelper.showDeviceList(IAPI.PREEMPT_LOCAL);
                    break;
                case R.id.btn_start_advertise:
                    mHelper.startAdvertise();
                    break;
                case R.id.btn_stop_advertise:
                    mHelper.stopAdvertise();
                    break;
                case R.id.btn_set_Orientation0:
                    mHelper.setActivityOrientationStatus(0);
                    break;
                case R.id.btn_set_Orientation1:
                    mHelper.setActivityOrientationStatus(1);
                    break;
            }
        }
    };

    private AdapterView.OnItemSelectedListener mItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (mHelper == null) {
                return;
            }
            Logger.i(TAG, "mItemSelectedListener onItemSelected " + position);
            if (parent == mCloudPreemptSpinner) {
                switch (position) {
                    case 0:
                        mHelper.setPreemptMode(IAPI.PREEMPT_MODE_FREE, IAPI.PREEMPT_CLOUD);
                        break;
                    case 1:
                        mHelper.setPreemptMode(IAPI.PREEMPT_MODE_AVOID_HARASS, IAPI.PREEMPT_CLOUD);
                        break;
                    case 2:
                        mHelper.setPreemptMode(IAPI.PREEMPT_MODE_RESTRICTED, IAPI.PREEMPT_CLOUD);
                        break;
                }
            } else if (parent == mLocalPreemptSpinner) {
                switch (position) {
                    case 0:
                        mHelper.setPreemptMode(IAPI.PREEMPT_MODE_FREE, IAPI.PREEMPT_LOCAL);
                        break;
                    case 1:
                        mHelper.setPreemptMode(IAPI.PREEMPT_MODE_AVOID_HARASS, IAPI.PREEMPT_LOCAL);
                        break;
                    case 2:
                        mHelper.setPreemptMode(IAPI.PREEMPT_MODE_RESTRICTED, IAPI.PREEMPT_LOCAL);
                        break;
                    case 3:
                        mHelper.setPreemptMode(IAPI.PREEMPT_MODE_FORCE_CONFIRM, IAPI.PREEMPT_LOCAL);
                        break;
                    case 4:
                        mHelper.setPreemptMode(IAPI.PREEMPT_MODE_EASY, IAPI.PREEMPT_LOCAL);
                        break;
                }
            } else if (parent == mAuthSpinner) {
                switch (position) {
                    case 0:
                        mHelper.changeAuthMode(IAPI.AUTH_MODE_FREE, "");
                        break;
                    case 1:
                        String psd = mAuthPsdEdit.getText().toString();
                        if (!TextUtils.isEmpty(psd)) {
                            mHelper.changeAuthMode(IAPI.AUTH_MODE_FIXED, psd);
                        } else {
                            Logger.w(TAG, "invalid call");
                            Toast.makeText(MainActivity.this, "请先输入密码", Toast.LENGTH_LONG).show();
                            mAuthSpinner.setSelection(0);
                        }
                        break;
                    case 2:
                        mHelper.changeAuthMode(IAPI.AUTH_MODE_RANDOM, "");
                        break;
                }
            } else if (parent == mFpsSpinner) {
                mHelper.setShowFps(position == 1);
            } else if (parent == mMaxFpsSpinner) {
                mHelper.setMaxFps(position * 30);
            } else if (parent == mMirrorSurfaceSpinner) {
                switch (position) {
                    case 0:
                        mHelper.setMirrorSurfaceType(IAPI.SURFACE_AUTO);
                        break;
                    case 1:
                        mHelper.setMirrorSurfaceType(IAPI.SURFACE_SURFACEVIEW);
                        break;
                    case 2:
                        mHelper.setMirrorSurfaceType(IAPI.SURFACE_GLSURFACEVIEW);
                        break;
                }
            } else if (parent == mAudioProtocolSpinner) {
                Logger.i(TAG, "setAudioProtocol position: " + position);
                switch (position) {
                    case 0:
                        mHelper.setAudioProtocol(IAPI.OPTION_AUDIO_PROTOCOL_UDP);
                        break;
                    case 1:
                        mHelper.setAudioProtocol(IAPI.OPTION_AUDIO_PROTOCOL_TCP);
                        break;
                }
            } else if (parent == mMirrorResetSpinner) {
                switch (position) {
                    case 0:
                        mHelper.resetMirrorPlayer(IAPI.MIRROR_RESET_AUTO);
                        break;
                    case 1:
                        mHelper.resetMirrorPlayer(IAPI.MIRROR_RESET_OPEN);
                        break;
                    case 2:
                        mHelper.resetMirrorPlayer(IAPI.MIRROR_RESET_CLOSE);
                        break;
                }
            } else if (parent == mMirrorSmoothSpinner) {
                mHelper.setMirrorSmooth(position);
            } else if (parent == mMirrorFrameInsertSpinner) {
                mHelper.setMirrorFrameInsert(position);
            } else if (parent == mPlayerSpinner) {
                switch (position) {
                    case 0:
                        mHelper.choosePlayer(IAPI.PLAYER_DEFAULT);
                        break;
                    case 1:
                        mHelper.choosePlayer(IAPI.PLAYER_SYSTEM);
                        break;
                    case 2:
                        mHelper.choosePlayer(IAPI.PLAYER_IJK);
                        break;
                }
            } else if (parent == mVolumeSpinner) {
                mHelper.setVolumeType(position);
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
        checkPermission();
        registerReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDisplayName();
    }

    private void initData() {
        mDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        initView();
        initCastServer();
    }

    private void initCastServer() {
        if (mHelper != null) {
            return;
        }
        mHelper = LelinkHelper.getInstance();
        mHelper.seUIUpdateListener(mUIUpdateListener);
        mHelper.setMiniProgramQRListener(mMiniProgramQRListener);
        mHelper.setBleAdvertisePublishListener(mBleAdvertiseListener);
        ServerInfo info = mHelper.getServerInfo();
        Logger.i(TAG, "initData info: " + info);
        if (info == null || info.serviceStatus == ServerInfo.SERVER_IDLE) {
            mHelper.startServer(getDeviceName1());
        } else {
            addMsg("cast server already started");
            mVersionBtn.setText("SDK:" + BuildConfig.FLAVOR + "-" + BuildConfig.BUILD_TYPE
                    + "-" + BuildConfig.VERSION_CODE + "\n" + com.hpplay.sdk.sink.util.BuildConfig.sBUVersionName);
        }
    }

    private void checkPermission() {
        String[] permissionsCheck = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        List<String> permissionList = new ArrayList<>();

        for (String permissionStr : permissionsCheck) {
            if (ContextCompat.checkSelfPermission(this,
                    permissionStr) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissionStr);
            }
        }
        String[] permissionArr = new String[]{};
        if (permissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(permissionArr), 100);
            return;
        }
        initData();
    }

    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 100) {
            Logger.w(TAG, "onRequestPermissionsResult failed requestCode: " + requestCode);
            finish();
            return;
        }
        if (grantResults.length <= 0) {
            Logger.w(TAG, "onRequestPermissionsResult grantResults.length: " + grantResults.length);
            finish();
            return;
        }
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Logger.w(TAG, "onRequestPermissionsResult grantResults[0]: " + grantResults[0]);
            finish();
            return;
        }
        initData();
    }

    protected void onDestroy() {
        Logger.i(TAG, "onDestroy");
        super.onDestroy();
        unRegisterReceiver();
        if (mHelper != null) {
            mHelper.seUIUpdateListener(null);
            mHelper.setMiniProgramQRListener(null);
        }
    }

    private void updateDisplayName() {
        if (mWifiTv == null) {
            return;
        }
        mWifiTv.setText("WiFi:" + Utils.getNetWorkName(MainActivity.this)
                + "\n" + Utils.getIP(MainActivity.this));
        mDeviceNameBtn1.setText(getDeviceName1());
        mDeviceNameBtn2.setText(getDeviceName2());
    }

    private void initView() {
        mVersionBtn = (Button) findViewById(R.id.btn_version);
        mVersionBtn.setOnClickListener(mOnClickListener);
        mWifiTv = (TextView) findViewById(R.id.wifiTxt);
        mDeviceNameBtn1 = (Button) findViewById(R.id.btn_device_name_1);
        mDeviceNameBtn1.setOnClickListener(mOnClickListener);
        mDeviceNameBtn1.requestFocus();
        mDeviceNameBtn2 = (Button) findViewById(R.id.btn_device_name_2);
        mDeviceNameBtn2.setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_server_stop).setOnClickListener(mOnClickListener);
        findViewById(R.id.miniProgramQrBtn).setOnClickListener(mOnClickListener);
        findViewById(R.id.cloudDeviceListBtn).setOnClickListener(mOnClickListener);
        findViewById(R.id.localDeviceListBtn).setOnClickListener(mOnClickListener);
        findViewById(R.id.dmpBtn).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_log).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_start_advertise).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_stop_advertise).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_set_Orientation0).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_set_Orientation1).setOnClickListener(mOnClickListener);

        List<String> authList = new ArrayList<String>();
        mAuthSpinner = (Spinner) findViewById(R.id.authSpinner);
        authList.add("无密码");
        authList.add("固定密码");
        authList.add("随机密码");
        initSpinner(mAuthSpinner, authList);
        mAuthPsdEdit = (EditText) findViewById(R.id.authPsd);

        mCloudPreemptSpinner = (Spinner) findViewById(R.id.cloudPreemptSpinner);
        List<String> preemptList = new ArrayList<String>();
        preemptList.add("自由模式");
        preemptList.add("防骚扰模式");
        preemptList.add("独占模式");
        initSpinner(mCloudPreemptSpinner, preemptList);

        mLocalPreemptSpinner = (Spinner) findViewById(R.id.localPreemptSpinner);
        List<String> localPreemptList = new ArrayList<String>();
        localPreemptList.add("自由模式");
        localPreemptList.add("防骚扰模式");
        localPreemptList.add("独占模式");
        localPreemptList.add("强制模式");
        localPreemptList.add("简易模式");
        initSpinner(mLocalPreemptSpinner, localPreemptList);

        List<String> fpsList = new ArrayList<String>();
        mFpsSpinner = (Spinner) findViewById(R.id.showFpsSpinner);
        fpsList.add("不显示");
        fpsList.add("显示");
        initSpinner(mFpsSpinner, fpsList);
        mFpsSpinner.setSelection(mCastBean.showMirrorFps ? 1 : 0);

        List<String> maxFpsList = new ArrayList<String>();
        mMaxFpsSpinner = (Spinner) findViewById(R.id.maxFpsSpinner);
        maxFpsList.add("自动");
        maxFpsList.add("30");
        maxFpsList.add("60");
        initSpinner(mMaxFpsSpinner, maxFpsList);
        mMaxFpsSpinner.setSelection(mCastBean.mirrorMaxFps / 30);

        List<String> surfaceList = new ArrayList<String>();
        mMirrorSurfaceSpinner = (Spinner) findViewById(R.id.surfaceSpinner);
        surfaceList.add("自动");
        surfaceList.add("标准模式");
        surfaceList.add("兼容模式");
        initSpinner(mMirrorSurfaceSpinner, surfaceList);
        mMirrorSurfaceSpinner.setSelection(mCastBean.mirrorSurfaceType);

        List<String> protocolList = new ArrayList<String>();
        mAudioProtocolSpinner = (Spinner) findViewById(R.id.audioProtocolSpinner);
        protocolList.add("udp");
        protocolList.add("tcp");
        initSpinner(mAudioProtocolSpinner, protocolList);
        mAudioProtocolSpinner.setSelection(mCastBean.audioProtocol - 1);

        List<String> mirrorResetList = new ArrayList<>();
        mMirrorResetSpinner = (Spinner) findViewById(R.id.mirrorResetSpinner);
        mirrorResetList.add("自动");
        mirrorResetList.add("重置");
        mirrorResetList.add("不重置");
        initSpinner(mMirrorResetSpinner, mirrorResetList);
        mMirrorResetSpinner.setSelection(mCastBean.mirrorResetMode);

        List<String> smoothList = new ArrayList<String>();
        mMirrorSmoothSpinner = (Spinner) findViewById(R.id.avSmoothSpinner);
        smoothList.add("实时性优先");
        smoothList.add("流畅优先");
        initSpinner(mMirrorSmoothSpinner, smoothList);
        mMirrorSmoothSpinner.setSelection(mCastBean.mirrorSmoothMode);

        List<String> insertList = new ArrayList<String>();
        mMirrorFrameInsertSpinner = (Spinner) findViewById(R.id.frameInsertSpinner);
        insertList.add("自动");
        insertList.add("开启");
        insertList.add("关闭");
        initSpinner(mMirrorFrameInsertSpinner, insertList);
        mMirrorFrameInsertSpinner.setSelection(0);

        List<String> playerList = new ArrayList<String>();
        mPlayerSpinner = (Spinner) findViewById(R.id.playerSpinner);
        playerList.add("自动");
        playerList.add("系统播放器");
        playerList.add("乐播播放器");
        initSpinner(mPlayerSpinner, playerList);
        mPlayerSpinner.setSelection(mCastBean.videoPlayerType);

        List<String> volumeList = new ArrayList<String>();
        mVolumeSpinner = findViewById(R.id.volumeSpinner);
        volumeList.add("自动");
        volumeList.add("媒体音量");
        volumeList.add("系统音量");
        volumeList.add("媒体音量&系统音量");
        initSpinner(mVolumeSpinner, volumeList);
        mVolumeSpinner.setSelection(mCastBean.volumeType);

        mSwitchShowFavoriteWindow = findViewById(R.id.switchShowFavoriteWindow);
        mSwitchShowFavoriteWindow.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(LelinkHelper.DEMO_KEY_SHOW_CONFIRM_WINDOW, false));
        mSwitchShowFavoriteWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LelinkHelper.getInstance().enableShowFavoriteWindow(isChecked);
            }
        });
        mSwitchLocalServer = findViewById(R.id.switch_localserver);
        mSwitchLocalServer.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(LelinkHelper.DEMO_KEY_LOCALSERVER_STATUS, true));
        mSwitchLocalServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LelinkHelper.getInstance().setLocalServerStatus(isChecked);
            }
        });

        mPreemptShow = findViewById(R.id.switchShowPreemptWindow);
        mPreemptShow.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(LelinkHelper.DEMO_KEY_PREEMPT_SHOW_STATUS, false));
        mPreemptShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LelinkHelper.getInstance().setPreemptOutside(isChecked);
            }
        });

        mSwitchEnableAudiofocus = findViewById(R.id.switchEnableAudiofocus);
        mSwitchEnableAudiofocus.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(LelinkHelper.DEMO_KEY_AUDIOFOCUS_STATUS, true));
        mSwitchEnableAudiofocus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LelinkHelper.getInstance().setAudiofocus(isChecked);
            }
        });

        mSwitchLoadingview = findViewById(R.id.switchLoadingview);
        mSwitchLoadingview.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(LelinkHelper.DEMO_KEY_LOADING_SHOW_STATUS, true));
        mSwitchLoadingview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LelinkHelper.getInstance().setLoadingViewState(isChecked);
            }
        });


        mListView = (ListView) findViewById(R.id.castInfoListView);
        mAdapter = new CastInfoAdapter(this, mCastInfoList);
        mListView.setAdapter(mAdapter);

        initRtcRadioButton();
    }

    private void initRtcRadioButton() {
        rbRtcDef = findViewById(R.id.rb_rtc_def);
        rbRtcTencent = findViewById(R.id.rb_rtc_tencent);
        rbRtcZego = findViewById(R.id.rb_rtc_zego);
        rbRtcNe = findViewById(R.id.rb_rtc_ne);
        rbRtcDef.setOnCheckedChangeListener(this);
        rbRtcTencent.setOnCheckedChangeListener(this);
        rbRtcZego.setOnCheckedChangeListener(this);
        rbRtcNe.setOnCheckedChangeListener(this);
    }

    private void initRtcRadioButtonValue() {
        int rtcProtocol = LelinkHelper.getInstance().getRTCProtocol();
        switch (rtcProtocol) {
            case 0:
                rbRtcDef.setChecked(true);
                break;
            case 4:
                rbRtcTencent.setChecked(true);
                break;
            case 5:
                rbRtcZego.setChecked(true);
                break;
            case 6:
                rbRtcNe.setChecked(true);
                break;
        }
    }

    private void addMsg(String msg) {
        if (mListView != null && mAdapter != null) {
            mCastInfoList.add(0, msg + " " + mDateFormat.format(new Date()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void initSpinner(Spinner spinner, List<String> dataList) {
        ArrayAdapter arr_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataList);
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arr_adapter);
        spinner.setOnItemSelectedListener(mItemSelectedListener);
    }

    private void registerReceiver() {
        Logger.i(TAG, "registerReceiver");
        if (mNetworkReceiver != null) {
            return;
        }
        mNetworkReceiver = new NetworkReceiver(this);
        mNetworkReceiver.setUIUpdate(mUIUpdateListener);
        registerReceiver(mNetworkReceiver, mNetworkReceiver.getIntentFilter());
    }

    private void unRegisterReceiver() {
        Logger.i(TAG, "unRegisterReceiver");
        if (mNetworkReceiver != null) {
            unregisterReceiver(mNetworkReceiver);
        }
        mNetworkReceiver = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == null || !isChecked) {
            return;
        }
        Logger.i(TAG, "======" + LelinkHelper.getInstance().getRTCProtocol());
        switch (buttonView.getId()) {
            case R.id.rb_rtc_def:
                LelinkHelper.getInstance().setRTCProtocol(IAPI.RTC_PROTOCOL_DEFAULT);
                break;
            case R.id.rb_rtc_tencent:
                LelinkHelper.getInstance().setRTCProtocol(IAPI.RTC_PROTOCOL_TENCENT);
                break;
            case R.id.rb_rtc_zego:
                LelinkHelper.getInstance().setRTCProtocol(IAPI.RTC_PROTOCOL_ZEGO);
                break;
            case R.id.rb_rtc_ne:
                LelinkHelper.getInstance().setRTCProtocol(IAPI.RTC_PROTOCOL_NE);
                break;
        }
    }
}
