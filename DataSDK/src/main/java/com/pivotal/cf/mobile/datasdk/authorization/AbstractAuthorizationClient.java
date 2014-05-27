package com.pivotal.cf.mobile.datasdk.authorization;

import android.app.Application;
import android.content.Context;

import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

public class AbstractAuthorizationClient {

    // TODO - we might be able to get rid of many of these fields since they have been buried in the ApiProvider

    protected Context context;
    protected ApiProvider apiProvider;
    protected AuthorizationPreferencesProvider authorizationPreferencesProvider;

    public AbstractAuthorizationClient(Context context,
                                       ApiProvider apiProvider,
                                       AuthorizationPreferencesProvider authorizationPreferencesProvider) {

        verifyArguments(context, authorizationPreferencesProvider, apiProvider);
        saveArguments(context, authorizationPreferencesProvider, apiProvider);
    }

    private void verifyArguments(Context context,
                                 AuthorizationPreferencesProvider authorizationPreferencesProvider,
                                 ApiProvider apiProvider) {

        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (authorizationPreferencesProvider == null) {
            throw new IllegalArgumentException("authorizationPreferencesProvider may not be null");
        }
        if (apiProvider == null) {
            throw new IllegalArgumentException("httpRequestFactoryProvider may not be null");
        }
    }

    private void saveArguments(Context context,
                               AuthorizationPreferencesProvider authorizationPreferencesProvider,
                               ApiProvider apiProvider) {
        if (!(context instanceof Application)) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
        this.authorizationPreferencesProvider = authorizationPreferencesProvider;
        this.apiProvider = apiProvider;
    }
}
