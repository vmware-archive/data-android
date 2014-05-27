package com.pivotal.cf.mobile.datasdk.client;

import android.content.Context;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;
import com.pivotal.cf.mobile.datasdk.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AuthorizedResourceClientTest extends AbstractAuthorizedResourceClientTest<AuthorizedResourceClient> {

    private static final String HTTP_TEST_GET_URL = "http://test.get.url";
    private static final String TEST_CONTENT_TYPE = "test/content-type";
    private static final String TEST_CONTENT_DATA = "TEST CONTENT DATA";
    private static final int TEST_HTTP_STATUS_CODE = 200;

    private URL url;
    private Map<String, String> headers;
    private AuthorizedResourceClient.Listener listener;
    private boolean shouldSuccessListenerBeCalled;
    private boolean shouldRequestBeSuccessful;
    private int expectedHttpStatusCode;
    private String expectedContentType;
    private String expectedContentData;
    private Credential credential;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
        url = new URL(HTTP_TEST_GET_URL);
        headers = new HashMap<String, String>();
        listener = new AuthorizedResourceClient.Listener() {

            @Override
            public void onSuccess(int httpStatusCode, String contentType, InputStream result) {
                assertTrue(shouldSuccessListenerBeCalled);
                assertEquals(expectedHttpStatusCode, httpStatusCode);
                assertEquals(expectedContentType, contentType);
                try {
                    assertEquals(expectedContentData, StreamUtil.readInput(result));
                } catch (IOException e) {
                    fail();
                }
                semaphore.release();
            }

            @Override
            public void onFailure(String reason) {
                assertFalse(shouldSuccessListenerBeCalled);
                semaphore.release();
            }
        };
    }

    public void testGetRequiresUrl() throws Exception {
        baseTestGetRequires(null, headers, parameters, listener);
    }

    public void testGetRequiresParameters() throws Exception {
        baseTestGetRequires(url, headers, null, listener);
    }

    public void testGetRequiresListener() throws Exception {
        baseTestGetRequires(url, headers, parameters, null);
    }

    public void testRequiresAuthorizationParameters() throws Exception {
        try {
            saveCredential();
            getClient().get(url, headers, parameters, listener);
            fail();
        } catch (AuthorizationException e) {
            // success
        }
    }

    public void testRequiresSavedCredential() throws Exception {
        try {
            savePreferences();
            getClient().get(url, headers, parameters, listener);
            fail();
        } catch (AuthorizationException e) {
            // success
        }
    }

    public void testGetDoesNotRequiresHeaders() throws Exception {
        setupSuccessfulRequest(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        getClient().get(url, null, parameters, listener);
        semaphore.acquire();
    }

    public void testSuccessfulGet() throws Exception {
        setupSuccessfulRequest(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        apiProvider.setHttpRequestResults(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        getClient().get(url, headers, parameters, listener);
        semaphore.acquire();
    }

    public void testFailedGet() throws Exception {
        setupFailedRequest();
        getClient().get(url, headers, parameters, listener);
        semaphore.acquire();
    }

    public void testFailedGet404() throws Exception {
        setupSuccessfulRequestWithFailedHttpStatus(404, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        getClient().get(url, headers, parameters, listener);
        semaphore.acquire();
    }

    // TODO - add test showing that credentials are cleared after a 401 error

    @Override
    protected AuthorizedResourceClient construct(Context context,
                                                 AuthorizationPreferencesProvider preferencesProvider,
                                                 ApiProvider apiProvider) {

        return new AuthorizedResourceClient(context, apiProvider, preferencesProvider);
    }

    private AuthorizedResourceClient getClient() {
        return new AuthorizedResourceClient(getContext(), apiProvider, preferences);
    }

    private void baseTestGetRequires(final URL url,
                                     final Map<String, String> headers,
                                     DataParameters parameters,
                                     final AuthorizedResourceClient.Listener listener) throws Exception {
        try {
            getClient().get(url, headers, parameters, listener);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    private void saveCredential() {
        apiProvider.setCredential(credential);
    }

    private void setupSuccessfulRequest(int httpStatusCode, String contentType, String contentData) {
        savePreferences();
        saveCredential();
        shouldSuccessListenerBeCalled = true;
        shouldRequestBeSuccessful = true;
        apiProvider.setShouldAuthorizedApiRequestBeSuccessful(shouldRequestBeSuccessful);
        setupHttpRequestResults(httpStatusCode, contentType, contentData);
    }

    private void setupFailedRequest() {
        savePreferences();
        saveCredential();
        shouldSuccessListenerBeCalled = false;
        shouldRequestBeSuccessful = false;
        apiProvider.setShouldAuthorizedApiRequestBeSuccessful(shouldRequestBeSuccessful);
    }

    private void setupSuccessfulRequestWithFailedHttpStatus(int httpStatusCode, String contentType, String contentData) {
        savePreferences();
        saveCredential();
        shouldSuccessListenerBeCalled = false;
        shouldRequestBeSuccessful = true;
        apiProvider.setShouldAuthorizedApiRequestBeSuccessful(shouldRequestBeSuccessful);
        setupHttpRequestResults(httpStatusCode, contentType, contentData);
    }

    private void setupHttpRequestResults(int httpStatusCode, String contentType, String contentData) {
        expectedHttpStatusCode = httpStatusCode;
        expectedContentType = contentType;
        expectedContentData = contentData;
        apiProvider.setHttpRequestResults(httpStatusCode, contentType, contentData);
    }
}
