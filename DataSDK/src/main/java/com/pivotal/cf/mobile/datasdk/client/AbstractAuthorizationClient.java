package com.pivotal.cf.mobile.datasdk.client;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

public class AbstractAuthorizationClient {

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

    protected void checkIfAuthorizationPreferencesAreSaved() throws AuthorizationException {
        if (authorizationPreferencesProvider.getClientId() == null || authorizationPreferencesProvider.getClientId().isEmpty()) {
            throw new AuthorizationException("parameters.clientId may not be null or empty");
        }
        if (authorizationPreferencesProvider.getClientSecret() == null || authorizationPreferencesProvider.getClientSecret().isEmpty()) {
            throw new AuthorizationException("parameters.clientSecret may not be null or empty");
        }
        if (authorizationPreferencesProvider.getAuthorizationUrl() == null) {
            throw new AuthorizationException("parameters.authorizationUrl may not be null");
        }
        if (authorizationPreferencesProvider.getTokenUrl() == null) {
            throw new AuthorizationException("parameters.tokenUrl may not be null");
        }
        if (authorizationPreferencesProvider.getRedirectUrl() == null) {
            throw new AuthorizationException("parameters.redirectUrl may not be null");
        }
    }


    // TODO - write Javadocs
    public void setParameters(DataParameters parameters) {
        verifyDataParameters(parameters);
        saveDataParameters(parameters);
        // TODO - if parameters have been modified since the last time they were saved then clear any saved credentials.
    }

    private void saveDataParameters(DataParameters parameters) {
        authorizationPreferencesProvider.setClientId(parameters.getClientId());
        authorizationPreferencesProvider.setClientSecret(parameters.getClientSecret());
        authorizationPreferencesProvider.setAuthorizationUrl(parameters.getAuthorizationUrl());
        authorizationPreferencesProvider.setTokenUrl(parameters.getTokenUrl());
        authorizationPreferencesProvider.setRedirectUrl(parameters.getRedirectUrl());
    }

    private void verifyDataParameters(DataParameters parameters) {
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
