package com.pivotal.cf.mobile.datasdk.client;

import android.content.Context;

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
        assertEquals(1, apiProvider.getApiRequests().size());
        assertNull(apiProvider.getApiRequests().get(0).getRequestHeaders());
    }

    public void testSuccessfulGet() throws Exception {
        setupSuccessfulRequest(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        apiProvider.setHttpRequestResults(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        getClient().get(url, new HashMap<String, Object>(headers), parameters, listener);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertEquals(headers, apiProvider.getApiRequests().get(0).getRequestHeaders());
    }

    public void testAddsHeaders() throws Exception {
        setupSuccessfulRequest(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        headers.put(TEST_HEADER_NAME, TEST_HEADER_VALUE);
        apiProvider.setHttpRequestResults(TEST_HTTP_STATUS_CODE, TEST_CONTENT_TYPE, TEST_CONTENT_DATA);
        getClient().get(url, new HashMap<String, Object>(headers), parameters, listener);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertEquals(headers, apiProvider.getApiRequests().get(0).getRequestHeaders());
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
                                     final Map<String, Object> headers,
                                     DataParameters parameters,
                                     final AuthorizedResourceClient.Listener listener) throws Exception {
        try {
            getClient().get(url, headers, parameters, listener);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
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
