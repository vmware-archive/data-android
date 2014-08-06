/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data.prefs;

public interface AuthorizationPreferencesProvider {
    
    public String getClientId();
    public void setClientId(String clientId);

    public String getAuthorizationUrl();
    public void setAuthorizationUrl(String authorizationUrl);

    public String getRedirectUrl();
    public void setRedirectUrl(String redirectUrl);

    public String getDataServicesUrl();
    public void setDataServicesUrl(String dataServicesUrl);
}
