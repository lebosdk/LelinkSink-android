package com.hpplay.sdk.sink.test;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by tcc on 2017/12/26.
 */

public class QRFragment extends Fragment {

    private final String TAG = "QRFragment";

    private RelativeLayout mRootLayout;
    private ImageView mQRView;
    private Bitmap mQRBitmap;
    private int screenWidth;
    private String mQRStr;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootLayout = new RelativeLayout(getActivity());
        mRootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRootLayout.setBackgroundColor(Color.WHITE);
        mRootLayout.setClickable(true);
        return mRootLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mQRView = new ImageView(getActivity());
        mQRView.setBackgroundColor(Color.RED);
        mQRView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        screenWidth = Utils.getScreenWidth(getActivity());
        RelativeLayout.LayoutParams qrParameter = new RelativeLayout.LayoutParams(
                screenWidth / 3, screenWidth / 3);
        qrParameter.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRootLayout.addView(mQRView, qrParameter);

    }

    @Override
    public void onResume() {
        super.onResume();

        if (!TextUtils.isEmpty(mQRStr)) {
            createQR();
        }

    }

    public void updateQR(String qrStr) {

        this.mQRStr = qrStr;
        Logger.i(TAG, "updateQR qrString = " + mQRStr);

        if (mQRView != null) {
            createQR();
        }

    }

    private void createQR() {

        if (mQRBitmap != null) {
            mQRBitmap = null;
        }

        if (getActivity() != null) {
            Logger.i(TAG, "creatQR qrString = " + mQRStr);
            mQRBitmap = Utils.createQRCode(mQRStr, 200, 0);
            if (mQRView != null) {
                mQRView.setImageBitmap(mQRBitmap);
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mQRBitmap != null) {
            mQRBitmap.recycle();
            mQRBitmap = null;
//            System.gc();
        }
    }

    public void setQRStr(String mQRStr) {
        this.mQRStr = mQRStr;
    }

}
