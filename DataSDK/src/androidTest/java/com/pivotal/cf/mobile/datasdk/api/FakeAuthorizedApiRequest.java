package com.pivotal.cf.mobile.datasdk.api;

import android.app.Activity;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;
import com.pivotal.cf.mobile.datasdk.util.StreamUtil;

import java.net.URL;
import java.util.Map;

public class FakeAuthorizedApiRequest implements AuthorizedApiRequest {

    private final FakeApiProvider apiProvider;
    private final boolean shouldGetAccessTokenBeSuccessful;
    private final boolean shouldGetAccessTokenBeUnauthorized;
    private final boolean shouldAuthorizedApiRequestBeSuccessful;
    private final boolean shouldAuthorizedApiRequestBeUnauthorized;
    private final String contentData;
    private String contentEncoding;
    private final String contentType;
    private final int httpStatusCode;
    private boolean didCallObtainAuthorization;
    private boolean didCallGetAccessToken;
    private TokenResponse tokenResponseToReturn;
    private TokenResponse savedTokenResponse;
    private Map<String, Object> requestHeaders;

    public FakeAuthorizedApiRequest(FakeApiProvider apiProvider,
                                    boolean shouldGetAccessTokenBeSuccessful,
                                    boolean shouldGetAccessTokenBeUnauthorized,
                                    boolean shouldAuthorizedApiRequestBeSuccessful,
                                    boolean shouldAuthorizedApiRequestBeUnauthorized,
                                    int httpStatus,
                                    String contentType,
                                    String contentData,
                                    TokenResponse savedTokenResponse,
                                    TokenResponse tokenResponseToReturn) {

        this.apiProvider = apiProvider;
        this.shouldGetAccessTokenBeSuccessful = shouldGetAccessTokenBeSuccessful;
        this.shouldGetAccessTokenBeUnauthorized = shouldGetAccessTokenBeUnauthorized;
        this.shouldAuthorizedApiRequestBeSuccessful = shouldAuthorizedApiRequestBeSuccessful;
        this.shouldAuthorizedApiRequestBeUnauthorized = shouldAuthorizedApiRequestBeUnauthorized;
        this.httpStatusCode = httpStatus;
        this.contentType = contentType;
        this.contentData = contentData;
        this.savedTokenResponse = savedTokenResponse;
        this.tokenResponseToReturn = tokenResponseToReturn;
    }

    public void obtainAuthorization(Activity activity) {
        didCallObtainAuthorization = true;
    }

    @Override
    public void getAccessToken(String authorizationCode, AuthorizationListener listener) {
        didCallGetAccessToken = true;
        if (shouldGetAccessTokenBeSuccessful) {
            savedTokenResponse = tokenResponseToReturn;
            listener.onSuccess(tokenResponseToReturn);
        } else if (shouldGetAccessTokenBeUnauthorized) {
            listener.onAuthorizationDenied();
        } else {
            listener.onFailure("Fake request failed fakely.");
        }
    }

    @Override
    public void get(URL url,
                    Map<String, Object> headers,
                    Credential credential,
                    AuthorizationPreferencesProvider authorizationPreferencesProvider,
                    HttpOperationListener listener) {

        this.requestHeaders = headers;
        if (shouldAuthorizedApiRequestBeSuccessful) {
            listener.onSuccess(httpStatusCode, contentType, contentEncoding, StreamUtil.getInputStream(contentData));
        } else if (shouldAuthorizedApiRequestBeUnauthorized) {
            listener.onUnauthorized();
        } else {
            listener.onFailure("Fake request failed fakely.");
        }
    }

    @Override
    public void loadCredential(LoadCredentialListener listener) {
        listener.onCredentialLoaded(apiProvider.getCredential());
    }

    @Override
    public void clearSavedCredentialAsynchronously(ClearSavedCredentialListener listener) {
        clearSavedCredentialSynchronously();
        if (listener != null) {
            listener.onSavedCredentialCleared();
        }
    }

    @Override
    public void clearSavedCredentialSynchronously() {
        savedTokenResponse = null;
        apiProvider.setCredential(null);
    }

    public TokenResponse getSavedTokenResponse() {
        return savedTokenResponse;
    }

    public boolean didCallObtainAuthorization() {
        return didCallObtainAuthorization;
    }

    public boolean didCallGetAccessToken() {
        return didCallGetAccessToken;
    }

    public Map<String, Object> getRequestHeaders() {
        return requestHeaders;
    }

}
