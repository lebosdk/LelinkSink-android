package com.hpplay.sdk.sink.test.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hpplay.sdk.sink.test.Logger;

/**
 * Created by hpplay on 2018/5/6.
 */

public class CountdownDialog extends Dialog {
    private final String TAG = "CountdownDialog";
    private Context mContext;
    private TextView mContentTxt;
    private Handler mHandler = new Handler();
    private Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mContext != null && isShowing()) {
                dismiss();
            }
        }
    };

    public CountdownDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        init();
    }

    public CountdownDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mContext = context;
        init();
    }

    public CountdownDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
        init();
    }

    private void init() {
        LinearLayout rootLayout = new LinearLayout(mContext);
        int paddingLeft = 200;
        int paddingTop = 10;
        rootLayout.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);
        rootLayout.setGravity(Gravity.CENTER);
        rootLayout.setBackgroundColor(Color.WHITE);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView textView = new TextView(mContext);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        rootLayout.addView(textView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mContentTxt = textView;

        setContentView(rootLayout);
    }

    public void setContent(String content) {
        mContentTxt.setText(content);
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
        try {
            super.dismiss();
            mHandler.removeCallbacks(mTimeOutRunnable);
        } catch (Exception e) {
            Logger.w(TAG, e);
        }
    }
}
