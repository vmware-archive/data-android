package com.pivotal.cf.mobile.datasdk;

import com.pivotal.cf.mobile.datasdk.activity.BaseAuthorizationActivity;
import com.pivotal.cf.mobile.datasdk.authorization.AuthorizationEngine;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProviderImpl;

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

    public void obtainAuthorization(BaseAuthorizationActivity activity, DataParameters parameters) {
        // TODO - AuthorizationEngine should be launched on a worker thread.
        // TODO - Calls to AuthorizationEngine should be serialized.
        final AuthorizationPreferencesProvider preferences = new AuthorizationPreferencesProviderImpl(activity);
        final AuthorizationEngine engine = new AuthorizationEngine(preferences);
        engine.obtainAuthorization(activity, parameters);
    }
}
