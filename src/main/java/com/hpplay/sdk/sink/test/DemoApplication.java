package com.hpplay.sdk.sink.test;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by hpplay on 2018/5/6.
 */

public class DemoApplication extends Application {
    private static final String TAG = "DemoApplication";

    private static DemoApplication instance = null;
    private Activity mCurrentActivity = null;
    ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            Logger.i(TAG, "onActivityResumed " + activity.getClass().getName());
            setCurrentActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Logger.i(TAG, "onActivityPaused " + activity.getClass().getName());
            setCurrentActivity(null);
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        instance = DemoApplication.this;
        registerActivityLifecycleCallbacks(lifecycleCallbacks);
    }

    public static DemoApplication getApplication() {
        return instance;
    }

    public void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }
}
