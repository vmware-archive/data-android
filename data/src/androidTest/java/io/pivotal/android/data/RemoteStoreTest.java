/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RemoteStoreTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final String TOKEN = UUID.randomUUID().toString();

    public void testContainsInvokesGetWithSuccess() {
        final AssertionLatch latch = new AssertionLatch(1);
        final RemoteStore store = new RemoteStore(new TestContext(), null) {

            @Override
            public Response get(final String accessToken, final String key) {
                latch.countDown();
                return Response.success(KEY, VALUE);
            }
        };

        assertTrue(store.contains(TOKEN, KEY));

        latch.assertComplete();
    }

    public void testContainsInvokesGetWithFailure() {
        final AssertionLatch latch = new AssertionLatch(1);
        final RemoteStore store = new RemoteStore(new TestContext(), null) {

            @Override
            public Response get(final String accessToken, final String key) {
                latch.countDown();
                return Response.failure(KEY, null);
            }
        };

        assertFalse(store.contains(TOKEN, KEY));

        latch.assertComplete();
    }

    public void testGetInvokesRemoteClientAndObserverHandlerWithSuccessResponse() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);

        final RemoteStore store = new RemoteStore(null, null) {
            @Override
            protected RemoteClient createRemoteClient(final Context context) {
                return new MockRemoteClient() {

                    @Override
                    public String get(final String accessToken, final String url) throws Exception {
                        latch1.countDown();
                        return VALUE;
                    }
                };
            }

            @Override
            protected ObserverHandler createObserverHandler(final Set<DataStore.Observer> observers, final Object lock) {
                return new TestObserverHandler() {

                    @Override
                    public void postResponse(final DataStore.Response response) {
                        latch2.countDown();
                    }
                };
            }
        };

        final DataStore.Response response = store.get(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        latch1.assertComplete();
        latch2.assertComplete();
    }

    public void testGetInvokesRemoteClientAndObserverHandlerWithFailureResponse() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);

        final RemoteStore store = new RemoteStore(null, null) {
            @Override
            protected RemoteClient createRemoteClient(final Context context) {
                return new MockRemoteClient() {

                    @Override
                    public String get(final String accessToken, final String url) throws Exception {
                        latch1.countDown();
                        throw new RuntimeException();
                    }
                };
            }

            @Override
            protected ObserverHandler createObserverHandler(final Set<DataStore.Observer> observers, final Object lock) {
                return new TestObserverHandler() {

                    @Override
                    public void postResponse(final DataStore.Response response) {
                        latch2.countDown();
                    }
                };
            }
        };

        final DataStore.Response response = store.get(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.FAILURE, response.status);
        assertEquals(KEY, response.key);
        assertNotNull(response.error);

        latch1.assertComplete();
        latch2.assertComplete();
    }

    public void testPutInvokesRemoteClientAndObserverHandlerWithSuccessResponse() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);

        final RemoteStore store = new RemoteStore(null, null) {
            @Override
            protected RemoteClient createRemoteClient(final Context context) {
                return new MockRemoteClient() {

                    @Override
                    public String put(final String accessToken, final String url, final String value) throws Exception {
                        latch1.countDown();
                        return VALUE;
                    }
                };
            }

            @Override
            protected ObserverHandler createObserverHandler(final Set<DataStore.Observer> observers, final Object lock) {
                return new TestObserverHandler() {

                    @Override
                    public void postResponse(final DataStore.Response response) {
                        latch2.countDown();
                    }
                };
            }
        };

        final DataStore.Response response = store.put(TOKEN, KEY, VALUE);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        latch1.assertComplete();
        latch2.assertComplete();
    }

    public void testPutInvokesRemoteClientAndObserverHandlerWithFailureResponse() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);

        final RemoteStore store = new RemoteStore(null, null) {
            @Override
            protected RemoteClient createRemoteClient(final Context context) {
                return new MockRemoteClient() {

                    @Override
                    public String put(final String accessToken, final String url, final String value) throws Exception {
                        latch1.countDown();
                        throw new RuntimeException();
                    }
                };
            }

            @Override
            protected ObserverHandler createObserverHandler(final Set<DataStore.Observer> observers, final Object lock) {
                return new TestObserverHandler() {

                    @Override
                    public void postResponse(final DataStore.Response response) {
                        latch2.countDown();
                    }
                };
            }
        };

        final DataStore.Response response = store.put(TOKEN, KEY, VALUE);

        assertEquals(DataStore.Response.Status.FAILURE, response.status);
        assertEquals(KEY, response.key);
        assertNotNull(response.error);

        latch1.assertComplete();
        latch2.assertComplete();
    }

    public void testDeleteInvokesRemoteClientAndObserverHandlerWithSuccessResponse() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);

        final RemoteStore store = new RemoteStore(null, null) {
            @Override
            protected RemoteClient createRemoteClient(final Context context) {
                return new MockRemoteClient() {

                    @Override
                    public String delete(final String accessToken, final String url) throws Exception {
                        latch1.countDown();
                        return VALUE;
                    }
                };
            }

            @Override
            protected ObserverHandler createObserverHandler(final Set<DataStore.Observer> observers, final Object lock) {
                return new TestObserverHandler() {

                    @Override
                    public void postResponse(final DataStore.Response response) {
                        latch2.countDown();
                    }
                };
            }
        };

        final DataStore.Response response = store.delete(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        latch1.assertComplete();
        latch2.assertComplete();
    }

    public void testDeleteInvokesRemoteClientAndObserverHandlerWithFailureResponse() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);

        final RemoteStore store = new RemoteStore(null, null) {
            @Override
            protected RemoteClient createRemoteClient(final Context context) {
                return new MockRemoteClient() {

                    @Override
                    public String delete(final String accessToken, final String url) throws Exception {
                        latch1.countDown();
                        throw new RuntimeException();
                    }
                };
            }

            @Override
            protected ObserverHandler createObserverHandler(final Set<DataStore.Observer> observers, final Object lock) {
                return new TestObserverHandler() {

                    @Override
                    public void postResponse(final DataStore.Response response) {
                        latch2.countDown();
                    }
                };
            }
        };

        final DataStore.Response response = store.delete(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.FAILURE, response.status);
        assertEquals(KEY, response.key);
        assertNotNull(response.error);

        latch1.assertComplete();
        latch2.assertComplete();
    }

    private static class TestObserverHandler extends ObserverHandler {

        public TestObserverHandler() {
            super(new HashSet<DataStore.Observer>(), new Object());
        }
    }

    private static class TestContext extends MockContext {

        @Override
        public SharedPreferences getSharedPreferences(final String name, final int mode) {
            return null;
        }
    }
}
