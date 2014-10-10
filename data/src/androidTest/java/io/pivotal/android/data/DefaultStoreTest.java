/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import java.util.UUID;

public class DefaultStoreTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();

    private static final DataStore.Observer OBSERVER = new DataStore.Observer() {
        @Override
        public void onChange(String key, String value) {

        }

        @Override
        public void onError(String key, DataError error) {

        }
    };

    public void testInstantiation() {
        final LocalStore localStore = new MockLocalStore();
        final RemoteStore remoteStore = new MockRemoteStore();
        final DefaultStore dataStore = new DefaultStore(localStore, remoteStore);

        assertNotNull(dataStore);
    }

    public void testGetInvokesLocalAndRemoteStoresPuttingBackToLocalStoreOnSuccess() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);
        final AssertionLatch latch3 = new AssertionLatch(1);

        final LocalStore localStore = new MockLocalStore() {

            @Override
            public Response get(final String token, final String key) {
                latch1.countDown();
                assertEquals(KEY, key);
                return Response.success(KEY, VALUE);
            }

            @Override
            public Response put(String token, String key, String value) {
                latch3.countDown();
                assertEquals(KEY, key);
                assertEquals(VALUE, value);
                return Response.success(KEY, VALUE);
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public void getAsync(final String accessToken, final String key, final Listener listener) {
                latch2.countDown();
                assertEquals(KEY, key);
                listener.onResponse(Response.success(KEY, VALUE));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.get(null, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        latch1.assertComplete();
        latch2.assertComplete();
        latch3.assertComplete();
    }

    public void testGetInvokesLocalAndRemoteStoresWithoutPuttingBackToLocalStoreOnFailure() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);
        final AssertionLatch latch3 = new AssertionLatch(0);

        final LocalStore localStore = new MockLocalStore() {

            @Override
            public Response get(final String token, final String key) {
                latch1.countDown();
                assertEquals(KEY, key);
                return Response.success(KEY, VALUE);
            }

            @Override
            public Response put(String token, String key, String value) {
                latch3.countDown();
                return Response.success(null, null);
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public void getAsync(final String accessToken, final String key, final Listener listener) {
                latch2.countDown();
                assertEquals(KEY, key);
                listener.onResponse(Response.failure(KEY, null));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.get(null, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        latch1.assertComplete();
        latch2.assertComplete();
        latch3.assertComplete();
    }

    public void testPutInvokesLocalAndRemoteStoreOnSuccess() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);

        final LocalStore localStore = new MockLocalStore() {

            @Override
            public Response put(final String token, final String key, final String value) {
                latch2.countDown();
                assertEquals(KEY, key);
                assertEquals(VALUE, value);
                return null;
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public void putAsync(final String accessToken, final String key, final String value, final Listener listener) {
                latch1.countDown();
                assertEquals(KEY, key);
                assertEquals(VALUE, value);
                listener.onResponse(Response.success(KEY, VALUE));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.put(null, KEY, VALUE);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        latch1.assertComplete();
        latch2.assertComplete();
    }

    public void testPutInvokesRemoteStoreOnlyOnFailure() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(0);

        final LocalStore localStore = new MockLocalStore() {

            @Override
            public Response put(final String token, final String key, final String value) {
                latch2.countDown();
                return null;
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public void putAsync(final String accessToken, final String key, final String value, final Listener listener) {
                latch1.countDown();
                assertEquals(KEY, key);
                assertEquals(VALUE, value);
                listener.onResponse(Response.failure(KEY, null));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.put(null, KEY, VALUE);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        latch1.assertComplete();
        latch2.assertComplete();
    }

    public void testDeleteInvokesLocalAndRemoteStores() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);

        final LocalStore localStore = new MockLocalStore() {

            @Override
            public Response delete(final String token, final String key) {
                latch2.countDown();
                assertEquals(KEY, key);
                return null;
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public void deleteAsync(final String accessToken, final String key, final Listener listener) {
                latch1.countDown();
                assertEquals(KEY, key);
                listener.onResponse(Response.success(KEY, null));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.delete(null, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertNull(response.value);

        latch1.assertComplete();
        latch2.assertComplete();
    }

    public void testDeleteInvokesRemoteStoreOnlyOnFailure() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(0);

        final LocalStore localStore = new MockLocalStore() {

            @Override
            public Response delete(final String token, final String key) {
                latch2.countDown();
                return null;
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public void deleteAsync(final String accessToken, final String key, final Listener listener) {
                latch1.countDown();
                assertEquals(KEY, key);
                listener.onResponse(Response.failure(KEY, null));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.delete(null, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertNull(response.value);

        latch1.assertComplete();
        latch2.assertComplete();
    }

    public void testContainsInvokesLocalStoreNotRemoteStore() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(0);

        final LocalStore localStore = new MockLocalStore() {

            @Override
            public boolean contains(final String token, final String key) {
                latch1.countDown();
                assertEquals(KEY, key);
                return true;
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public boolean contains(final String token, final String key) {
                latch2.countDown();
                return false;
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final boolean response = store.contains(null, KEY);

        assertTrue(response);

        latch1.assertComplete();
        latch2.assertComplete();

    }

    public void testAddObserverInvokesLocalAndRemoteStores() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);

        final LocalStore localStore = new MockLocalStore() {

            @Override
            public boolean addObserver(final Observer observer) {
                latch1.countDown();
                assertEquals(OBSERVER, observer);
                return true;
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public boolean addObserver(final Observer observer) {
                latch2.countDown();
                assertEquals(OBSERVER, observer);
                return true;
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final boolean response = store.addObserver(OBSERVER);

        assertTrue(response);

        latch1.assertComplete();
        latch2.assertComplete();

    }

    public void testRemoveObserverInvokesLocalAndRemoteStores() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);

        final LocalStore localStore = new MockLocalStore() {

            @Override
            public boolean removeObserver(final Observer observer) {
                latch1.countDown();
                assertEquals(OBSERVER, observer);
                return true;
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public boolean removeObserver(final Observer observer) {
                latch2.countDown();
                assertEquals(OBSERVER, observer);
                return true;
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final boolean response = store.removeObserver(OBSERVER);

        assertTrue(response);

        latch1.assertComplete();
        latch2.assertComplete();

    }
}
