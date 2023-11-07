package com.hpplay.sdk.sink.test.dialog;

import android.app.Activity;
import android.view.WindowManager;
import android.widget.Toast;

import com.hpplay.sdk.sink.test.DemoApplication;
import com.hpplay.sdk.sink.test.Logger;

/**
 * Created by tcc on 2018/11/14.
 */
public class LeboDialog {
    private final String TAG = "LeboDialog";
    private CountdownDialog mPwdDialog;

    public LeboDialog() {

    }

    public void show(String content, int duration) {
        if (mPwdDialog != null && mPwdDialog.isShowing()) {
            mPwdDialog.dismiss();
        }

        DemoApplication demoApplication = DemoApplication.getApplication();
        try {
            Activity currentActivity = demoApplication.getCurrentActivity();
            if (currentActivity != null) {
                mPwdDialog = new CountdownDialog(currentActivity);
                mPwdDialog.setContent(content);
                mPwdDialog.show(duration);
            } else {
                // 系统弹窗需要权限 <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
                mPwdDialog = new CountdownDialog(demoApplication);
                //设置弹出全局对话框
                mPwdDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                mPwdDialog.setContent(content);
                mPwdDialog.show(duration);
            }
        } catch (Exception e) {
            Logger.w(TAG, e);
            Toast.makeText(demoApplication, content, Toast.LENGTH_LONG).show();
        }
    }

    public void dismiss() {
        if (mPwdDialog != null && mPwdDialog.isShowing()) {
            mPwdDialog.dismiss();
        }
    }

}
