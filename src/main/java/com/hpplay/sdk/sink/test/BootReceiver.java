package com.hpplay.sdk.sink.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by tcc on 2017/12/19.
 */

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i(TAG, "onReceive " + intent.getAction());

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // SDK内部使用bindService方式启动，所以不能在BroadcastReceiver中直接启动服务
            Intent serviceIntent = new Intent(context, CastService.class);
            context.startService(serviceIntent);
        }
    }
}
