package com.pivotal.cf.mobile.datasdk.api;

import android.app.Activity;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class FakeAuthorizedApiRequest implements AuthorizedApiRequest {

    private final boolean shouldAuthorizationListenerBeSuccessful;
    private final boolean shouldAuthorizedApiRequestBeSuccessful;
    private final boolean shouldUnauthorizedListenerBeCalled;
    private final String contentData;
    private final String contentType;
    private final int httpStatusCode;
    private boolean didCallObtainAuthorization;
    private boolean didCallGetAccessToken;
    private TokenResponse tokenResponseToReturn;
    private TokenResponse savedTokenResponse;
    private Map<String, Object> requestHeaders;
    private Credential credentialToReturn;

    public FakeAuthorizedApiRequest(boolean shouldAuthorizationListenerBeSuccessful,
                                    boolean shouldAuthorizedApiRequestBeSuccessful,
                                    boolean shouldUnauthorizedListenerBeCalled,
                                    int httpStatus,
                                    String contentType,
                                    String contentData,
                                    TokenResponse savedTokenResponse,
                                    TokenResponse tokenResponseToReturn,
                                    Credential credentialToReturn) {

        this.shouldAuthorizationListenerBeSuccessful = shouldAuthorizationListenerBeSuccessful;
        this.shouldAuthorizedApiRequestBeSuccessful = shouldAuthorizedApiRequestBeSuccessful;
        this.shouldUnauthorizedListenerBeCalled = shouldUnauthorizedListenerBeCalled;
        this.httpStatusCode = httpStatus;
        this.contentType = contentType;
        this.contentData = contentData;
        this.savedTokenResponse = savedTokenResponse;
        this.tokenResponseToReturn = tokenResponseToReturn;
        this.credentialToReturn = credentialToReturn;
    }

    public void obtainAuthorization(Activity activity, DataParameters parameters) {
        didCallObtainAuthorization = true;
    }

    @Override
    public void getAccessToken(String authorizationCode, AuthorizationListener listener) {
        didCallGetAccessToken = true;
        if (shouldAuthorizationListenerBeSuccessful) {
            savedTokenResponse = tokenResponseToReturn;
            listener.onSuccess(tokenResponseToReturn);
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
            listener.onSuccess(httpStatusCode, contentType, getInputStream());
        } else if (shouldUnauthorizedListenerBeCalled) {
            listener.onUnauthorized();
        } else {
            listener.onFailure("Fake request failed fakely.");
        }
    }

    @Override
    public void loadCredential(LoadCredentialListener listener) {
        listener.onCredentialLoaded(credentialToReturn);
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
        credentialToReturn = null;
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

    private InputStream getInputStream() {
        return new ByteArrayInputStream(contentData.getBytes());
    }
}
