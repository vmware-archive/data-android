/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.app.Activity;
import android.content.Context;

import io.pivotal.android.data.api.ApiProvider;
import io.pivotal.android.data.api.ApiProviderImpl;
import io.pivotal.android.data.client.AuthorizationEngine;
import io.pivotal.android.data.client.AuthorizationException;
import io.pivotal.android.data.client.AuthorizedResourceClient;
import io.pivotal.android.data.client.AuthorizedResourceClientImpl;
import io.pivotal.android.data.prefs.AuthorizationPreferencesProvider;
import io.pivotal.android.data.prefs.AuthorizationPreferencesProviderImpl;
import io.pivotal.android.data.util.ThreadUtil;

public class DataStore {

    private static DataStore instance;

    public synchronized static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private DataStore() {}

    // TODO - add Javadocs.  Must be called on UI thread?
    public void setParameters(Context context, DataStoreParameters parameters) {
        final AuthorizationPreferencesProvider preferences = new AuthorizationPreferencesProviderImpl(context);
        final ApiProvider apiProvider = new ApiProviderImpl(context);
        final AuthorizationEngine engine = new AuthorizationEngine(apiProvider, preferences);
        engine.setParameters(parameters);
    }

    // TODO - add Javadocs. Note: must be called on UI thread.
    public void obtainAuthorization(Activity activity) {
        assertCalledOnUIThread();
        final AuthorizationPreferencesProvider preferences = new AuthorizationPreferencesProviderImpl(activity);
        final ApiProvider apiProvider = new ApiProviderImpl(activity);
        final AuthorizationEngine engine = new AuthorizationEngine(apiProvider, preferences);
        engine.obtainAuthorization(activity);
    }

    // TODO - add Javadocs. Note: must be called on UI thread.
    public void clearAuthorization(Context context) {
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
