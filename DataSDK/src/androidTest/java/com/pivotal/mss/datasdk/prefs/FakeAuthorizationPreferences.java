package com.pivotal.mss.datasdk.prefs;

import java.net.URL;

public class FakeAuthorizationPreferences implements AuthorizationPreferencesProvider {

    private String clientId;
    private String clientSecret;
    private URL authorizationUrl;
    private URL tokenUrl;
    private String redirectUrl;
    private URL dataServicesUrl;

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
    public String getRedirectUrl() {
        return redirectUrl;
    }

    @Override
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @Override
    public URL getDataServicesUrl() {
        return dataServicesUrl;
    }

    @Override
    public void setDataServicesUrl(URL dataServicesUrl) {
        this.dataServicesUrl = dataServicesUrl;
    }
}
