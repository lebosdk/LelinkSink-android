package com.hpplay.sdk.sink.test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by tcc on 2018/6/8.
 */

public class CastService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        LelinkHelper.getInstance().startServer(Utils.createDeviceName(this) + "-1");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
