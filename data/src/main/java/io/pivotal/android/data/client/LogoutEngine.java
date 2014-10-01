/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data.client;

import com.google.api.client.auth.oauth2.Credential;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import io.pivotal.android.data.api.ApiProvider;
import io.pivotal.android.data.api.AuthorizedApiRequest;
import io.pivotal.android.data.model.AccessTokenInfo;
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
                getTokenId(credential);
            }
        });
    }

    private void getTokenId(final Credential credential) throws AuthorizationException {
        final AuthorizedApiRequest request = getRequest();
        final URL getTokenIdUrl = getTokenIdUrl();
        final AuthorizedApiRequest.HttpOperationListener listener = new AuthorizedApiRequest.HttpOperationListener() {

            @Override
            public void onSuccess(int httpStatusCode, String contentType, String contentEncoding, InputStream result) {
                if (!isSuccessHttpStatusCode(httpStatusCode)) {
                    onFailure("Server returned HTTP status code: " + httpStatusCode);
                    return;
                }

                if (contentType == null || !contentType.startsWith("application/json")) {
                    onFailure("Server returned bad content type: " + contentType);
                    return;
                }

                if (result == null) {
                    onFailure("Server returned empty data.");
                    return;
                }

                final List<AccessTokenInfo> accessTokens = parseAccessTokenInfoList(result);
                final AccessTokenInfo accessToken = AccessTokenInfo.findItemMatchingCredential(credential, accessTokens);

                if (accessToken == null) {
                    Logger.v("Server does not have a token that matches our saved credential. Nothing to delete from server.");
                    deleteCredential();
                    return;
                }

                deleteToken(credential, accessToken.id);
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

        request.executeHttpRequest("GET", getTokenIdUrl, null, null, "UTF-8", null, credential, authorizationPreferencesProvider, listener);
    }

    private void deleteToken(final Credential credential, final String id) {
        final AuthorizedApiRequest request = getRequest();
        final URL deleteTokenUrl = getDeleteTokenUrl(id);
        final AuthorizedApiRequest.HttpOperationListener listener = new AuthorizedApiRequest.HttpOperationListener() {

            @Override
            public void onSuccess(int httpStatusCode, String contentType, String contentEncoding, InputStream result) {
                if (!isSuccessHttpStatusCode(httpStatusCode)) {
                    onFailure("Server returned HTTP status code: " + httpStatusCode);
                    return;
                }

                Logger.v("Successfully deleted access token from server.");
                deleteCredential();
            }

            @Override
            public void onUnauthorized() {
                Logger.e("Unauthorized result trying to delete access token from identity server.");
            }

            @Override
            public void onFailure(String reason) {
                Logger.e("Error deleting access token from identity server: " + reason);
            }
        };

        request.executeHttpRequest("DELETE", deleteTokenUrl, null, null, "UTF-8", null, credential, authorizationPreferencesProvider, listener);
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

    private List<AccessTokenInfo> parseAccessTokenInfoList(final InputStream stream) {
        final Gson gson = new Gson();
        final Type listType = new TypeToken<List<AccessTokenInfo>>() {}.getType();
        final Reader reader = new InputStreamReader(stream);
        final List<AccessTokenInfo> list = gson.fromJson(reader, listType);
        return list;
    }

    private boolean isSuccessHttpStatusCode(final int httpStatusCode) {
        return httpStatusCode >= 200 && httpStatusCode < 300;
    }

    private URL getTokenIdUrl() throws AuthorizationException {
        final String base = authorizationPreferencesProvider.getAuthorizationUrl() + "/api/tokens/access";
        try {
            final URL url = new URL(base);
            return url;
        } catch (MalformedURLException e) {
            throw new AuthorizationException(e.getMessage());
        }
    }

    private URL getDeleteTokenUrl(String id) throws AuthorizationException {
        final String base = authorizationPreferencesProvider.getAuthorizationUrl() + "/api/tokens/access/" + id;
        try {
            final URL url = new URL(base);
            return url;
        } catch (MalformedURLException e) {
            throw new AuthorizationException(e.getMessage());
        }
    }
}
