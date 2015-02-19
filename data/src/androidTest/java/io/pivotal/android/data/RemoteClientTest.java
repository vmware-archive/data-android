/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
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
import java.util.Random;
import java.util.UUID;

public class RemoteClientTest extends AndroidTestCase {

    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String IF_MATCH = "If-Match";
    private static final String ETAG = "Etag";

    private static final String TOKEN = UUID.randomUUID().toString();
    private static final String RESULT = UUID.randomUUID().toString();
    private static final byte[] DATA = UUID.randomUUID().toString().getBytes();
    private static final String URL = "http://" + UUID.randomUUID().toString() + ".com";
    private static final boolean FORCE = new Random().nextBoolean();

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

    public void testGetCallsExecuteWithRequest() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null, null));

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpGet.class), Mockito.anyBoolean());

        assertEquals(RESULT, client.get(URL, FORCE));

        Mockito.verify(client).execute(Mockito.isA(HttpGet.class), Mockito.eq(FORCE));
    }

    public void testPutCallsExecuteWithRequest() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null, null));

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpPut.class), Mockito.anyBoolean());

        assertEquals(RESULT, client.put(URL, DATA, FORCE));

        Mockito.verify(client).execute(Mockito.isA(HttpPut.class), Mockito.eq(FORCE));
    }

    public void testPutCallsExecuteWithRequestAndEmptyResult() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null, null));

        Mockito.doReturn("").when(client).execute(Mockito.any(HttpPut.class), Mockito.anyBoolean());

        assertEquals(new String(DATA), client.put(URL, DATA, FORCE));

        Mockito.verify(client).execute(Mockito.isA(HttpPut.class), Mockito.eq(FORCE));
    }

    public void testDeleteCallsExecuteWithRequest() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null, null));

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpDelete.class), Mockito.anyBoolean());

        assertEquals(RESULT, client.delete(URL, FORCE));

        Mockito.verify(client).execute(Mockito.isA(HttpDelete.class), Mockito.eq(FORCE));
    }

    public void testExecuteAddsHeadersAndHandlesResponse() throws Exception {
        final HttpUriRequest request = Mockito.mock(HttpUriRequest.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null, null));
        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final StatusLine statusLine = Mockito.mock(StatusLine.class);

        Mockito.when(request.getURI()).thenReturn(new URI(URL));
        Mockito.doReturn(httpClient).when(client).getHttpClient();
        Mockito.when(httpClient.execute(Mockito.any(HttpUriRequest.class))).thenReturn(httpResponse);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
        Mockito.doNothing().when(client).addHeaders(Mockito.any(HttpUriRequest.class), Mockito.anyBoolean());
        Mockito.doReturn(RESULT).when(client).handleResponse(Mockito.any(HttpResponse.class), Mockito.anyString());

        assertEquals(RESULT, client.execute(request, FORCE));

        Mockito.verify(client).getHttpClient();
        Mockito.verify(httpClient).execute(request);
        Mockito.verify(httpResponse).getStatusLine();
        Mockito.verify(statusLine).getStatusCode();
        Mockito.verify(client).addHeaders(request, FORCE);
        Mockito.verify(client).handleResponse(httpResponse, URL);
        Mockito.verify(client, Mockito.never()).invalidateAccessToken();
    }

    public void testExecuteAddsHeadersAndHandles401Response() throws Exception {
        final HttpUriRequest request = Mockito.mock(HttpUriRequest.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null, null));
        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final StatusLine statusLine = Mockito.mock(StatusLine.class);

        Mockito.when(request.getURI()).thenReturn(new URI(URL));
        Mockito.doReturn(httpClient).when(client).getHttpClient();
        Mockito.when(httpClient.execute(Mockito.any(HttpUriRequest.class))).thenReturn(httpResponse);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        Mockito.when(statusLine.getStatusCode()).thenReturn(401);
        Mockito.doNothing().when(client).invalidateAccessToken();
        Mockito.doNothing().when(client).addHeaders(Mockito.any(HttpUriRequest.class), Mockito.anyBoolean());
        Mockito.doNothing().when(client).addAuthHeader(Mockito.any(HttpUriRequest.class));
        Mockito.doReturn(RESULT).when(client).handleResponse(Mockito.any(HttpResponse.class), Mockito.anyString());

        assertEquals(RESULT, client.execute(request, FORCE));

        Mockito.verify(client).getHttpClient();
        Mockito.verify(httpClient, Mockito.times(2)).execute(request);
        Mockito.verify(httpResponse).getStatusLine();
        Mockito.verify(statusLine).getStatusCode();
        Mockito.verify(client).addHeaders(request, FORCE);
        Mockito.verify(client).addAuthHeader(request);
        Mockito.verify(client).handleResponse(httpResponse, URL);
        Mockito.verify(client).invalidateAccessToken();
    }

    public void testAddHeaders() throws Exception {
        final HttpUriRequest request = Mockito.mock(HttpUriRequest.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null, null));

        Mockito.when(request.getURI()).thenReturn(new URI(URL));
        Mockito.doReturn(TOKEN).when(client).provideAccessToken();

        client.addHeaders(request, FORCE);

        Mockito.verify(client).addUserAgentHeader(Mockito.any(HttpGet.class));
        Mockito.verify(client).addAuthHeader(Mockito.any(HttpGet.class));

        if (!FORCE) {
            Mockito.verify(client).addEtagHeader(Mockito.any(HttpUriRequest.class), Mockito.eq(URL));
        }
    }

    public void testAddUserAgentHeaderAddsUserAgent() {
        final RemoteClient.Default client = new RemoteClient.Default(null, null);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        client.addUserAgentHeader(httpRequest);

        final String sdkVersion = String.format("PCFData/%s;", BuildConfig.SDK_VERSION);
        final String androidVersion = String.format("Android Version %s (Build %s)", Build.VERSION.RELEASE, Build.ID);
        Mockito.verify(httpRequest).addHeader(RemoteClient.Default.Headers.USER_AGENT, sdkVersion + " " + androidVersion);
    }

    public void testAddAuthHeaderAddsAccessToken() {
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null, null));

        Mockito.doReturn(TOKEN).when(client).provideAccessToken();

        client.addAuthHeader(httpRequest);

        Mockito.verify(httpRequest).addHeader(RemoteClient.Default.Headers.AUTHORIZATION, "Bearer " + TOKEN);
    }

    public void testAddAuthHeaderThrowsExceptionIfAccessTokenIsNull() {
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null, null));

        Mockito.doReturn(null).when(client).provideAccessToken();

        try {
            client.addAuthHeader(httpRequest);
            fail();
        } catch (IllegalStateException e) {
            assertNotNull(e);
        }

        Mockito.verify(httpRequest, Mockito.never()).addHeader(Mockito.anyString(), Mockito.anyString());
    }

    public void testAddIfMatchEtagHeaderIfEtagsAreSupportedAndAvailable() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.collisionStrategy", "OptimisticLocking");
        Pivotal.setProperties(properties);

        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final Context context = Mockito.mock(Context.class);
        final RemoteClient.Default client = new RemoteClient.Default(context, etagStore);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        Mockito.when(etagStore.get(URL)).thenReturn(RESULT);

        client.addEtagHeader(httpRequest, URL);

        Mockito.verify(httpRequest).addHeader(IF_MATCH, RESULT);
    }

    public void testAddIfNonMatchEtagHeaderIfEtagsAreSupportedAndAvailable() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.collisionStrategy", "OptimisticLocking");
        Pivotal.setProperties(properties);

        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final Context context = Mockito.mock(Context.class);
        final RemoteClient.Default client = new RemoteClient.Default(context, etagStore);
        final HttpGet httpRequest = Mockito.mock(HttpGet.class);

        Mockito.when(etagStore.get(URL)).thenReturn(RESULT);

        client.addEtagHeader(httpRequest, URL);

        Mockito.verify(httpRequest).addHeader(IF_NONE_MATCH, RESULT);
    }

    public void testAddEtagHeaderIfEtagsAreSupportedAndNotAvailable() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.collisionStrategy", "OptimisticLocking");
        Pivotal.setProperties(properties);

        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final Context context = Mockito.mock(Context.class);
        final RemoteClient.Default client = new RemoteClient.Default(context, etagStore);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        Mockito.when(etagStore.get(URL)).thenReturn(null);

        client.addEtagHeader(httpRequest, URL);

        Mockito.verify(httpRequest, Mockito.never()).addHeader(Mockito.anyString(), Mockito.anyString());
    }

    public void testAddEtagHeaderIfEtagsAreNotSupported() {
        final RemoteClient.Default client = new RemoteClient.Default(null, null);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        client.addEtagHeader(httpRequest, URL);

        Mockito.verify(httpRequest, Mockito.never()).addHeader(Mockito.anyString(), Mockito.anyString());
    }

    public void testGetHttpClientIsCreatedWithAppropriateTimeouts() {
        final RemoteClient.Default client = new RemoteClient.Default(null, null);
        final HttpClient httpClient = client.getHttpClient();
        final HttpParams params = httpClient.getParams();

        assertEquals(RemoteClient.Default.Timeouts.CONNECTION, HttpConnectionParams.getConnectionTimeout(params));
        assertEquals(RemoteClient.Default.Timeouts.SOCKET, HttpConnectionParams.getSoTimeout(params));
    }

    public void testHandleResponseWithSuccessStatusCodeAndEtagsDisabled() throws Exception {
        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final Context context = Mockito.mock(Context.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(context, etagStore));
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
        final Context context = Mockito.mock(Context.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(context, etagStore));
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
        final Context context = Mockito.mock(Context.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(context, etagStore));
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
        final Context context = Mockito.mock(Context.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(context, etagStore));
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
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null, null));
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
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null, null));
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

    public void testGetAccessTokenWithNoProvider() {
        TokenProviderFactory.registerTokenProvider(null);

        final RemoteClient.Default client = new RemoteClient.Default(null, null);

        assertNull(client.provideAccessToken());
    }

    public void testGetAccessTokenWithContext() {
        final TokenProvider provider = Mockito.mock(TokenProvider.class);
        TokenProviderFactory.registerTokenProvider(provider);

        final RemoteClient.Default client = new RemoteClient.Default(mContext, null);

        Mockito.when(provider.provideAccessToken(Mockito.any(Context.class))).thenReturn(TOKEN);

        assertEquals(TOKEN, client.provideAccessToken());

        Mockito.verify(provider).provideAccessToken(mContext);
    }
}
