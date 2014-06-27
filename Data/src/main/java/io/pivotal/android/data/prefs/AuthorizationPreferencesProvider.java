package io.pivotal.android.data.prefs;

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

    public String getRedirectUrl();
    public void setRedirectUrl(String redirectUrl);

    public URL getDataServicesUrl();
    public void setDataServicesUrl(URL dataServicesUrl);
}
