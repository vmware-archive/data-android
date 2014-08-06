/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data.client;

import com.google.api.client.auth.oauth2.Credential;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import io.pivotal.android.data.api.ApiProvider;
import io.pivotal.android.data.api.AuthorizedApiRequest;
import io.pivotal.android.data.DataException;
import io.pivotal.android.data.prefs.AuthorizationPreferencesProvider;
import io.pivotal.android.data.util.Logger;

public class AuthorizedResourceClientImpl extends AbstractAuthorizationClient implements AuthorizedResourceClient {

    public AuthorizedResourceClientImpl(ApiProvider apiProvider,
                                        AuthorizationPreferencesProvider authorizationPreferencesProvider) {

        super(apiProvider, authorizationPreferencesProvider);
    }

    @Override
    public void executeDataServicesRequest(String method,
                                           String className,
                                           String objectId,
                                           Map<String, Object> headers,
                                           String contentType,
                                           String contentEncoding,
                                           byte[] contentData,
                                           Listener listener) throws AuthorizationException, DataException {

        URL requestUrl;
        try {
            final String dataServicesUrl = authorizationPreferencesProvider.getDataServicesUrl();
            requestUrl = new URL(dataServicesUrl + "/" + className + "/" + objectId);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }

        executeHttpRequest(method, requestUrl, headers, contentType, contentEncoding, contentData, listener);
    }


    // TODO provide documentation - including which exceptions can get thrown
    // This method may or may not be called from a background thread.
    // NOTE - listener may be called on a background thread    @Override
    public void executeHttpRequest(final String method,
                                   final URL url,
                                   final Map<String, Object> headers,
                                   final String contentType,
                                   final String contentEncoding,
                                   final byte[] contentData,
                                   final Listener listener) throws AuthorizationException {


        verifyArguments(method, url, contentType, contentEncoding, listener);
        checkIfAuthorizationPreferencesAreSaved();

        final AuthorizedApiRequest request = apiProvider.getAuthorizedApiRequest(authorizationPreferencesProvider);
        request.loadCredential(new AuthorizedApiRequest.LoadCredentialListener() {

            @Override
            public void onCredentialLoaded(Credential credential) {

                if (credential == null) {
                    listener.onFailure("Authorization credentials are not available. You must authorize with DataSDK.obtainAuthorization first.");
                    return;
                }

                Logger.fd("Making '%s' call to '%s'.", method, url);

                try {

                    request.executeHttpRequest(method,
                            url,
                            headers,
                            contentType,
                            contentEncoding,
                            contentData,
                            credential,
                            authorizationPreferencesProvider,
                            new AuthorizedApiRequest.HttpOperationListener() {

                                @Override
                                public void onSuccess(int returnedHttpStatusCode,
                                                      String returnedContentType,
                                                      String returnedContentEncoding,
                                                      InputStream returnedContentData) {

                                    if (isSuccessfulHttpStatusCode(returnedHttpStatusCode)) {
                                        listener.onSuccess(returnedHttpStatusCode, returnedContentType, returnedContentEncoding, returnedContentData);
                                    } else {
                                        listener.onFailure("Received failure HTTP status code: '" + returnedHttpStatusCode + "'");
                                    }
                                }

                                @Override
                                public void onUnauthorized() {
                                    request.clearSavedCredentialSynchronously();
                                    listener.onUnauthorized();
                                }

                                @Override
                                public void onFailure(String reason) {
                                    listener.onFailure(reason);
                                }
                            }
                    );

                } catch (Exception e) {
                    Logger.ex("Could not perform authorized GET request", e);
                    listener.onFailure("Could not perform authorized GET request: " + e.getLocalizedMessage());
                }
            }
        });
    }

    private boolean isSuccessfulHttpStatusCode(int httpStatusCode) {
        return httpStatusCode >= 200 && httpStatusCode < 300;
    }

    private void verifyArguments(String method, URL url, String contentType, String contentEncoding, Listener listener) {
        if (method == null) {
            throw new IllegalArgumentException("method may not be null");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("contentType may not be null");
        }
        if (contentEncoding == null) {
            throw new IllegalArgumentException("contentEncoding may not be null");
        }
        if (url == null) {
            throw new IllegalArgumentException("url may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }
}
