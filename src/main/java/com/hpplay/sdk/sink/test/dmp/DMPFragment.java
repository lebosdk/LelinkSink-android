package com.hpplay.sdk.sink.test.dmp;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.hpplay.sdk.sink.dmp.BaseDMPBean;
import com.hpplay.sdk.sink.dmp.DeviceBean;
import com.hpplay.sdk.sink.dmp.FolderBean;
import com.hpplay.sdk.sink.dmp.OnDMPListener;
import com.hpplay.sdk.sink.test.LelinkHelper;
import com.hpplay.sdk.sink.test.R;
import com.hpplay.sdk.sink.test.dmp.RecyclerAdapter.OnItemClickLitener;
import com.hpplay.sdk.sink.test.dmp.RecyclerAdapter.OnItemSelectedLitener;

import java.util.ArrayList;
import java.util.List;

import static com.hpplay.sdk.sink.test.dmp.RecyclerAdapter.MIMETYPE_AUDIO;
import static com.hpplay.sdk.sink.test.dmp.RecyclerAdapter.MIMETYPE_FOLDER;
import static com.hpplay.sdk.sink.test.dmp.RecyclerAdapter.MIMETYPE_PHOTO;
import static com.hpplay.sdk.sink.test.dmp.RecyclerAdapter.MIMETYPE_VIDEO;

/**
 * Created by Jrh on 2018/3/14.
 */

public class DMPFragment extends Fragment implements OnDMPListener, View.OnClickListener {
    private final String TAG = "DMPFragment";
    private View mBrowseLayout;
    private RecyclerView mDeviceListView;
    private RecyclerView mFileListView;
    private RecyclerAdapter mDeviceAdapter;
    private RecyclerAdapter mFileAdapter;
    private List<BaseDMPBean> mDeviceData = new ArrayList<>();
    private List<BaseDMPBean> mCurFileData = new ArrayList<>();
    private List<List<BaseDMPBean>> mFileDatas = new ArrayList<>();
    private List<Integer> mPositons = new ArrayList<>();
    private DeviceBean mCurDeviceBean;
    private int mPositon = 1;
    private String mFolderId;
    private int mWidthPixels;
    private View mVideoPalyRl;
    private VideoView mDMPPlayer;
    private MediaController mMediaController;
    private ViewPager mDMPPPhotolayer;
    private DMPPhotoAdapter mPhotoAdapter;
    private View mRefreshIv;
    private View mRefreshProgress;
    private View mMusicIcon;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = View.inflate(getActivity(), setLayoutId(), null);
        return rootView;
    }

    public int setLayoutId() {
        return R.layout.fragment_dmp;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initDeviceData();
    }

    private void initView() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mWidthPixels = displayMetrics.widthPixels;
        mBrowseLayout = getView().findViewById(R.id.browse_layout_ll);
        getView().findViewById(R.id.dmp_back).setOnClickListener(this);
        getView().findViewById(R.id.dmp_refresh).setOnClickListener(this);
        mVideoPalyRl = getView().findViewById(R.id.video_paly_rl);
        mDMPPlayer = (VideoView) getView().findViewById(R.id.video_play_vv);
        mMediaController = new MediaController(getActivity());
        mMediaController.setAnchorView(mDMPPlayer);
        mMediaController.setKeepScreenOn(true);
        mDMPPlayer.setMediaController(mMediaController);
        mDMPPPhotolayer = (ViewPager) getView().findViewById(R.id.photo_play_vp);
        mRefreshIv = getView().findViewById(R.id.refresh_iv);
        mRefreshProgress = getView().findViewById(R.id.refresh_progress);
        mMusicIcon = getView().findViewById(R.id.music_icon);

        initDeviceListView();
        initFileListView();
    }

    private void initDeviceListView() {
        mDeviceListView = (RecyclerView) getView().findViewById(R.id.device_list_rc);
        mDeviceListView.requestFocus();
        CardItemDecoration decortation = new CardItemDecoration();
        int decoration = dip2px(15);
        decortation.setRect(decoration, decoration, decoration, decoration);
        mDeviceListView.addItemDecoration(decortation);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 4);
        mDeviceListView.setLayoutManager(manager);
        mDeviceListView.setItemAnimator(new DefaultItemAnimator());
        mDeviceAdapter = new RecyclerAdapter(getActivity(), mDeviceData);
        int hight = (mWidthPixels - (dip2px(74f * 2) + 30 * 4 * 2)) / 4;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(hight, hight);
        mDeviceAdapter.setItemLayoutParams(params);
        mDeviceAdapter.setOnItemClickLitener(mDeviceOnItemClickLitener);
        mDeviceListView.setAdapter(mDeviceAdapter);
    }

    private void initFileListView() {
        mFileListView = (RecyclerView) getView().findViewById(R.id.file_list_rc);
        CardItemDecoration decortation = new CardItemDecoration();
        int decoration = dip2px(15);
        decortation.setRect(decoration, decoration, decoration, decoration);
        mFileListView.addItemDecoration(decortation);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 6);

        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                FolderBean bean = (FolderBean) (mCurFileData.get(position));
                if (bean.mimeType < -2) {
                    return 6;
                } else {
                    return 1;
                }
            }
        });

        mFileListView.setLayoutManager(manager);
        mFileListView.setItemAnimator(new DefaultItemAnimator());
        mFileAdapter = new RecyclerAdapter(getActivity(), mCurFileData);
        int hight = (mWidthPixels - (dip2px(74f * 2) + decoration * 6 * 2)) / 6;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(hight, hight);
        mFileAdapter.setItemLayoutParams(params);
        mFileAdapter.setOnItemClickLitener(mFileOnItemClickLitener);
        mFileAdapter.setOnItemSelectedLitener(mOnItemSelectedLitener);
        mFileListView.setAdapter(mFileAdapter);

    }

    private void initDeviceData() {
        LelinkHelper.getInstance().setDMPListener(this);
        LelinkHelper.getInstance().startDMP();
    }

    private OnItemClickLitener mDeviceOnItemClickLitener = new OnItemClickLitener() {
        @Override
        public void onItemClick(View view, int position) {
            if (position >= mDeviceData.size()) {
                return;
            }
            mDeviceListView.setVisibility(View.GONE);
            mFileListView.setVisibility(View.VISIBLE);
            mCurDeviceBean = (DeviceBean) mDeviceData.get(position);
            LelinkHelper.getInstance().browseDevice(mCurDeviceBean);
        }
    };

    private OnItemSelectedLitener mOnItemSelectedLitener = new OnItemSelectedLitener() {
        public void onItemSelected(View view, int pos) {
            if (pos < 7) {
                mFileListView.scrollToPosition(0);
            }
        }
    };

    private OnItemClickLitener mFileOnItemClickLitener = new OnItemClickLitener() {
        @Override
        public void onItemClick(View view, int position) {
            if (position >= mCurFileData.size()) {
                return;
            }
            mPositon = position;
            FolderBean folderBean = (FolderBean) (mCurFileData.get(position));
            switch (folderBean.mimeType) {
                case MIMETYPE_AUDIO:
                    playAudio(folderBean.resourceUrl);
                    break;
                case MIMETYPE_VIDEO:
                    playVideo(folderBean.resourceUrl);
                    break;
                case MIMETYPE_PHOTO:
                    playPhoto();
                    break;
                case MIMETYPE_FOLDER:
                    // mCurFileData.clear();
                    // mFileAdapter.notifyDataSetChanged();
                    LelinkHelper.getInstance().browseFolder(mCurDeviceBean.actionUrl, folderBean.id);
                    break;
            }
        }
    };

    private void playAudio(String url) {
        play(url);
        mMusicIcon.setVisibility(View.VISIBLE);
    }

    private void playVideo(String url) {
        play(url);
        mMusicIcon.setVisibility(View.GONE);
    }

    private void play(String url) {
        mBrowseLayout.setVisibility(View.GONE);
        mVideoPalyRl.setVisibility(View.VISIBLE);
        mDMPPlayer.setVisibility(View.VISIBLE);
        mDMPPPhotolayer.setVisibility(View.GONE);
        mDMPPlayer.stopPlayback();
        mDMPPlayer.setVideoPath(Uri.parse(url).toString());
        mDMPPlayer.start();
        mMediaController.requestFocus();
    }

    private void playPhoto() {
        mBrowseLayout.setVisibility(View.GONE);
        mVideoPalyRl.setVisibility(View.VISIBLE);
        mDMPPPhotolayer.setVisibility(View.VISIBLE);
        mDMPPlayer.setVisibility(View.GONE);
        mDMPPPhotolayer.setFocusable(true);
        mDMPPPhotolayer.setFocusableInTouchMode(true);
        mPhotoAdapter = new DMPPhotoAdapter(getActivity(), getPhotoBean());
        mDMPPPhotolayer.setAdapter(mPhotoAdapter);
    }

    private List<BaseDMPBean> getPhotoBean() {
        List<BaseDMPBean> photoData = new ArrayList<>();
        for (BaseDMPBean fileData : mCurFileData) {
            if (((FolderBean) fileData).mimeType == MIMETYPE_PHOTO) {
                photoData.add(fileData);
            }
        }
        return photoData;
    }

    private void stopVideo() {
        if (mDMPPlayer != null) {
            mDMPPlayer.stopPlayback();
        }
        mVideoPalyRl.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dmp_back:
                back();
                break;
            case R.id.dmp_refresh:
                refresh();
                break;
        }
    }

    private void refresh() {
        mRefreshIv.setVisibility(View.GONE);
        mRefreshProgress.setVisibility(View.VISIBLE);
        if (mFileListView.isShown()) {
            if (mCurFileData.size() > 1) {
                FolderBean folderBean = (FolderBean) (mCurFileData.get(1));
                mFolderId = folderBean.parentId;
            }
            if (!TextUtils.isEmpty(mFolderId)) {
                mCurFileData.clear();
                if (mFileDatas.size() > 0) {
                    mFileDatas.remove(mFileDatas.size() - 1);
                    mPositon = mPositons.get(mPositons.size() - 1);
                    mPositons.remove(mPositons.size() - 1);
                }
                mFileAdapter.notifyDataSetChanged();
                LelinkHelper.getInstance().browseFolder(mCurDeviceBean.actionUrl, mFolderId);
            }
        } else {
            LelinkHelper.getInstance().searchDMP();
        }
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshProgress.setVisibility(View.GONE);
                mRefreshIv.setVisibility(View.VISIBLE);
            }
        }, 10 * 1000);
    }

    @Override
    public void onAddDevice(DeviceBean deviceBean) {
        for (BaseDMPBean device : mDeviceData) {
            if (device.name.equals(deviceBean.name)) {
                return;
            }
        }
        DeviceBean device = (DeviceBean) deviceBean.clone();
        mDeviceData.add(device);
        getView().post(new Runnable() {
            @Override
            public void run() {
                mDeviceAdapter.notifyDataSetChanged();
                if (mDeviceListView.isShown()) {
                    setSelection(mDeviceListView, 0);
                    mRefreshProgress.setVisibility(View.GONE);
                    mRefreshIv.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setSelection(final RecyclerView recyclerView, final int pos) {
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                View view = recyclerView.getChildAt(pos);
                if (view != null) {
                    view.requestFocus();
                }
            }
        }, 200);
    }

    @Override
    public void onRemoveDevice(String s) {
    }

    @Override
    public void onAddFolders(final FolderBean[] folderBeen) {
        getView().post(new Runnable() {
            @Override
            public void run() {
                List<BaseDMPBean> folderList = getMimeTypeList("文件夹", -MIMETYPE_FOLDER);
                List<BaseDMPBean> audioList = getMimeTypeList("音乐", -MIMETYPE_AUDIO);
                List<BaseDMPBean> videoList = getMimeTypeList("视频", -MIMETYPE_VIDEO);
                List<BaseDMPBean> photoList = getMimeTypeList("图片", -MIMETYPE_PHOTO);
                for (FolderBean folder : folderBeen) {
                    if (folder.childCount < 0) {
                        switch (folder.mimeType) {
                            case MIMETYPE_AUDIO:
                                audioList.add(folder);
                                break;
                            case MIMETYPE_VIDEO:
                                videoList.add(folder);
                                break;
                            case MIMETYPE_PHOTO:
                                photoList.add(folder);
                                break;
                        }
                    } else {
                        folder.mimeType = MIMETYPE_FOLDER;
                        folderList.add(folder);
                    }
                }
                List<BaseDMPBean> fileData = new ArrayList<>();
                addAll(fileData, folderList);
                addAll(fileData, audioList);
                addAll(fileData, videoList);
                addAll(fileData, photoList);
                mFileDatas.add(fileData);
                mPositons.add(mPositon);
                mPositon = 1;
                refreshFolderList();
                mRefreshProgress.setVisibility(View.GONE);
                mRefreshIv.setVisibility(View.VISIBLE);
            }
        });
    }

    private void refreshFolderList() {
        if (mFileDatas.size() > 0) {
            mCurFileData.clear();
            mCurFileData.addAll(mFileDatas.get(mFileDatas.size() - 1));
            mFileAdapter.notifyDataSetChanged();
            setSelection(mFileListView, mPositon);
        }
    }

    private void addAll(List<BaseDMPBean> fileData, List<BaseDMPBean> data) {
        if (data.size() > 1) {
            fileData.addAll(data);
        }
    }

    private List<BaseDMPBean> getMimeTypeList(String name, int mimeType) {
        List<BaseDMPBean> bean = new ArrayList<>();
        FolderBean folderBean = new FolderBean();
        folderBean.name = name;
        folderBean.mimeType = mimeType;
        bean.add(folderBean);
        return bean;
    }

    public boolean handleTopInfoEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (mVideoPalyRl.isShown()) {
                    mBrowseLayout.setVisibility(View.VISIBLE);
                    stopVideo();
                    setSelection(mFileListView, mPositon);
                    return true;
                } else if (mFileListView.isShown()) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        return false;
                    }
                    if (mFileDatas.size() > 1) {
                        mFileDatas.remove(mFileDatas.size() - 1);
                        mPositon = mPositons.get(mPositons.size() - 1);
                        mPositons.remove(mPositons.size() - 1);
                        refreshFolderList();
                    } else {
                        mFileDatas.clear();
                        mDeviceListView.setVisibility(View.VISIBLE);
                        mFileListView.setVisibility(View.GONE);
                        setSelection(mDeviceListView, 0);
                    }
                    return true;
                }
                break;
        }
        return false;
    }

    private boolean back() {
        if (mFileListView.isShown()) {
            if (mFileDatas.size() > 1) {
                mFileDatas.remove(mFileDatas.size() - 1);
                mPositon = mPositons.get(mPositons.size() - 1);
                mPositons.remove(mPositons.size() - 1);
                refreshFolderList();
            } else {
                mFileDatas.clear();
                mDeviceListView.setVisibility(View.VISIBLE);
                mFileListView.setVisibility(View.GONE);
                setSelection(mDeviceListView, 0);
            }
            return true;
        } else {
            getActivity().finish();
        }
        return false;
    }

    public int dip2px(float dipValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    public void onPause() {
        stopVideo();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        LelinkHelper.getInstance().stopDMP();
        super.onDestroy();
    }
}
