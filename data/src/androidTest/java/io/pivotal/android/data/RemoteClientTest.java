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
import org.apache.http.entity.ByteArrayEntity;

public class RemoteClientTest extends AndroidTestCase {

    public void testGetAddsHeadersAndExecutes() throws Exception {
        final ClientLatch latch = new ClientLatch(1, 1, 1);
        final RemoteClient client = new TestRemoteClient(null) {

            @Override
            protected void addAuthHeader(final HttpUriRequest request, final String accessToken) {
                latch.addAuthHeader();
            }

            @Override
            protected void addEtagHeader(final HttpUriRequest request, final String url, final String header) {
                latch.addEtagHeader();
            }

            @Override
            public String execute(final HttpUriRequest request) throws Exception {
                latch.execute();
                return null;
            }
        };

        client.get(null, "");

        latch.assertComplete();
    }

    public void testDeleteAddsHeadersAndExecutes() throws Exception {
        final ClientLatch latch = new ClientLatch(1, 1, 1);
        final RemoteClient client = new TestRemoteClient(null) {

            @Override
            protected void addAuthHeader(final HttpUriRequest request, final String accessToken) {
                latch.addAuthHeader();
            }

            @Override
            protected void addEtagHeader(final HttpUriRequest request, final String url, final String header) {
                latch.addEtagHeader();
            }

            @Override
            public String execute(final HttpUriRequest request) throws Exception {
                latch.execute();
                return null;
            }
        };

        client.delete(null, "");

        latch.assertComplete();
    }

    public void testPutAddsHeadersAndExecutes() throws Exception {
        final ClientLatch latch = new ClientLatch(1, 1, 1);
        final RemoteClient client = new TestRemoteClient(null) {

            @Override
            protected void addAuthHeader(final HttpUriRequest request, final String accessToken) {
                latch.addAuthHeader();
            }

            @Override
            protected void addEtagHeader(final HttpUriRequest request, final String url, final String header) {
                latch.addEtagHeader();
            }

            @Override
            public String execute(final HttpUriRequest request) throws Exception {
                latch.execute();
                return null;
            }
        };

        client.put(null, "", "");

        latch.assertComplete();
    }

    public void testGetSucceeds() throws Exception {
        final FakeHttpResponse response = new FakeHttpResponse(200, "test");
        final TestRemoteClient client = new TestRemoteClient(response);
        final String result = client.execute(new HttpGet());

        assertEquals("test", result);
    }

    public void testDeleteSucceeds() throws Exception {
        final FakeHttpResponse response = new FakeHttpResponse(200, "test");
        final TestRemoteClient client = new TestRemoteClient(response);
        final String result = client.execute(new HttpDelete());

        assertEquals("test", result);
    }

    public void testPutSucceeds() throws Exception {
        final FakeHttpResponse response = new FakeHttpResponse(200, "test");
        final TestRemoteClient client = new TestRemoteClient(response);
        final String result = client.execute(new HttpPut());

        assertEquals("test", result);
    }

    public void testGetThrowsNotModifiedException() throws Exception {
        try {
            final FakeHttpResponse response = new FakeHttpResponse(304, null);
            final TestRemoteClient client = new TestRemoteClient(response);
            client.execute(new HttpGet());

            fail();
        } catch (final NotModifiedException e) {
            assertEquals(304, e.getStatusCode());
        }
    }

    public void testPutThrowsPreconditionFailedException() throws Exception {
        try {
            final FakeHttpResponse response = new FakeHttpResponse(412, null);
            final TestRemoteClient client = new TestRemoteClient(response);
            client.execute(new HttpPut());

            fail();
        } catch (final PreconditionFailedException e) {
            assertEquals(412, e.getStatusCode());
        }
    }

    public void testDeleteThrowsPreconditionFailedException() throws Exception {
        try {
            final FakeHttpResponse response = new FakeHttpResponse(412, null);
            final TestRemoteClient client = new TestRemoteClient(response);
            client.execute(new HttpDelete());

            fail();
        } catch (final PreconditionFailedException e) {
            assertEquals(412, e.getStatusCode());
        }
    }



    // ===================================================



    public void testGetSucceedsWith200() throws Exception {
        final RemoteClient client = new TestRemoteClient(new FakeHttpResponse(200, "test"));
        final String result = client.get("ACCESS_TOKEN", "http://example.com");

        assertEquals("test", result);
    }

    public void testGetFailsWith304() throws Exception {
        try {
            final RemoteClient client = new TestRemoteClient(new FakeHttpResponse(304, null));
            client.get("ACCESS_TOKEN", "http://example.com");
            fail();
        } catch (final NotModifiedException e) {
            assertEquals(304, e.getStatusCode());
        }
    }

    public void testGetFailsWith400() throws Exception {
        try {
            final RemoteClient client = new TestRemoteClient(new FakeHttpResponse(400, null));
            client.get("ACCESS_TOKEN", "http://example.com");
            fail();
        } catch (final DataException e) {
            assertEquals(400, e.getStatusCode());
        }
    }

    public void testDeleteSucceedsWith200() throws Exception {
        final RemoteClient client = new TestRemoteClient(new FakeHttpResponse(200, "test"));
        final String result = client.delete("ACCESS_TOKEN", "http://example.com");

        assertEquals("test", result);
    }

    public void testDeleteFailsWith412() throws Exception {
        try {
            final RemoteClient client = new TestRemoteClient(new FakeHttpResponse(412, null));
            client.delete("ACCESS_TOKEN", "http://example.com");
            fail();
        } catch (final PreconditionFailedException e) {
            assertEquals(412, e.getStatusCode());
        }
    }

    public void testDeleteFailsWith400() throws Exception {
        try {
            final RemoteClient client = new TestRemoteClient(new FakeHttpResponse(400, null));
            client.delete("ACCESS_TOKEN", "http://example.com");
            fail();
        } catch (final DataException e) {
            assertEquals(400, e.getStatusCode());
        }
    }

    public void testPutSucceedsWith200() throws Exception {
        final RemoteClient client = new TestRemoteClient(new FakeHttpResponse(200, "test"));
        final String result = client.put("ACCESS_TOKEN", "http://example.com", "test1");

        assertEquals("test", result);
    }

    public void testPutSucceedsWith200WithoutBodyFromServer() throws Exception {
        final RemoteClient client = new TestRemoteClient(new FakeHttpResponse(200, ""));
        final String result = client.put("ACCESS_TOKEN", "http://example.com", "test1");

        assertEquals("test1", result);
    }

    public void testPutFailsWith412() throws Exception {
        try {
            final RemoteClient client = new TestRemoteClient(new FakeHttpResponse(412, null));
            client.put("ACCESS_TOKEN", "http://example.com", "test");
            fail();
        } catch (final PreconditionFailedException e) {
            assertEquals(412, e.getStatusCode());
        }
    }

    public void testPutFailsWith400() throws Exception {
        try {
            final RemoteClient client = new TestRemoteClient(new FakeHttpResponse(400, null));
            client.put("ACCESS_TOKEN", "http://example.com", "test");
            fail();
        } catch (final DataException e) {
            assertEquals(400, e.getStatusCode());
        }
    }


    // ===================================================



    private static class TestRemoteClient extends RemoteClient.Default {

        private final HttpResponse mResponse;

        public TestRemoteClient(final HttpResponse response) {
            super(null);
            mResponse = response;
        }

        @Override
        protected HttpClient getHttpClient() {
            return new MockHttpClient() {

                @Override
                public HttpResponse execute(final HttpUriRequest httpUriRequest) {
                    return mResponse;
                }
            };
        }
    }

    private static class FakeHttpResponse extends MockHttpResponse {

        private final int mCode;
        private final String mBody;

        public FakeHttpResponse(final int code, final String body) {
            mCode = code;
            mBody = body;
        }

        @Override
        public StatusLine getStatusLine() {
            return new FakeStatusLine(mCode);
        }

        @Override
        public Header getFirstHeader(final String s) {
            return null;
        }

        @Override
        public HttpEntity getEntity() {
            return new ByteArrayEntity(mBody.getBytes());
        }
    }

    private static class FakeStatusLine extends MockStatusLine {

        private final int mCode;

        public FakeStatusLine(final int code) {
            mCode = code;
        }

        @Override
        public int getStatusCode() {
            return mCode;
        }

        @Override
        public String getReasonPhrase() {
            return null;
        }
    }

    private class ClientLatch {

        private final AssertionLatch mLatch1, mLatch2, mLatch3;

        public ClientLatch(final int count1, final int count2, final int count3) {
            mLatch1 = new AssertionLatch(count1);
            mLatch2 = new AssertionLatch(count2);
            mLatch3 = new AssertionLatch(count3);
        }

        public void addAuthHeader() {
            mLatch1.countDown();
        }

        public void addEtagHeader() {
            mLatch2.countDown();
        }

        public void execute() {
            mLatch3.countDown();
        }

        public void assertComplete() {
            mLatch1.assertComplete();
            mLatch2.assertComplete();
            mLatch3.assertComplete();
        }
    }
}
