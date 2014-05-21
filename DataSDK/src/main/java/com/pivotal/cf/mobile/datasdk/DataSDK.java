package com.pivotal.cf.mobile.datasdk;

import android.app.Activity;

import com.pivotal.cf.mobile.datasdk.authorization.AuthorizationEngine;

public class DataSDK {

    private static DataSDK instance;

    public synchronized static DataSDK getInstance() {
        if (instance == null) {
            instance = new DataSDK();
        }
        return instance;
    }

    private DataSDK() {

    }

    public void obtainAuthorization(Activity activity, DataParameters parameters) {
        // TODO - AuthorizationEngine should be launched on a worker thread.
        final AuthorizationEngine engine = new AuthorizationEngine();
        engine.obtainAuthorization(activity, parameters);
    }
}
