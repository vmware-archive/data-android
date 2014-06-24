package com.pivotal.mss.datasdk.api;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.pivotal.mss.datasdk.client.AuthorizationException;
import com.pivotal.mss.datasdk.prefs.AuthorizationPreferencesProvider;

public interface ApiProvider {

    public HttpTransport getTransport();
    public HttpRequestFactory getFactory(Credential credential);
    public AuthorizedApiRequest getAuthorizedApiRequest(AuthorizationPreferencesProvider authorizationPreferencesProvider) throws AuthorizationException;

}
