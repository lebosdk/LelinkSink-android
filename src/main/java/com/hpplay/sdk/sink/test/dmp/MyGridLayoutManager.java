package com.hpplay.sdk.sink.test.dmp;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

/**
 * Created by Jrh on 2018/3/15.
 */

public class MyGridLayoutManager extends GridLayoutManager {

    public MyGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    @Override
    public View onInterceptFocusSearch(View focused, int direction) {

        int pos = getPosition(focused);
        int count = getItemCount();
        int orientation = getOrientation();
        if (direction == View.FOCUS_RIGHT) {
            View view = getChildAt(getChildCount() - 1);
            if (view == focused) {
                return focused;
            }
        } else if (direction == View.FOCUS_LEFT) {
            View view = getChildAt(0);
            if (view == focused) {
                return focused;
            }
        }
        return super.onInterceptFocusSearch(focused, direction);
    }
}
