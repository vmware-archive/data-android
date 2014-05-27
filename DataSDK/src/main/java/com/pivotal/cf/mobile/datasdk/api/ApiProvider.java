package com.pivotal.cf.mobile.datasdk.api;

import android.content.Context;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

public interface ApiProvider {

    public HttpTransport getTransport();
    public HttpRequestFactory getFactory(Credential credential);
    public AuthorizedApiRequest getAuthorizedApiRequest(Context context, AuthorizationPreferencesProvider authorizationPreferencesProvider) throws Exception;

}
