package com.hpplay.sdk.sink.test.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hpplay.sdk.sink.api.PreemptCastInfo;
import com.hpplay.sdk.sink.test.LelinkHelper;
import com.hpplay.sdk.sink.test.Logger;

public class PreemptDialog extends Dialog {
    private final String TAG = "PreemptDialog";
    private Context mContext;
    private Window mWindow;
    private RelativeLayout mRootView;
    private Handler mHandler = new Handler();
    private PreemptCastInfo mPreemptCastInfo;
    private Button allowOnceBtn;
    private Button whiteBtn;
    private Button rejectBtn;
    private Button blackBtn;
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == allowOnceBtn) {
                mPreemptCastInfo.option = PreemptCastInfo.OPTION_ALLOW_ONCE;
            } else if(v == whiteBtn){
                mPreemptCastInfo.option = PreemptCastInfo.OPTION_WHITE;
            } else if(v == rejectBtn){
                mPreemptCastInfo.option = PreemptCastInfo.OPTION_REJECT;
            } else if(v == blackBtn){
                mPreemptCastInfo.option = PreemptCastInfo.OPTION_BLACK;
            }
            LelinkHelper.getInstance().userPreemptOption(mPreemptCastInfo);
            dismiss();
        }
    };


    private Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mContext != null && isShowing()) {
                mPreemptCastInfo.option = PreemptCastInfo.OPTION_TIMEOUT;
                LelinkHelper.getInstance().userPreemptOption(mPreemptCastInfo);
                dismiss();
            }
        }
    };


    public PreemptDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }


    public PreemptDialog(Context context, PreemptCastInfo info) {
        super(context);
        Logger.i(TAG, "PreemptDialog info: " + info);
        mContext = context;
        mPreemptCastInfo = info;
        init();
    }

    private void init() {
        mRootView = new RelativeLayout(mContext);
        mRootView.setGravity(Gravity.BOTTOM);
        mRootView.setBackgroundColor(Color.TRANSPARENT);
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mWindow = getWindow();
        if (mWindow != null) {
            mWindow.requestFeature(Window.FEATURE_NO_TITLE);
            //背景全透明
            mWindow.setDimAmount(0f);
            mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        setContentView(mRootView);
    }


    public void show() {
        Logger.i(TAG, "show Dialog");
        if (isShowing()) {
            dismiss();
        }
        super.show();
        LinearLayout rootPanel = new LinearLayout(mContext);
        rootPanel.setBackgroundColor(Color.TRANSPARENT);
        rootPanel.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams panelParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        panelParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mRootView.addView(rootPanel, panelParams);

        allowOnceBtn = new Button(mContext);
        allowOnceBtn.setText("单次允许");
        allowOnceBtn.setOnClickListener(mClickListener);
        rootPanel.addView(allowOnceBtn);

        whiteBtn = new Button(mContext);
        whiteBtn.setText("加入白名单");
        whiteBtn.setOnClickListener(mClickListener);
        rootPanel.addView(whiteBtn);

        rejectBtn = new Button(mContext);
        rejectBtn.setText("单次拒绝");
        rejectBtn.setOnClickListener(mClickListener);
        rootPanel.addView(rejectBtn);

        blackBtn = new Button(mContext);
        blackBtn.setText("加入黑名单");
        blackBtn.setOnClickListener(mClickListener);
        rootPanel.addView(blackBtn);
    }

    /**
     * @param timeOut 超时时间，单位毫秒
     */
    public void show(int timeOut) {
        show();
        mHandler.postDelayed(mTimeOutRunnable, timeOut);
    }

    @Override
    public void dismiss() {
        Logger.i(TAG, "dismiss Dialog");
        try {
            super.dismiss();
            mHandler.removeCallbacks(mTimeOutRunnable);
        } catch (Exception e) {
            Logger.w(TAG, e);
        }
    }
}
