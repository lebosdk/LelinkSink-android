package com.hpplay.sdk.sink.test.dmp;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;

public class CardItemDecoration extends RecyclerView.ItemDecoration {
    private int left, top, right, bottom;

    public void setRect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        outRect.set(left, top, right, bottom);
    }

}
