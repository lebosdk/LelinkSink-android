package com.hpplay.sdk.sink.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkReceiver";

    private static final String CONNECTIVITY_CHANGE = ConnectivityManager.CONNECTIVITY_ACTION;
    private static final String WIFI_AP_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";

    public NetworkReceiver(Context context) {
        Logger.i(TAG, "NetworkReceiver context: " + context.getApplicationContext());
    }

    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CONNECTIVITY_CHANGE);
        filter.addAction(WIFI_AP_STATE_CHANGED);
        return filter;
    }

    private IUIUpdateListener mUI;

    public void setUIUpdate(IUIUpdateListener l) {
        mUI = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logger.i(TAG, "onReceive action: " + action);
        if (mUI != null) {
            // mUI.onUpdateNetwork();
        }
    }
}
