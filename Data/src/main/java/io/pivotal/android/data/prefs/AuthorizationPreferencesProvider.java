package io.pivotal.android.data.prefs;

public interface AuthorizationPreferencesProvider {
    
    public String getClientId();
    public void setClientId(String clientId);

    public String getClientSecret();
    public void setClientSecret(String clientSecret);

    public String getAuthorizationUrl();
    public void setAuthorizationUrl(String authorizationUrl);

    public String getTokenUrl();
    public void setTokenUrl(String tokenUrl);

    public String getRedirectUrl();
    public void setRedirectUrl(String redirectUrl);

    public String getDataServicesUrl();
    public void setDataServicesUrl(String dataServicesUrl);
}
