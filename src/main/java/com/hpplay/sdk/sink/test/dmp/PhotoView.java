package com.hpplay.sdk.sink.test.dmp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hpplay.sdk.sink.test.R;

/**
 * Created by lenovo on 2018/1/5.
 */

public class PhotoView extends FrameLayout {
    private final String TAG = "PhotoView";

    private Context mContext;
    private ImageView mImageView;

    public PhotoView(Context context) {
        super(context);
        initView(context);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        mImageView = new ImageView(context);
        mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mImageView, params);
    }

    public void showNetPhoto(final String photoUrl) {
        Log.i(TAG, "showNetPhoto");
        Glide.with(mContext)
                .load(photoUrl)
                .placeholder(R.drawable.load_default)
                .error(R.drawable.load_default)
                .into(mImageView);
    }
}
