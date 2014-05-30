package com.pivotal.cf.mobile.datasdk.api;

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
    private boolean shouldUnauthorizedListenerBeCalled;
    private int httpStatus;
    private String contentType;
    private String contentData;
    private Credential credential;
    private TokenResponse expectedTokenResponse;
    private TokenResponse savedTokenResponse;

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
    public AuthorizedApiRequest getAuthorizedApiRequest(AuthorizationPreferencesProvider authorizationPreferencesProvider) {

        final FakeAuthorizedApiRequest apiRequest = new FakeAuthorizedApiRequest(
                shouldAuthorizationListenerBeSuccessful,
                shouldAuthorizedApiRequestBeSuccessful,
                shouldUnauthorizedListenerBeCalled,
                httpStatus,
                contentType,
                contentData,
                savedTokenResponse,
                expectedTokenResponse,
                credential);

        apiRequests.add(apiRequest);
        return apiRequest;
    }

    public void setShouldAuthorizedApiRequestBeSuccessful(boolean b) {
        shouldAuthorizedApiRequestBeSuccessful = b;
    }

    public void setShouldUnauthorizedListenerBeCalled(boolean b) {
        shouldUnauthorizedListenerBeCalled = b;
    }

    public void setHttpRequestResults(int httpStatus, String contentType, String contentData) {
        this.httpStatus = httpStatus;
        this.contentType = contentType;
        this.contentData = contentData;
    }

    public void setShouldAuthorizationListenerBeSuccessful(boolean b) {
        shouldAuthorizationListenerBeSuccessful = b;
    }

    public void setTokenResponseToReturn(TokenResponse tokenResponse) {
        this.expectedTokenResponse = tokenResponse;
    }

    public void setSavedTokenResponse(TokenResponse tokenResponse) {
        this.savedTokenResponse = tokenResponse;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public List<FakeAuthorizedApiRequest> getApiRequests() {
        return Collections.unmodifiableList(apiRequests);
    }
}
