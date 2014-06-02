package com.pivotal.cf.mobile.datasdk;

import android.app.Activity;
import android.content.Context;

import com.pivotal.cf.mobile.common.util.ThreadUtil;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.api.ApiProviderImpl;
import com.pivotal.cf.mobile.datasdk.client.AuthorizationEngine;
import com.pivotal.cf.mobile.datasdk.client.AuthorizationException;
import com.pivotal.cf.mobile.datasdk.client.AuthorizedResourceClient;
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

    // TODO - add Javadocs.  Must be called on UI thread?
    public void setParameters(Context context, DataParameters parameters) throws Exception {
        final AuthorizationPreferencesProvider preferences = new AuthorizationPreferencesProviderImpl(context);
        final ApiProvider apiProvider = new ApiProviderImpl(context);
        final AuthorizationEngine engine = new AuthorizationEngine(apiProvider, preferences);
        engine.setParameters(parameters);
    }

    // TODO - add Javadocs. Note: must be called on UI thread.
    public void obtainAuthorization(Activity activity) throws Exception {
        assertCalledOnUIThread();
        final AuthorizationPreferencesProvider preferences = new AuthorizationPreferencesProviderImpl(activity);
        final ApiProvider apiProvider = new ApiProviderImpl(activity);
        final AuthorizationEngine engine = new AuthorizationEngine(apiProvider, preferences);
        engine.obtainAuthorization(activity);
    }

    // TODO - add Javadocs. Note: must be called on UI thread.
    public void clearAuthorization(Context context) throws Exception {
        assertCalledOnUIThread();
        final AuthorizationPreferencesProvider preferences = new AuthorizationPreferencesProviderImpl(context);
        final ApiProvider apiProvider = new ApiProviderImpl(context);
        final AuthorizationEngine engine = new AuthorizationEngine(apiProvider, preferences);
        engine.clearAuthorization();
    }

    // TODO - add Javadocs. Note: does not to be called on UI thread.
    public AuthorizedResourceClient getClient(Context context) {
        final AuthorizationPreferencesProvider preferences = new AuthorizationPreferencesProviderImpl(context);
        final ApiProvider apiProvider = new ApiProviderImpl(context);
        return new AuthorizedResourceClient(apiProvider, preferences);
    }

    private void assertCalledOnUIThread() throws AuthorizationException {
        if (!ThreadUtil.isUIThread()) {
            throw new AuthorizationException("Must be called on the main thread");
        }
    }
}
