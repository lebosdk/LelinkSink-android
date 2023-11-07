package com.hpplay.sdk.sink.test.dmp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.hpplay.sdk.sink.dmp.BaseDMPBean;
import com.hpplay.sdk.sink.dmp.DeviceBean;
import com.hpplay.sdk.sink.dmp.FolderBean;
import com.hpplay.sdk.sink.test.R;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements ItemView.OnItemSelectedListener {
    public static final int MIMETYPE_UNKNOWN = 0;
    public static final int MIMETYPE_AUDIO = 101;
    public static final int MIMETYPE_VIDEO = 102;
    public static final int MIMETYPE_PHOTO = 103;
    public static final int MIMETYPE_FOLDER = 105;
    private Context mContext;
    private List<BaseDMPBean> mData;
    private OnItemClickLitener mItemClickLitener;
    private OnItemSelectedLitener mOnItemSelectedLitener;
    private View mSelectedView;
    private LayoutParams mItemParams;

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    public interface OnItemSelectedLitener {
        void onItemSelected(View view, int pos);
    }

    public RecyclerAdapter(Context context, List<BaseDMPBean> data) {
        mContext = context;
        mData = data;
    }

    public void setOnItemClickLitener(OnItemClickLitener itemClickLitener) {
        mItemClickLitener = itemClickLitener;
    }

    public void setOnItemSelectedLitener(OnItemSelectedLitener onItemSelectedLitener) {
        mOnItemSelectedLitener = onItemSelectedLitener;
    }

    public void setItemLayoutParams(LayoutParams itemParams) {
        mItemParams = itemParams;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        ItemView itemView = new ItemView(mContext);
        itemView.setOnItemSelectedLitener(this);
        itemView.setTag(position);
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ItemView itemView;
        public View layout;
        public ImageView icon;
        public MarqueTextView name;
        public TextView fileType;

        public ViewHolder(ItemView item) {
            super(item);
            itemView = item;
            layout = itemView.findViewById(R.id.item_layout);
            icon = (ImageView) itemView.findViewById(R.id.device_im);
            name = (MarqueTextView) itemView.findViewById(R.id.name_tv);
            fileType = (TextView) itemView.findViewById(R.id.file_type_tv);

            if (mItemParams != null) {
                itemView.findViewById(R.id.item_layout).setLayoutParams(mItemParams);
            }
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.itemView.setTag(position);
        viewHolder.itemView.setBaseDMPBean(mData.get(position));
        if (mData.get(position) instanceof DeviceBean) {
            DeviceBean deviceBean = (DeviceBean) (mData.get(position));
            viewHolder.icon.setImageResource(R.drawable.storage_f);
            viewHolder.name.setText(deviceBean.name);
        } else {
            FolderBean folderBean = (FolderBean) (mData.get(position));
            switch (folderBean.mimeType) {
                case MIMETYPE_FOLDER:
                    refreshFile(viewHolder, R.drawable.file_f, folderBean);
                    break;
                case MIMETYPE_AUDIO:
                    refreshFile(viewHolder, R.drawable.music_f, folderBean);
                    break;
                case MIMETYPE_VIDEO:
                    refreshFile(viewHolder, R.drawable.video_f, folderBean);
                    break;
                case MIMETYPE_PHOTO:
                    refreshFile(viewHolder, R.drawable.picture_f, folderBean);
                    break;
                case -MIMETYPE_FOLDER:
                case -MIMETYPE_AUDIO:
                case -MIMETYPE_VIDEO:
                case -MIMETYPE_PHOTO:
                    refreshType(viewHolder, folderBean);
                    break;
                default:
                    refreshFile(viewHolder, R.drawable.file_f, folderBean);
                    break;
                case MIMETYPE_UNKNOWN:
                    break;
            }
        }
        if (mItemClickLitener != null) {
            viewHolder.itemView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mItemClickLitener.onItemClick(v, position);
                }
            });

        }
    }

    private void refreshFile(ViewHolder viewHolder, int drawableId, FolderBean folderBean) {
        viewHolder.icon.setImageResource(drawableId);
        viewHolder.fileType.setVisibility(View.GONE);
        viewHolder.layout.setVisibility(View.VISIBLE);
        viewHolder.name.setText(folderBean.name);
        viewHolder.itemView.setFocusable(true);
        viewHolder.itemView.setFocusableInTouchMode(true);
    }

    private void refreshType(ViewHolder viewHolder, FolderBean folderBean) {
        viewHolder.layout.setVisibility(View.GONE);
        viewHolder.fileType.setVisibility(View.VISIBLE);
        viewHolder.fileType.setText(folderBean.name);
        viewHolder.itemView.setFocusable(false);
        viewHolder.itemView.setFocusableInTouchMode(false);
    }

    @Override
    public void OnItemSelected(View view) {
        mSelectedView = view;
        if (mOnItemSelectedLitener != null) {
            mOnItemSelectedLitener.onItemSelected(view, (int) view.getTag());
        }
    }

    public void lastFcouseItemRequestFocus() {
        if (mSelectedView != null) {
            mSelectedView.requestFocus();
        }
    }
}
