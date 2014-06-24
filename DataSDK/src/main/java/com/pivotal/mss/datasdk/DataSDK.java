package com.pivotal.mss.datasdk;

import android.app.Activity;
import android.content.Context;

import com.pivotal.mss.common.util.ThreadUtil;
import com.pivotal.mss.datasdk.api.ApiProvider;
import com.pivotal.mss.datasdk.api.ApiProviderImpl;
import com.pivotal.mss.datasdk.client.AuthorizationEngine;
import com.pivotal.mss.datasdk.client.AuthorizationException;
import com.pivotal.mss.datasdk.client.AuthorizedResourceClient;
import com.pivotal.mss.datasdk.client.AuthorizedResourceClientImpl;
import com.pivotal.mss.datasdk.prefs.AuthorizationPreferencesProvider;
import com.pivotal.mss.datasdk.prefs.AuthorizationPreferencesProviderImpl;

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
        return new AuthorizedResourceClientImpl(apiProvider, preferences);
    }

    private void assertCalledOnUIThread() throws AuthorizationException {
        if (!ThreadUtil.isUIThread()) {
            throw new AuthorizationException("Must be called on the main thread");
        }
    }
}
