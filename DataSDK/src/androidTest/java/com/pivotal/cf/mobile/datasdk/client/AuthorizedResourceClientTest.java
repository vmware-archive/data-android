package com.pivotal.cf.mobile.datasdk.client;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;
import com.pivotal.cf.mobile.datasdk.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AuthorizedResourceClientTest extends AbstractAuthorizedClientTest<AuthorizedResourceClient> {

    private static final String TEST_HTTP_GET_URL = "http://test.get.url";
    private static final String TEST_CONTENT_TYPE = "test/content-type";
    private static final String TEST_CONTENT_DATA = "TEST CONTENT DATA";
    private static final String TEST_HEADER_NAME = "Test Header Name";
    private static final String TEST_HEADER_VALUE = "Test Header Value";
    private static final int TEST_HTTP_STATUS_CODE = 200;

    private URL url;
    private Map<String, Object> headers;
    private AuthorizedResourceClient.Listener listener;
    private boolean shouldSuccessListenerBeCalled;
    private boolean shouldRequestBeSuccessful;
    private boolean shouldUnauthorizedListenerBeCalled;
    private int expectedHttpStatusCode;
    private String expectedContentType;
    private String expectedContentData;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        url = new URL(TEST_HTTP_GET_URL);
        headers = new HashMap<String, Object>();
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
            public void onUnauthorized() {
                assertTrue(shouldUnauthorizedListenerBeCalled);
                assertFalse(shouldSuccessListenerBeCalled);
                semaphore.release();
            }

            @Override
            public void onFailure(String reason) {
                assertFalse(shouldSuccessListenerBeCalled);
                semaphore.release();
            }
        };
    }

    @Override
    protected AuthorizedResourceClient construct(AuthorizationPreferencesProvider preferencesProvider,
                                                 ApiProvider apiProvider) {

        return new AuthorizedResourceClient(apiProvider, preferencesProvider);
    }

    private AuthorizedResourceClient getClient() {
        return new AuthorizedResourceClient(apiProvider, preferences);
    }

    public void testGetRequiresUrl() throws Exception {
        baseTestGetRequires(null, headers, parameters, listener);
    }

    public void testGetRequiresListener() throws Exception {
        baseTestGetRequires(url, headers, parameters, null);
    }

    public void testGetRequiresParameters() throws Exception {
        baseTestGetRequires(url, headers, null, listener);
    }

    public void testObtainAuthorizationRequiresNotNullClientId() throws Exception {
        baseTestGetRequires(url, headers, new DataParameters(null, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL), listener);
    }

    public void testObtainAuthorizationRequiresNotEmptyClientId() throws Exception {
        baseTestGetRequires(url, headers, new DataParameters("", TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL), listener);
    }

    public void testObtainAuthorizationRequiresNotNullClientSecret() throws Exception {
        baseTestGetRequires(url, headers, new DataParameters(TEST_CLIENT_ID, null, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL), listener);
    }

    public void testObtainAuthorizationRequiresNotEmptyClientSecret() throws Exception {
        baseTestGetRequires(url, headers, new DataParameters(TEST_CLIENT_ID, "", TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL), listener);
    }

    public void testObtainAuthorizationRequiresAuthorizationUrl() throws Exception {
        baseTestGetRequires(url, headers, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, null, TEST_TOKEN_URL, TEST_REDIRECT_URL), listener);
    }

    public void testObtainAuthorizationRequiresTokenUrl() throws Exception {
        baseTestGetRequires(url, headers, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, null, TEST_REDIRECT_URL), listener);
    }

    public void testObtainAuthorizationRequiresRedirectUrl() throws Exception {
        baseTestGetRequires(url, headers, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, null), listener);
    }

    public void testRequiresAuthorizationParameters() throws Exception {
        try {
            saveCredential();
            getClient().get(url, headers, listener);
            fail();
        } catch (AuthorizationException e) {
            // success
        }
    }

    public void testRequiresSavedCredential() throws Exception {
        shouldSuccessListenerBeCalled = false;
        shouldRequestBeSuccessful = false;
        savePreferences();
        getClient().get(url, headers, listener);
        semaphore.acquire();
    }

    public void testGetDoesNotRequiresHeaders() throws Exception {
        setupSuccessfulRequest(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        getClient().get(url, null, listener);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertNull(apiProvider.getApiRequests().get(0).getRequestHeaders());
    }

    public void testSuccessfulGet() throws Exception {
        setupSuccessfulRequest(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        apiProvider.setHttpRequestResults(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        getClient().get(url, new HashMap<String, Object>(headers), listener);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertEquals(headers, apiProvider.getApiRequests().get(0).getRequestHeaders());
    }

    public void testAddsHeaders() throws Exception {
        setupSuccessfulRequest(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        headers.put(TEST_HEADER_NAME, TEST_HEADER_VALUE);
        apiProvider.setHttpRequestResults(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        getClient().get(url, new HashMap<String, Object>(headers), listener);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertEquals(headers, apiProvider.getApiRequests().get(0).getRequestHeaders());
    }

    public void testFailedGet() throws Exception {
        setupFailedRequest();
        getClient().get(url, headers, listener);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
    }

    public void testFailedGet404() throws Exception {
        setupSuccessfulRequestWithFailedHttpStatus(404, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        getClient().get(url, headers, listener);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
    }

    public void testUnauthorized() throws Exception {
        setupFailedUnauthorized();
        getClient().get(url, headers, listener);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertCredential(null, apiProvider.getApiRequests().get(0));
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

    private void setupFailedUnauthorized() {
        savePreferences();
        saveCredential();
        shouldSuccessListenerBeCalled = false;
        shouldRequestBeSuccessful = false;
        shouldUnauthorizedListenerBeCalled = true;
        apiProvider.setShouldAuthorizedApiRequestBeSuccessful(shouldRequestBeSuccessful);
        apiProvider.setShouldAuthorizedApiRequestBeUnauthorized(shouldUnauthorizedListenerBeCalled);
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


    private void baseTestGetRequires(final URL url,
                                     final Map<String, Object> headers,
                                     DataParameters parameters,
                                     final AuthorizedResourceClient.Listener listener) throws Exception {
        try {
            if (parameters != null) {
                getClient().setParameters(parameters);
            }
            getClient().get(url, headers, listener);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        } catch (AuthorizationException e) {
            // success
        }
    }

}
