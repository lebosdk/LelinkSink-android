package com.hpplay.sdk.sink.test.dmp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

import com.hpplay.sdk.sink.test.R;

/**
 * Created by Jrh on 2018/3/15.
 */

public class DMPActivity extends FragmentActivity {
    private DMPFragment mDMPFragment = new DMPFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dmp);
        initView();
    }

    protected void initView() {
        getFragmentManager().beginTransaction()
                .replace(R.id.dmp_content_fl, mDMPFragment)
                .commitAllowingStateLoss();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mDMPFragment.handleTopInfoEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}
