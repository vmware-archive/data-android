package com.pivotal.cf.mobile.datasdk.prefs;

import java.net.URL;

public interface AuthorizationPreferencesProvider {
    
    public String getClientId();
    public void setClientId(String clientId);

    public String getClientSecret();
    public void setClientSecret(String clientSecret);

    public URL getAuthorizationUrl();
    public void setAuthorizationUrl(URL authorizationUrl);

    public URL getTokenUrl();
    public void setTokenUrl(URL tokenUrl);

    public URL getRedirectUrl();
    public void setRedirectUrl(URL redirectUrl);
}
