package com.hpplay.sdk.sink.test;

/**
 * Created by tcc on 2017/12/19.
 */

public interface IUIUpdateListener {

    void onUpdateText(String msg);

    void onUpdateNetwork();

    void onServerStart();
}
