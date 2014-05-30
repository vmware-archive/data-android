package com.pivotal.cf.mobile.datasdk.client;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

public class AbstractAuthorizationClient {

    // TODO - we might be able to get rid of many of these fields since they have been buried in the ApiProvider

    protected ApiProvider apiProvider;
    protected AuthorizationPreferencesProvider authorizationPreferencesProvider;

    public AbstractAuthorizationClient(ApiProvider apiProvider,
                                       AuthorizationPreferencesProvider authorizationPreferencesProvider) {

        verifyArguments(authorizationPreferencesProvider, apiProvider);
        saveArguments(authorizationPreferencesProvider, apiProvider);
    }

    private void verifyArguments(AuthorizationPreferencesProvider authorizationPreferencesProvider,
                                 ApiProvider apiProvider) {

        if (authorizationPreferencesProvider == null) {
            throw new IllegalArgumentException("authorizationPreferencesProvider may not be null");
        }
        if (apiProvider == null) {
            throw new IllegalArgumentException("httpRequestFactoryProvider may not be null");
        }
    }

    private void saveArguments(AuthorizationPreferencesProvider authorizationPreferencesProvider,
                               ApiProvider apiProvider) {
        this.authorizationPreferencesProvider = authorizationPreferencesProvider;
        this.apiProvider = apiProvider;
    }

    protected boolean areAuthorizationPreferencesAvailable() {
        if (authorizationPreferencesProvider.getClientId() == null) {
            return false;
        }
        if (authorizationPreferencesProvider.getClientSecret() == null) {
            return false;
        }
        if (authorizationPreferencesProvider.getAuthorizationUrl() == null) {
            return false;
        }
        if (authorizationPreferencesProvider.getTokenUrl() == null) {
            return false;
        }
        if (authorizationPreferencesProvider.getRedirectUrl() == null) {
            return false;
        }
        return true;
    }

    protected void verifyDataParameters(DataParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getClientId() == null || parameters.getClientId().isEmpty()) {
            throw new IllegalArgumentException("parameters.clientId may not be null or empty");
        }
        if (parameters.getClientSecret() == null || parameters.getClientSecret().isEmpty()) {
            throw new IllegalArgumentException("parameters.clientSecret may not be null or empty");
        }
        if (parameters.getAuthorizationUrl() == null) {
            throw new IllegalArgumentException("parameters.authorizationUrl may not be null");
        }
        if (parameters.getTokenUrl() == null) {
            throw new IllegalArgumentException("parameters.tokenUrl may not be null");
        }
        if (parameters.getRedirectUrl() == null) {
            throw new IllegalArgumentException("parameters.redirectUrl may not be null");
        }
    }
}
