package com.pivotal.cf.mobile.datasdk.api;

import android.content.Context;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

public class ApiProviderImpl implements ApiProvider {

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    @Override
    public HttpTransport getTransport() {
        return HTTP_TRANSPORT;
    }

    @Override
    public HttpRequestFactory getFactory(Credential credential) {
        return HTTP_TRANSPORT.createRequestFactory(credential);
    }

    @Override
    public AuthorizedApiRequest getAuthorizedApiRequest(Context context, AuthorizationPreferencesProvider authorizationPreferencesProvider) {
        return new AuthorizedApiRequestImpl(context, authorizationPreferencesProvider, this);
    }
}
