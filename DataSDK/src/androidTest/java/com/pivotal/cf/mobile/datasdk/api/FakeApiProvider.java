package com.pivotal.cf.mobile.datasdk.api;

import android.content.Context;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FakeApiProvider implements ApiProvider {

    private List<FakeAuthorizedApiRequest> apiRequests = new LinkedList<FakeAuthorizedApiRequest>();
    private boolean shouldAuthorizationListenerBeSuccessful;
    private boolean shouldAuthorizedApiRequestBeSuccessful;
    private int httpStatus;
    private String contentType;
    private String contentData;
    private TokenResponse tokenResponse;
    private Credential credential;

    @Override
    public HttpTransport getTransport() {
        // TODO - implement
        return null;
    }

    @Override
    public HttpRequestFactory getFactory(Credential credential) {
        return null;
    }

    @Override
    public AuthorizedApiRequest getAuthorizedApiRequest(Context context,
                                                        AuthorizationPreferencesProvider authorizationPreferencesProvider) {

        final FakeAuthorizedApiRequest apiRequest = new FakeAuthorizedApiRequest(
                shouldAuthorizationListenerBeSuccessful,
                shouldAuthorizedApiRequestBeSuccessful,
                httpStatus,
                contentType,
                contentData,
                tokenResponse,
                credential);

        apiRequests.add(apiRequest);
        return apiRequest;
    }

    public void setShouldAuthorizedApiRequestBeSuccessful(boolean b) {
        shouldAuthorizedApiRequestBeSuccessful = b;
    }

    public void setHttpRequestResults(int httpStatus, String contentType, String contentData) {
        this.httpStatus = httpStatus;
        this.contentType = contentType;
        this.contentData = contentData;
    }

    public void setShouldAuthorizationListenerBeSuccessful(boolean b) {
        shouldAuthorizationListenerBeSuccessful = b;
    }

    public void setTokenResponse(TokenResponse tokenResponse) {
        this.tokenResponse = tokenResponse;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public List<FakeAuthorizedApiRequest> getApiRequests() {
        return Collections.unmodifiableList(apiRequests);
    }
}
