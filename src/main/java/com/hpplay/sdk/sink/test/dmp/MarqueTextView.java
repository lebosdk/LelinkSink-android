package com.hpplay.sdk.sink.test.dmp;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by DON on 2017/8/19.
 */

public class MarqueTextView extends TextView {
    private boolean mIsAutoMarque = false;

    public MarqueTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MarqueTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueTextView(Context context) {
        super(context);
    }

    public void setAutoMarque(boolean isAutoMarque) {
        mIsAutoMarque = isAutoMarque;
        onFocusChanged(isAutoMarque, View.FOCUS_BACKWARD, new Rect());
    }

    @Override
    public boolean isFocused() {
        return mIsAutoMarque;
    }
}
