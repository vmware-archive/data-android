/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.os.Build;
import android.test.AndroidTestCase;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.UUID;

public class RemoteClientTest extends AndroidTestCase {

    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String IF_MATCH = "If-Match";
    private static final String ETAG = "Etag";

    private static final String TOKEN = UUID.randomUUID().toString();
    private static final String COLLECTION = UUID.randomUUID().toString();
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final String RESULT = UUID.randomUUID().toString();
    private static final String URL = "http://" + UUID.randomUUID().toString() + ".com";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        Pivotal.setProperties(null);
    }

    public void testGetAddsHeadersAndExecutes() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final KeyValue keyValue = new KeyValue(COLLECTION, KEY, VALUE);
        final Request<KeyValue> request = new Request<KeyValue>(TOKEN, keyValue);

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpGet.class));

        assertEquals(RESULT, ((KeyValue)client.get(request).object).value);

        Mockito.verify(client).addUserAgentHeader(Mockito.any(HttpGet.class));
        Mockito.verify(client).addAuthHeader(Mockito.any(HttpGet.class), Mockito.eq(TOKEN));
        Mockito.verify(client).addEtagHeader(Mockito.any(HttpGet.class), Mockito.eq(keyValue.getUrl()), Mockito.eq(IF_NONE_MATCH));
        Mockito.verify(client).execute(Mockito.any(HttpGet.class));
    }

    public void testForceGetDoesntAddEtagsAndExecutes() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final KeyValue keyValue = new KeyValue(COLLECTION, KEY, VALUE);
        final Request<KeyValue> request = new Request<KeyValue>(TOKEN, keyValue);
        request.force = true;

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpGet.class));

        assertEquals(RESULT, ((KeyValue)client.get(request).object).value);

        Mockito.verify(client).addUserAgentHeader(Mockito.any(HttpGet.class));
        Mockito.verify(client).addAuthHeader(Mockito.any(HttpGet.class), Mockito.eq(TOKEN));
        Mockito.verify(client, Mockito.never()).addEtagHeader(Mockito.any(HttpGet.class), Mockito.eq(keyValue.getUrl()), Mockito.eq(IF_NONE_MATCH));
        Mockito.verify(client).execute(Mockito.any(HttpGet.class));
    }

    public void testPutAddsHeadersAndExecutes() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final KeyValue keyValue = new KeyValue(COLLECTION, KEY, VALUE);
        final Request<KeyValue> request = new Request<KeyValue>(TOKEN, keyValue);

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpPut.class));

        assertEquals(RESULT, ((KeyValue)client.put(request).object).value);

        Mockito.verify(client).addUserAgentHeader(Mockito.any(HttpPut.class));
        Mockito.verify(client).addAuthHeader(Mockito.any(HttpPut.class), Mockito.eq(TOKEN));
        Mockito.verify(client).addEtagHeader(Mockito.any(HttpPut.class), Mockito.eq(keyValue.getUrl()), Mockito.eq(IF_MATCH));
        Mockito.verify(client).execute(Mockito.any(HttpPut.class));
    }

    public void testForcePutDoesntAddEtagsAndExecutes() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final KeyValue keyValue = new KeyValue(COLLECTION, KEY, VALUE);
        final Request<KeyValue> request = new Request<KeyValue>(TOKEN, keyValue);
        request.force = true;

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpPut.class));

        assertEquals(RESULT, ((KeyValue)client.put(request).object).value);

        Mockito.verify(client).addUserAgentHeader(Mockito.any(HttpPut.class));
        Mockito.verify(client).addAuthHeader(Mockito.any(HttpPut.class), Mockito.eq(TOKEN));
        Mockito.verify(client, Mockito.never()).addEtagHeader(Mockito.any(HttpPut.class), Mockito.eq(keyValue.getUrl()), Mockito.eq(IF_NONE_MATCH));
        Mockito.verify(client).execute(Mockito.any(HttpPut.class));
    }

    public void testPutAddsHeadersAndExecutesWithEmptyResultFromServer() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final KeyValue keyValue = new KeyValue(COLLECTION, KEY, VALUE);
        final Request<KeyValue> request = new Request<KeyValue>(TOKEN, keyValue);

        Mockito.doReturn("").when(client).execute(Mockito.any(HttpPut.class));

        assertEquals(VALUE, ((KeyValue)client.put(request).object).value);

        Mockito.verify(client).addUserAgentHeader(Mockito.any(HttpPut.class));
        Mockito.verify(client).addAuthHeader(Mockito.any(HttpPut.class), Mockito.eq(TOKEN));
        Mockito.verify(client).addEtagHeader(Mockito.any(HttpPut.class), Mockito.eq(keyValue.getUrl()), Mockito.eq(IF_MATCH));
        Mockito.verify(client).execute(Mockito.any(HttpPut.class));
    }

    public void testDeleteAddsHeadersAndExecutes() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final KeyValue keyValue = new KeyValue(COLLECTION, KEY, VALUE);
        final Request<KeyValue> request = new Request<KeyValue>(TOKEN, keyValue);

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpDelete.class));

        assertEquals(RESULT, ((KeyValue)client.delete(request).object).value);

        Mockito.verify(client).addUserAgentHeader(Mockito.any(HttpDelete.class));
        Mockito.verify(client).addAuthHeader(Mockito.any(HttpDelete.class), Mockito.eq(TOKEN));
        Mockito.verify(client).addEtagHeader(Mockito.any(HttpDelete.class), Mockito.eq(keyValue.getUrl()), Mockito.eq(IF_MATCH));
        Mockito.verify(client).execute(Mockito.any(HttpDelete.class));
    }

    public void testForceDeleteDoesntAddEtagsAndExecutes() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final KeyValue keyValue = new KeyValue(COLLECTION, KEY, VALUE);
        final Request<KeyValue> request = new Request<KeyValue>(TOKEN, keyValue);
        request.force = true;

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpDelete.class));

        assertEquals(RESULT, ((KeyValue)client.delete(request).object).value);

        Mockito.verify(client).addUserAgentHeader(Mockito.any(HttpDelete.class));
        Mockito.verify(client).addAuthHeader(Mockito.any(HttpDelete.class), Mockito.eq(TOKEN));
        Mockito.verify(client, Mockito.never()).addEtagHeader(Mockito.any(HttpDelete.class), Mockito.eq(keyValue.getUrl()), Mockito.eq(IF_MATCH));
        Mockito.verify(client).execute(Mockito.any(HttpDelete.class));
    }

    public void testExecuteInvokesHttpClient() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final HttpClient httpClient = Mockito.mock(HttpClient.class);

        Mockito.when(httpRequest.getURI()).thenReturn(new URI(URL));
        Mockito.when(httpClient.execute(httpRequest)).thenReturn(httpResponse);

        Mockito.doReturn(httpClient).when(client).getHttpClient();
        Mockito.doReturn(RESULT).when(client).handleResponse(httpResponse, URL);

        assertEquals(RESULT, client.execute(httpRequest));

        Mockito.verify(httpRequest).getURI();
        Mockito.verify(httpClient).execute(httpRequest);
        Mockito.verify(client).handleResponse(httpResponse, URL);
    }

    public void testAddUserAgentHeaderAddsUserAgent() {
        final RemoteClient.Default client = new RemoteClient.Default(null);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        client.addUserAgentHeader(httpRequest);

        final String sdkVersion = String.format("PCFData/%s", BuildConfig.SDK_VERSION);
        final String androidVersion = String.format("[Android Version %s (Build %s)]", Build.VERSION.RELEASE, Build.ID);
        Mockito.verify(httpRequest).addHeader(RemoteClient.Default.Headers.USER_AGENT, sdkVersion + " " + androidVersion);
    }

    public void testAddAuthHeaderAddsAccessToken() {
        final RemoteClient.Default client = new RemoteClient.Default(null);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        client.addAuthHeader(httpRequest, TOKEN);

        Mockito.verify(httpRequest).addHeader(RemoteClient.Default.Headers.AUTHORIZATION, "Bearer " + TOKEN);
    }

    public void testAddAuthHeaderDoesNotAddNullAccessToken() {
        final RemoteClient.Default client = new RemoteClient.Default(null);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        client.addAuthHeader(httpRequest, null);

        Mockito.verify(httpRequest, Mockito.never()).addHeader(Mockito.anyString(), Mockito.anyString());
    }

    public void testAddEtagHeaderIfEtagsAreSupportedAndAvailable() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.collisionStrategy", "OptimisticLocking");
        Pivotal.setProperties(properties);

        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final RemoteClient.Default client = new RemoteClient.Default(etagStore);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        Mockito.when(etagStore.get(URL)).thenReturn(RESULT);

        client.addEtagHeader(httpRequest, URL, IF_MATCH);

        Mockito.verify(httpRequest).addHeader(IF_MATCH, RESULT);
    }

    public void testAddEtagHeaderIfEtagsAreSupportedAndNotAvailable() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.collisionStrategy", "OptimisticLocking");
        Pivotal.setProperties(properties);

        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final RemoteClient.Default client = new RemoteClient.Default(etagStore);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        Mockito.when(etagStore.get(URL)).thenReturn(null);

        client.addEtagHeader(httpRequest, URL, IF_MATCH);

        Mockito.verify(httpRequest, Mockito.never()).addHeader(Mockito.anyString(), Mockito.anyString());
    }

    public void testAddEtagHeaderIfEtagsAreNotSupported() {
        final RemoteClient.Default client = new RemoteClient.Default(null);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        client.addEtagHeader(httpRequest, URL, IF_MATCH);

        Mockito.verify(httpRequest, Mockito.never()).addHeader(Mockito.anyString(), Mockito.anyString());
    }

    public void testGetHttpClientIsCreatedWithAppropriateTimeouts() {
        final RemoteClient.Default client = new RemoteClient.Default(null);
        final HttpClient httpClient = client.getHttpClient();
        final HttpParams params = httpClient.getParams();

        assertEquals(RemoteClient.Default.Timeouts.CONNECTION, HttpConnectionParams.getConnectionTimeout(params));
        assertEquals(RemoteClient.Default.Timeouts.SOCKET, HttpConnectionParams.getSoTimeout(params));
    }

    public void testHandleResponseWithSuccessStatusCodeAndEtagsDisabled() throws Exception {
        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(etagStore));
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final StatusLine httpStatusLine = Mockito.mock(StatusLine.class);

        Mockito.when(httpResponse.getStatusLine()).thenReturn(httpStatusLine);
        Mockito.when(httpStatusLine.getStatusCode()).thenReturn(200);
        Mockito.doReturn(RESULT).when(client).getResponseBody(httpResponse);

        assertEquals(RESULT, client.handleResponse(httpResponse, URL));

        Mockito.verify(httpResponse).getStatusLine();
        Mockito.verify(etagStore, Mockito.never()).put(URL, RESULT);
        Mockito.verify(client).getResponseBody(httpResponse);
    }

    public void testHandleResponseWithSuccessStatusCodeAndEtagsEnabled() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.collisionStrategy", "OptimisticLocking");
        Pivotal.setProperties(properties);

        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(etagStore));
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final StatusLine httpStatusLine = Mockito.mock(StatusLine.class);
        final Header httpHeader = Mockito.mock(Header.class);

        Mockito.when(httpResponse.getStatusLine()).thenReturn(httpStatusLine);
        Mockito.when(httpStatusLine.getStatusCode()).thenReturn(200);
        Mockito.when(httpResponse.getFirstHeader(ETAG)).thenReturn(httpHeader);
        Mockito.when(httpHeader.getValue()).thenReturn(RESULT);
        Mockito.doReturn(RESULT).when(client).getResponseBody(httpResponse);

        assertEquals(RESULT, client.handleResponse(httpResponse, URL));

        Mockito.verify(httpResponse).getStatusLine();
        Mockito.verify(httpResponse).getFirstHeader(ETAG);
        Mockito.verify(httpHeader).getValue();
        Mockito.verify(etagStore).put(URL, RESULT);
        Mockito.verify(client).getResponseBody(httpResponse);
    }

    public void testHandleResponseWithNotFoundStatusCodeAndEtagsDisabled() throws Exception {
        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(etagStore));
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final StatusLine httpStatusLine = Mockito.mock(StatusLine.class);

        Mockito.when(httpResponse.getStatusLine()).thenReturn(httpStatusLine);
        Mockito.when(httpStatusLine.getStatusCode()).thenReturn(404);
        Mockito.doReturn(RESULT).when(client).getResponseBody(httpResponse);

        try {
            client.handleResponse(httpResponse, URL);
        } catch (final DataHttpException e) {
            assertEquals(404, e.getStatusCode());
        }

        Mockito.verify(httpResponse).getStatusLine();
        Mockito.verify(etagStore, Mockito.never()).put(URL, "");
    }

    public void testHandleResponseWithNotFoundStatusCodeAndEtagsEnabled() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.collisionStrategy", "OptimisticLocking");
        Pivotal.setProperties(properties);

        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(etagStore));
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final StatusLine httpStatusLine = Mockito.mock(StatusLine.class);

        Mockito.when(httpResponse.getStatusLine()).thenReturn(httpStatusLine);
        Mockito.when(httpStatusLine.getStatusCode()).thenReturn(404);
        Mockito.doReturn(RESULT).when(client).getResponseBody(httpResponse);

        try {
            client.handleResponse(httpResponse, URL);
        } catch (final DataHttpException e) {
            assertEquals(404, e.getStatusCode());
        }

        Mockito.verify(httpResponse).getStatusLine();
        Mockito.verify(etagStore).put(URL, "");
    }

    public void testGetResponseBodyReturnsCorrectResult() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final HttpEntity httpEntity = Mockito.mock(HttpEntity.class);
        final InputStream inputStream = new ByteArrayInputStream(RESULT.getBytes());

        Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
        Mockito.when(httpEntity.getContent()).thenReturn(inputStream);

        assertEquals(RESULT, client.getResponseBody(httpResponse));

        Mockito.verify(httpResponse).getEntity();
        Mockito.verify(httpEntity).getContent();
    }

    public void testGetResponseBodyClosesInputStream() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final HttpEntity httpEntity = Mockito.mock(HttpEntity.class);
        final InputStream inputStream = Mockito.mock(InputStream.class);

        Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
        Mockito.when(httpEntity.getContent()).thenReturn(inputStream);

        client.getResponseBody(httpResponse);

        Mockito.verify(httpResponse).getEntity();
        Mockito.verify(httpEntity).getContent();
        Mockito.verify(inputStream).close();
    }
}
