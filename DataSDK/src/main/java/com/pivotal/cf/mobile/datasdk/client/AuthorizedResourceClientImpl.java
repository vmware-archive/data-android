package com.pivotal.cf.mobile.datasdk.client;

import com.google.api.client.auth.oauth2.Credential;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.api.AuthorizedApiRequest;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class AuthorizedResourceClientImpl extends AbstractAuthorizationClient implements AuthorizedResourceClient {

    public AuthorizedResourceClientImpl(ApiProvider apiProvider,
                                        AuthorizationPreferencesProvider authorizationPreferencesProvider) {

        super(apiProvider, authorizationPreferencesProvider);
    }

    // TODO provide documentation - including which exceptions can get thrown
    // This method may or may not be called from a background thread.
    // NOTE - listener may be called on a background thread
    public void get(final URL url,
                    final Map<String, Object> headers,
                    final Listener listener) throws AuthorizationException {

        verifyGetArguments(url, listener);
        checkIfAuthorizationPreferencesAreSaved();

        final AuthorizedApiRequest request = apiProvider.getAuthorizedApiRequest(authorizationPreferencesProvider);
        request.loadCredential(new AuthorizedApiRequest.LoadCredentialListener() {

            @Override
            public void onCredentialLoaded(Credential credential) {

                if (credential == null) {
                    listener.onFailure("Authorization credentials are not available. You must authorize with DataSDK.obtainAuthorization first.");
                    return;
                }

                Logger.fd("Making GET call to '%s'.", url);

                try {
                    request.get(url, headers, credential, authorizationPreferencesProvider, new AuthorizedApiRequest.HttpOperationListener() {

                        @Override
                        public void onSuccess(int httpStatusCode, String contentType, String contentEncoding, InputStream result) {
                            if (isSuccessfulHttpStatusCode(httpStatusCode)) {
                                listener.onSuccess(httpStatusCode, contentType, contentEncoding, result);
                            } else {
                                listener.onFailure("Received failure HTTP status code: '" + httpStatusCode + "'");
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
                    });

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

    private void verifyGetArguments(URL url, Listener listener) {
        if (url == null) {
            throw new IllegalArgumentException("url may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }
}
