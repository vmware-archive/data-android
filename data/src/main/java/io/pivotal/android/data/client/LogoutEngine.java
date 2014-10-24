/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data.client;

import com.google.api.client.auth.oauth2.Credential;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import io.pivotal.android.data.api.ApiProvider;
import io.pivotal.android.data.api.AuthorizedApiRequest;
import io.pivotal.android.data.prefs.AuthorizationPreferencesProvider;
import io.pivotal.android.data.util.Logger;

public class LogoutEngine extends AbstractAuthorizationClient {

    public LogoutEngine(final ApiProvider apiProvider,
                        final AuthorizationPreferencesProvider authorizationPreferencesProvider) {

        super(apiProvider, authorizationPreferencesProvider);
    }

    /**
     * Starts the logout process.
     *
     */
    // TODO - describe thrown exceptions
    public void logout() throws AuthorizationException {
        checkIfAuthorizationPreferencesAreSaved();
        loadCredential();
    }

    private void loadCredential() {
        final AuthorizedApiRequest request = getRequest();
        request.loadCredential(new AuthorizedApiRequest.LoadCredentialListener() {

            @Override
            public void onCredentialLoaded(final Credential credential) {

                if (credential == null) {
                    // Already logged out. Considered successful.
                    return;
                }
                logoutFromAuthProvider(credential);
            }
        });
    }

    private void logoutFromAuthProvider(final Credential credential) throws AuthorizationException {
        final AuthorizedApiRequest request = getRequest();
        final URL logoutUrl = getLogoutUrl();
        final AuthorizedApiRequest.HttpOperationListener listener = new AuthorizedApiRequest.HttpOperationListener() {

            @Override
            public void onSuccess(int httpStatusCode, String contentType, String contentEncoding, InputStream result) {
                if (!isSuccessHttpStatusCode(httpStatusCode)) {
                    onFailure("Server returned HTTP status code: " + httpStatusCode);
                    return;
                }

                Logger.v("Successfully logged out from authorization server.");
                deleteCredential();
            }

            @Override
            public void onUnauthorized() {
                Logger.e("Unauthorized result trying to get access tokens from identity server.");
            }

            @Override
            public void onFailure(String reason) {
                Logger.e("Error fetching access tokens from identity server: " + reason);
            }
        };

        request.executeHttpRequest("GET", logoutUrl, null, null, "UTF-8", null, credential, authorizationPreferencesProvider, listener);
    }

    private void deleteCredential() {
        final AuthorizedApiRequest request = getRequest();
        request.clearSavedCredentialAsynchronously(new AuthorizedApiRequest.ClearSavedCredentialListener() {

            @Override
            public void onSavedCredentialCleared() {
                Logger.v("Deleted credentials from persistent storage.");
            }
        });
    }

    private AuthorizedApiRequest getRequest() {
        final AuthorizedApiRequest request = apiProvider.getAuthorizedApiRequest(authorizationPreferencesProvider);
        return request;
    }

    private boolean isSuccessHttpStatusCode(final int httpStatusCode) {
        return httpStatusCode >= 200 && httpStatusCode < 300;
    }

    private URL getLogoutUrl() throws AuthorizationException {
        final String base = authorizationPreferencesProvider.getAuthorizationUrl() + "/oauth/logout";
        try {
            final URL url = new URL(base);
            return url;
        } catch (MalformedURLException e) {
            throw new AuthorizationException(e.getMessage());
        }
    }
}
