package com.hpplay.sdk.sink.test;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hpplay.sdk.sink.api.IAPI;
import com.hpplay.sdk.sink.api.IUploadLogCallback;
import com.hpplay.sdk.sink.api.ServerInfo;
import com.hpplay.sdk.sink.api.UploadLogBean;
import com.hpplay.sdk.sink.api.UploadLogResult;


/**
 * author : tcc
 * date   : 2019/12/304:08 PM
 * desc   :
 */
public class LogFragment extends Fragment {
    private final String TAG = "LogFragment";

    private Button mUploadLogBtn;
    private TextView mBrowseLogTxt;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return View.inflate(getActivity(), R.layout.fragment_log, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUploadLogBtn = getView().findViewById(R.id.upload_log_btn);
        mUploadLogBtn.requestFocus();
        mBrowseLogTxt = getView().findViewById(R.id.browse_log_txt);
        initData();
    }

    private void initData() {
        mUploadLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadLogBean uploadLogBean = new UploadLogBean();
                uploadLogBean.errorCode = "sink demo";
                uploadLogBean.isDrive = true;
                uploadLogBean.uploadLogCallback = new IUploadLogCallback() {
                    @Override
                    public void uploadLogStatus(UploadLogResult uploadLogResult) {
                        if (uploadLogResult == null) {
                            Logger.w(TAG, "demo upload log fail");
                            return;
                        }
                        Logger.i(TAG, "initData,uploadLogResult " + uploadLogResult.code);
                    }
                };
                LelinkHelper.getInstance().uploadLog(uploadLogBean);
            }
        });
        ServerInfo serverInfo = LelinkHelper.getInstance().getServerInfo();
        String port = "";
        if (serverInfo == null) {
            Logger.w(TAG, "initData,get serverInfo is null");
        } else {
            port = String.valueOf(serverInfo.remotePort);
        }
        mBrowseLogTxt.setText(browserGetLogFile(Utils.getIP(getActivity()), "" + port));
    }

    private String browserGetLogFile(String serverIp, String port) {
        if (TextUtils.isEmpty(serverIp) || TextUtils.isEmpty(port)) {
            return null;
        }
        String result = serverIp + ":" + port + "/log";
        Logger.i(TAG, "browserGetLogFile,log url: " + "http://" + result);
        return "请在本机局域网浏览器中输入: " + result + " 获取日志文件";
    }

}
