package com.hpplay.sdk.sink.test.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tcc on 2018/5/28.
 */

public class CastInfoAdapter extends BaseAdapter {
    private final String TAG = "CastInfoAdapter";
    private Context mContext;
    private List<String> mDataList;

    public CastInfoAdapter(Context context, List<String> dataList) {
        mContext = context;
        mDataList = dataList;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LinearLayout linearLayout = new LinearLayout(mContext);
            linearLayout.setLayoutParams(new ListView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            convertView = linearLayout;

            TextView textView = new TextView(mContext);
            textView.setPadding(20, 20, 20, 20);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            textView.setTextColor(Color.BLACK);
            linearLayout.addView(textView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            viewHolder = new ViewHolder();
            viewHolder.mTextView = textView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mTextView.setText((getCount() - position) + ": " + mDataList.get(position));
        return convertView;
    }

    static class ViewHolder {

        public TextView mTextView;

    }
}
