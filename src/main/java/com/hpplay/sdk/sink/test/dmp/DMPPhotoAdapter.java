package com.hpplay.sdk.sink.test.dmp;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.hpplay.sdk.sink.dmp.BaseDMPBean;
import com.hpplay.sdk.sink.dmp.FolderBean;

import java.util.List;

/**
 * Created by hpplay on 2018/3/7.
 */

public class DMPPhotoAdapter extends PagerAdapter {
    private final String TAG = "DMPPhotoAdapter";
    private Context mContext;
    private List<BaseDMPBean> mPhotoList;

    public DMPPhotoAdapter(Context context, List<BaseDMPBean> photoList) {
        mContext = context;
        mPhotoList = photoList;
    }

    @Override
    public int getCount() {

        return mPhotoList == null ? 0 : mPhotoList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        PhotoView photoView = (PhotoView) object;
        container.removeView(photoView);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(mContext);
        photoView.showNetPhoto(((FolderBean) mPhotoList.get(position)).resourceUrl);
        container.addView(photoView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return photoView;
    }
}
