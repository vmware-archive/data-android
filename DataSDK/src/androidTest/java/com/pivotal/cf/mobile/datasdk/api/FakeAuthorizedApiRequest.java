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
    private boolean didCallObtainAuthorization;
    private boolean didCallGetAccessToken;
    private final String contentData;
    private final String contentType;
    private final int httpStatusCode;
    private TokenResponse tokenResponseToReturn;
    private TokenResponse savedTokenReponse;
    private final Credential credentialToReturn;

    public FakeAuthorizedApiRequest(boolean shouldAuthorizationListenerBeSuccessful,
                                    boolean shouldAuthorizedApiRequestBeSuccessful,
                                    int httpStatus,
                                    String contentType,
                                    String contentData,
                                    TokenResponse tokenResponseToReturn,
                                    Credential credentialToReturn) {

        this.shouldAuthorizationListenerBeSuccessful = shouldAuthorizationListenerBeSuccessful;
        this.shouldAuthorizedApiRequestBeSuccessful = shouldAuthorizedApiRequestBeSuccessful;
        this.httpStatusCode = httpStatus;
        this.contentType = contentType;
        this.contentData = contentData;
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
            listener.onSuccess(tokenResponseToReturn);
        } else {
            listener.onFailure("Fake request failed fakely.");
        }
    }

    @Override
    public void get(URL url,
                    Map<String, String> headers,
                    Credential credential, AuthorizationPreferencesProvider authorizationPreferencesProvider,
                    HttpOperationListener listener) {

        if (shouldAuthorizedApiRequestBeSuccessful) {
            listener.onSuccess(httpStatusCode, contentType, getInputStream());
        } else {
            listener.onFailure("Fake request failed fakely.");
        }
    }

    @Override
    public void storeTokenResponse(TokenResponse tokenResponse) {
        savedTokenReponse = tokenResponse;
    }

    @Override
    public Credential loadCredential() {
        return credentialToReturn;
    }

    public TokenResponse getSavedTokenReponse() {
        return savedTokenReponse;
    }

    public boolean didCallObtainAuthorization() {
        return didCallObtainAuthorization;
    }

    public boolean didCallGetAccessToken() {
        return didCallGetAccessToken;
    }

    private InputStream getInputStream() {
        return new ByteArrayInputStream(contentData.getBytes());
    }
}
