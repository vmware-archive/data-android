/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;
import android.test.mock.MockContext;

public class DefaultStoreTest extends AndroidTestCase {

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
                return Response.success(null, null);
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
                listener.onResponse(Response.success(null, null));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.get(null, null);

        assertEquals(DataStore.Response.Status.PENDING, response.status);

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
                return Response.success(null, null);
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
                listener.onResponse(Response.failure(null, null));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.get(null, null);

        assertEquals(DataStore.Response.Status.PENDING, response.status);

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
                return null;
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public void putAsync(final String accessToken, final String key, final String value, final Listener listener) {
                latch1.countDown();
                listener.onResponse(Response.success(null, null));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.put(null, null, null);

        assertEquals(DataStore.Response.Status.PENDING, response.status);

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
                listener.onResponse(Response.failure(null, null));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.put(null, null, null);

        assertEquals(DataStore.Response.Status.PENDING, response.status);

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
                return null;
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public void deleteAsync(final String accessToken, final String key, final Listener listener) {
                latch1.countDown();
                listener.onResponse(Response.success(null, null));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.delete(null, null);

        assertEquals(DataStore.Response.Status.PENDING, response.status);

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
                listener.onResponse(Response.failure(null, null));
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.delete(null, null);

        assertEquals(DataStore.Response.Status.PENDING, response.status);

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
        final boolean response = store.contains(null, null);

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
                return true;
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public boolean addObserver(final Observer observer) {
                latch2.countDown();
                return true;
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final boolean response = store.addObserver(null);

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
                return true;
            }
        };

        final RemoteStore remoteStore = new MockRemoteStore() {

            @Override
            public boolean removeObserver(final Observer observer) {
                latch2.countDown();
                return true;
            }
        };

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final boolean response = store.removeObserver(null);

        assertTrue(response);

        latch1.assertComplete();
        latch2.assertComplete();

    }

    // ==========================================



    private static class FakeLocalStore extends LocalStore {

        public FakeLocalStore() {
            super(new MockContext(), null);
        }
    }

    private static class FakeRemoteStore extends LocalStore {

        public FakeRemoteStore() {
            super(new MockContext(), null);
        }
    }
}
