/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data.prefs;

public class FakeAuthorizationPreferences implements AuthorizationPreferencesProvider {

    private String clientId;
    private String clientSecret;
    private String authorizationUrl;
    private String tokenUrl;
    private String redirectUrl;
    private String dataServicesUrl;

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    @Override
    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    @Override
    public String getTokenUrl() {
        return tokenUrl;
    }

    @Override
    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    @Override
    public String getRedirectUrl() {
        return redirectUrl;
    }

    @Override
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @Override
    public String getDataServicesUrl() {
        return dataServicesUrl;
    }

    @Override
    public void setDataServicesUrl(String dataServicesUrl) {
        this.dataServicesUrl = dataServicesUrl;
    }
}
