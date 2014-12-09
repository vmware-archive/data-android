/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

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

    private static final String URL = "http://example.com";

    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String IF_MATCH = "If-Match";
    private static final String ETAG = "Etag";

    private static final String TOKEN = UUID.randomUUID().toString();
    private static final String RESULT = UUID.randomUUID().toString();

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

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpGet.class));

        assertEquals(RESULT, client.get(TOKEN, URL));

        Mockito.verify(client).addAuthHeader(Mockito.any(HttpGet.class), Mockito.eq(TOKEN));
        Mockito.verify(client).addEtagHeader(Mockito.any(HttpGet.class), Mockito.eq(URL), Mockito.eq(IF_NONE_MATCH));
        Mockito.verify(client).execute(Mockito.any(HttpGet.class));
    }

    public void testDeleteAddsHeadersAndExecutes() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpDelete.class));

        assertEquals(RESULT, client.delete(TOKEN, URL));

        Mockito.verify(client).addAuthHeader(Mockito.any(HttpDelete.class), Mockito.eq(TOKEN));
        Mockito.verify(client).addEtagHeader(Mockito.any(HttpDelete.class), Mockito.eq(URL), Mockito.eq(IF_MATCH));
        Mockito.verify(client).execute(Mockito.any(HttpDelete.class));
    }

    public void testPutAddsHeadersAndExecutes() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));

        Mockito.doReturn(RESULT).when(client).execute(Mockito.any(HttpPut.class));

        assertEquals(RESULT, client.put(TOKEN, URL, RESULT));

        Mockito.verify(client).addAuthHeader(Mockito.any(HttpPut.class), Mockito.eq(TOKEN));
        Mockito.verify(client).addEtagHeader(Mockito.any(HttpPut.class), Mockito.eq(URL), Mockito.eq(IF_MATCH));
        Mockito.verify(client).execute(Mockito.any(HttpPut.class));
    }

    public void testPutAddsHeadersAndExecutesWithEmptyResultFromServer() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));

        Mockito.doReturn("").when(client).execute(Mockito.any(HttpPut.class));

        assertEquals(RESULT, client.put(TOKEN, URL, RESULT));

        Mockito.verify(client).addAuthHeader(Mockito.any(HttpPut.class), Mockito.eq(TOKEN));
        Mockito.verify(client).addEtagHeader(Mockito.any(HttpPut.class), Mockito.eq(URL), Mockito.eq(IF_MATCH));
        Mockito.verify(client).execute(Mockito.any(HttpPut.class));
    }

    public void testExecuteInvokesHttpClient() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final HttpClient httpClient = Mockito.mock(HttpClient.class);

        Mockito.when(httpRequest.getURI()).thenReturn(new URI(URL));
        Mockito.when(httpClient.execute(httpRequest)).thenReturn(httpResponse);

        Mockito.doReturn(httpClient).when(client).getHttpClient();
        Mockito.doReturn(RESULT).when(client).handleResponse(Mockito.eq(httpResponse), Mockito.eq(URL));

        assertEquals(RESULT, client.execute(httpRequest));

        Mockito.verify(httpRequest).getURI();
        Mockito.verify(httpClient).execute(httpRequest);
        Mockito.verify(client).handleResponse(Mockito.eq(httpResponse), Mockito.eq(URL));
    }

    public void testAddAuthHeaderAddsAccessToken() {
        final RemoteClient.Default client = new RemoteClient.Default(null);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        client.addAuthHeader(httpRequest, TOKEN);

        Mockito.verify(httpRequest).addHeader(Mockito.eq(RemoteClient.Default.Headers.AUTHORIZATION), Mockito.eq("Bearer " + TOKEN));
    }

    public void testAddAuthHeaderDoesNotAddNullAccessToken() {
        final RemoteClient.Default client = new RemoteClient.Default(null);
        final HttpUriRequest httpRequest = Mockito.mock(HttpUriRequest.class);

        client.addAuthHeader(httpRequest, null);

        Mockito.verify(httpRequest, Mockito.never()).addHeader(Mockito.anyString(), Mockito.anyString());
    }

    public void testAddEtagHeaderIfEtagsAreSupportedAndAvailable() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.etagSupport", "enabled");
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
        properties.setProperty("pivotal.data.etagSupport", "enabled");
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

    public void testHandleResponse() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final StatusLine httpStatusLine = Mockito.mock(StatusLine.class);

        Mockito.when(httpResponse.getStatusLine()).thenReturn(httpStatusLine);
        Mockito.doNothing().when(client).checkStatusLine(httpStatusLine);
        Mockito.doNothing().when(client).checkEtagHeader(httpResponse, URL);
        Mockito.doReturn(RESULT).when(client).getResponseBody(httpResponse);

        assertEquals(RESULT, client.handleResponse(httpResponse, URL));

        Mockito.verify(httpResponse).getStatusLine();
        Mockito.verify(client).checkStatusLine(httpStatusLine);
        Mockito.verify(client).checkEtagHeader(httpResponse, URL);
        Mockito.verify(client).getResponseBody(httpResponse);
    }


    public void testCheckStatusLineThrowsNotModifiedException() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final StatusLine httpStatusLine = Mockito.mock(StatusLine.class);

        final int statusCode = 304;

        Mockito.when(httpStatusLine.getStatusCode()).thenReturn(statusCode);
        Mockito.when(httpStatusLine.getReasonPhrase()).thenReturn(RESULT);

        try {
            client.checkStatusLine(httpStatusLine);
            fail();
        } catch (final NotModifiedException e) {
            assertEquals(statusCode, e.getStatusCode());
            assertEquals(RESULT, e.getMessage());
        }

        Mockito.verify(httpStatusLine).getStatusCode();
        Mockito.verify(httpStatusLine).getReasonPhrase();
    }

    public void testCheckStatusLineThrowsPreconditionFailedException() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final StatusLine httpStatusLine = Mockito.mock(StatusLine.class);

        final int statusCode = 412;

        Mockito.when(httpStatusLine.getStatusCode()).thenReturn(statusCode);
        Mockito.when(httpStatusLine.getReasonPhrase()).thenReturn(RESULT);

        try {
            client.checkStatusLine(httpStatusLine);
            fail();
        } catch (final PreconditionFailedException e) {
            assertEquals(statusCode, e.getStatusCode());
            assertEquals(RESULT, e.getMessage());
        }

        Mockito.verify(httpStatusLine).getStatusCode();
        Mockito.verify(httpStatusLine).getReasonPhrase();
    }

    public void testCheckStatusLineThrowsDataException() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final StatusLine httpStatusLine = Mockito.mock(StatusLine.class);

        final int statusCode = 500;

        Mockito.when(httpStatusLine.getStatusCode()).thenReturn(statusCode);
        Mockito.when(httpStatusLine.getReasonPhrase()).thenReturn(RESULT);

        try {
            client.checkStatusLine(httpStatusLine);
            fail();
        } catch (final DataException e) {
            assertEquals(statusCode, e.getStatusCode());
            assertEquals(RESULT, e.getMessage());
        }

        Mockito.verify(httpStatusLine).getStatusCode();
        Mockito.verify(httpStatusLine).getReasonPhrase();
    }

    public void testCheckStatusLineDoesNotThrowExceptionOnSuccess() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final StatusLine httpStatusLine = Mockito.mock(StatusLine.class);

        final int statusCode = 200;

        Mockito.when(httpStatusLine.getStatusCode()).thenReturn(statusCode);
        Mockito.when(httpStatusLine.getReasonPhrase()).thenReturn(RESULT);

        try {
            client.checkStatusLine(httpStatusLine);
        } catch (final Exception e) {
            fail();
        }

        Mockito.verify(httpStatusLine).getStatusCode();
        Mockito.verify(httpStatusLine).getReasonPhrase();
    }

    public void testCheckEtagHeaderIfEtagsAreSupportedAndPresent() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.etagSupport", "enabled");
        Pivotal.setProperties(properties);

        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(etagStore));
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        final Header httpHeader = Mockito.mock(Header.class);

        Mockito.when(httpResponse.getFirstHeader(ETAG)).thenReturn(httpHeader);
        Mockito.when(httpHeader.getValue()).thenReturn(RESULT);

        client.checkEtagHeader(httpResponse, URL);

        Mockito.verify(httpResponse).getFirstHeader(ETAG);
        Mockito.verify(httpHeader).getValue();
        Mockito.verify(etagStore).put(URL, RESULT);
    }

    public void testCheckEtagHeaderIfEtagsAreSupportedAndNotPresent() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.etagSupport", "enabled");
        Pivotal.setProperties(properties);

        final EtagStore etagStore = Mockito.mock(EtagStore.class);
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(etagStore));
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);

        Mockito.when(httpResponse.getFirstHeader(ETAG)).thenReturn(null);

        client.checkEtagHeader(httpResponse, URL);

        Mockito.verify(httpResponse).getFirstHeader(ETAG);
        Mockito.verify(etagStore).put(URL, "");
    }

    public void testCheckEtagHeaderIfEtagsAreNotSupported() throws Exception {
        final RemoteClient.Default client = Mockito.spy(new RemoteClient.Default(null));
        final HttpResponse httpResponse = Mockito.mock(HttpResponse.class);

        client.checkEtagHeader(httpResponse, URL);

        Mockito.verify(httpResponse, Mockito.never()).getFirstHeader(Mockito.anyString());
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
