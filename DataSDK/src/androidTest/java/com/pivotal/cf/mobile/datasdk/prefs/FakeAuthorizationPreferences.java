package com.pivotal.cf.mobile.datasdk.prefs;

import java.net.URL;

public class FakeAuthorizationPreferences implements AuthorizationPreferencesProvider {

    private String clientId;
    private String clientSecret;
    private URL authorizationUrl;
    private URL tokenUrl;
    private URL userInfoUrl;
    private URL redirectUrl;

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
    public URL getAuthorizationUrl() {
        return authorizationUrl;
    }

    @Override
    public void setAuthorizationUrl(URL authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    @Override
    public URL getTokenUrl() {
        return tokenUrl;
    }

    @Override
    public void setTokenUrl(URL tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    @Override
    public URL getRedirectUrl() {
        return redirectUrl;
    }

    @Override
    public void setRedirectUrl(URL redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
