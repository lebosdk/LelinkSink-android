package com.hpplay.sdk.sink.test.dmp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hpplay.sdk.sink.dmp.BaseDMPBean;
import com.hpplay.sdk.sink.dmp.FolderBean;
import com.hpplay.sdk.sink.test.R;

import static com.hpplay.sdk.sink.test.dmp.RecyclerAdapter.MIMETYPE_AUDIO;
import static com.hpplay.sdk.sink.test.dmp.RecyclerAdapter.MIMETYPE_FOLDER;
import static com.hpplay.sdk.sink.test.dmp.RecyclerAdapter.MIMETYPE_PHOTO;
import static com.hpplay.sdk.sink.test.dmp.RecyclerAdapter.MIMETYPE_VIDEO;

public class ItemView extends FrameLayout implements OnFocusChangeListener {
    private Paint mPaint;
    private OnItemSelectedListener mListener;
    private ImageView icon;
    public MarqueTextView name;
    private BaseDMPBean mBaseDMPBean;

    public interface OnItemSelectedListener {
        void OnItemSelected(View view);
    }

    public ItemView(Context context) {
        super(context);
        inflate(context, R.layout.item_view, this);
        icon = (ImageView) findViewById(R.id.device_im);
        name = (MarqueTextView) findViewById(R.id.name_tv);
        mPaint = new Paint();
        setFocusable(true);
        setOnFocusChangeListener(this);
    }

    public void setOnItemSelectedLitener(OnItemSelectedListener listener) {
        mListener = listener;
    }

    public void setBaseDMPBean(BaseDMPBean baseDMPBean) {
        mBaseDMPBean = baseDMPBean;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBorder(canvas);
    }

    private void drawBorder(Canvas canvas) {
        if (hasFocus()) {
            mPaint.reset();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setAntiAlias(true);
            RectF innerRect = new RectF();
            innerRect.left = 1.5f;
            innerRect.top = 1.5f;
            innerRect.right = getWidth() - 1.5f;
            innerRect.bottom = getHeight() - 1.5f;

            mPaint.setColor(Color.WHITE);
            mPaint.setStrokeWidth(3);
            canvas.drawRoundRect(innerRect, 13, 13, mPaint);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (mListener != null) {
                mListener.OnItemSelected(v);
            }
            scaleViewZoom();
            selected();
        } else {
            scaleViewDefault();
            unSelected();
        }
        invalidate();
    }

    private void selected() {
        name.setAutoMarque(true);
        if (mBaseDMPBean instanceof FolderBean) {
            FolderBean folderBean = (FolderBean) mBaseDMPBean;
            switch (folderBean.mimeType) {
                case MIMETYPE_FOLDER:
                    icon.setImageResource(R.drawable.file_n);
                    break;
                case MIMETYPE_AUDIO:
                    icon.setImageResource(R.drawable.music_n);
                    break;
                case MIMETYPE_VIDEO:
                    icon.setImageResource(R.drawable.video_n);
                    break;
                case MIMETYPE_PHOTO:
                    icon.setImageResource(R.drawable.picture_n);
                    break;
            }
        }
    }

    private void unSelected() {
        name.setAutoMarque(false);
        if (mBaseDMPBean instanceof FolderBean) {
            FolderBean folderBean = (FolderBean) mBaseDMPBean;
            switch (folderBean.mimeType) {
                case MIMETYPE_FOLDER:
                    icon.setImageResource(R.drawable.file_f);
                    break;
                case MIMETYPE_AUDIO:
                    icon.setImageResource(R.drawable.music_f);
                    break;
                case MIMETYPE_VIDEO:
                    icon.setImageResource(R.drawable.video_f);
                    break;
                case MIMETYPE_PHOTO:
                    icon.setImageResource(R.drawable.picture_f);
                    break;
            }
        }
    }

    private void scaleViewZoom() {
        scaleView(this, 1.08f, 300);
    }

    private void scaleViewDefault() {
        scaleView(this, 1.0f, 300);
    }

    private void scaleView(View view, float x, long time) {
        if (view != null) {
            ObjectAnimator animx = ObjectAnimator.ofFloat(view, "scaleX", x);
            ObjectAnimator animy = ObjectAnimator.ofFloat(view, "scaleY", x);
            AnimatorSet set = new AnimatorSet();
            set.setDuration(time);
            set.play(animx).with(animy);
            set.start();
        }
    }
}
