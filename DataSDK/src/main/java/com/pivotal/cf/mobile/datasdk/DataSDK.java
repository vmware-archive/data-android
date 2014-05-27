package com.pivotal.cf.mobile.datasdk;

import android.app.Activity;
import android.content.Context;

import com.pivotal.cf.mobile.datasdk.client.AuthorizationEngine;
import com.pivotal.cf.mobile.datasdk.client.AuthorizedResourceClient;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.api.ApiProviderImpl;
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

    public void obtainAuthorization(Activity activity, DataParameters parameters) {
        // TODO - AuthorizationEngine should be launched on a worker thread.
        // TODO - Calls to AuthorizationEngine should be serialized.
        final AuthorizationPreferencesProvider preferences = new AuthorizationPreferencesProviderImpl(activity);
        final ApiProvider apiProvider = new ApiProviderImpl();
        final AuthorizationEngine engine = new AuthorizationEngine(activity, apiProvider, preferences);
        engine.obtainAuthorization(activity, parameters);
    }

    public AuthorizedResourceClient getClient(Context context) {
        final AuthorizationPreferencesProvider preferences = new AuthorizationPreferencesProviderImpl(context);
        final ApiProvider apiProvider = new ApiProviderImpl();
        return new AuthorizedResourceClient(context, apiProvider, preferences);
    }
}
