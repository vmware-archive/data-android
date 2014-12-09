/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.UUID;

public class OfflineStoreTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final String FALLBACK = UUID.randomUUID().toString();
    private static final String TOKEN = UUID.randomUUID().toString();
    private static final String COLLECTION = UUID.randomUUID().toString();

    private static final DataStore.Observer OBSERVER = new DataStore.Observer() {
        @Override
        public void onChange(final String key, final String value) {}

        @Override
        public void onError(final String key, final DataError error) {}
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testContainsInvokesLocalStoreNotRemoteStore() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final OfflineStore offlineStore = new OfflineStore(null, COLLECTION, localStore, remoteStore);

        Mockito.when(localStore.contains(TOKEN, KEY)).thenReturn(true);
        Mockito.when(remoteStore.contains(TOKEN, KEY)).thenReturn(true);

        assertTrue(offlineStore.contains(TOKEN, KEY));

        Mockito.verify(localStore).contains(TOKEN, KEY);
        Mockito.verify(remoteStore, Mockito.never()).contains(TOKEN, KEY);
    }

    public void testAddObserverInvokesLocalStoreAndRemoteStore() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final OfflineStore offlineStore = new OfflineStore(null, COLLECTION, localStore, remoteStore);

        Mockito.when(localStore.addObserver(OBSERVER)).thenReturn(true);
        Mockito.when(remoteStore.addObserver(OBSERVER)).thenReturn(true);

        assertTrue(offlineStore.addObserver(OBSERVER));

        Mockito.verify(localStore).addObserver(OBSERVER);
        Mockito.verify(remoteStore).addObserver(OBSERVER);
    }

    public void testRemoveObserverInvokesLocalStoreAndRemoteStore() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final OfflineStore offlineStore = new OfflineStore(null, COLLECTION, localStore, remoteStore);

        Mockito.when(localStore.removeObserver(OBSERVER)).thenReturn(true);
        Mockito.when(remoteStore.removeObserver(OBSERVER)).thenReturn(true);

        assertTrue(offlineStore.removeObserver(OBSERVER));

        Mockito.verify(localStore).removeObserver(OBSERVER);
        Mockito.verify(remoteStore).removeObserver(OBSERVER);
    }


    public void testGetInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteSucceeds() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Response localResponse = new DataStore.Response(KEY, VALUE);
        final DataStore.Response remoteResponse = new DataStore.Response(KEY, VALUE);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.get(TOKEN, KEY)).thenReturn(remoteResponse);
        Mockito.when(localStore.put(TOKEN, KEY, VALUE)).thenReturn(localResponse);

        assertEquals(localResponse, offlineStore.get(TOKEN, KEY));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).get(TOKEN, KEY);
        Mockito.verify(localStore).put(TOKEN, KEY, VALUE);
    }

    public void testGetInvokesRemoteStoreWhenConnectionIsAvailableAndRemoteFails() {
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Response remoteResponse = new DataStore.Response(KEY, new DataError(new Exception()));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.get(TOKEN, KEY)).thenReturn(remoteResponse);

        assertEquals(remoteResponse, offlineStore.get(TOKEN, KEY));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).get(TOKEN, KEY);
    }

    public void testGetInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteFailsWithNotModified() {
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final DataStore.Response localResponse = new DataStore.Response(KEY, VALUE);
        final DataStore.Response remoteResponse = new DataStore.Response(KEY, new DataError(new DataException(304, "")));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.get(TOKEN, KEY)).thenReturn(remoteResponse);
        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(localResponse);

        assertEquals(localResponse, offlineStore.get(TOKEN, KEY));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).get(TOKEN, KEY);
        Mockito.verify(localStore).get(TOKEN, KEY);
    }

    public void testGetInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsNotSupported() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final DataStore.Response localResponse = new DataStore.Response(KEY, VALUE);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, null));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(false).when(offlineStore).isSyncSupported();
        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(localResponse);
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();

        assertEquals(localResponse, offlineStore.get(TOKEN, KEY));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(localStore).get(TOKEN, KEY);
        Mockito.verify(requestCache, Mockito.never()).queueGet(TOKEN, COLLECTION, KEY);
    }

    public void testGetInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsSupported() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final DataStore.Response localResponse = new DataStore.Response(KEY, VALUE);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, null));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(true).when(offlineStore).isSyncSupported();
        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(localResponse);
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();
        Mockito.doNothing().when(requestCache).queueGet(TOKEN, COLLECTION, KEY);

        assertEquals(localResponse, offlineStore.get(TOKEN, KEY));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(localStore).get(TOKEN, KEY);
        Mockito.verify(requestCache).queueGet(TOKEN, COLLECTION, KEY);
    }

    public void testPutInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteSucceeds() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Response localResponse = new DataStore.Response(KEY, VALUE);
        final DataStore.Response remoteResponse = new DataStore.Response(KEY, VALUE);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.put(TOKEN, KEY, VALUE)).thenReturn(remoteResponse);
        Mockito.when(localStore.put(TOKEN, KEY, VALUE)).thenReturn(localResponse);

        assertEquals(localResponse, offlineStore.put(TOKEN, KEY, VALUE));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).put(TOKEN, KEY, VALUE);
        Mockito.verify(localStore).put(TOKEN, KEY, VALUE);
    }

    public void testPutInvokesRemoteStoreWhenConnectionIsAvailableAndRemoteFails() {
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Response remoteResponse = new DataStore.Response(KEY, new DataError(new Exception()));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.put(TOKEN, KEY, VALUE)).thenReturn(remoteResponse);

        assertEquals(remoteResponse, offlineStore.put(TOKEN, KEY, VALUE));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).put(TOKEN, KEY, VALUE);
    }

    public void testPutInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsSupported() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final DataStore.Response fallbackResponse = new DataStore.Response(KEY, FALLBACK);
        final DataStore.Response localResponse = new DataStore.Response(KEY, VALUE);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, null));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(true).when(offlineStore).isSyncSupported();
        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(fallbackResponse);
        Mockito.when(localStore.put(TOKEN, KEY, VALUE)).thenReturn(localResponse);
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();
        Mockito.doNothing().when(requestCache).queuePut(TOKEN, COLLECTION, KEY, VALUE, FALLBACK);

        assertEquals(localResponse, offlineStore.put(TOKEN, KEY, VALUE));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(localStore).get(TOKEN, KEY);
        Mockito.verify(localStore).put(TOKEN, KEY, VALUE);
        Mockito.verify(requestCache).queuePut(TOKEN, COLLECTION, KEY, VALUE, fallbackResponse.value);
    }

    public void testPutFailsWhenConnectionIsNotAvailableAndSyncIsNotSupported() {
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, null));
        final DataStore.Response noConnectionResponse = new DataStore.Response(KEY, new DataError(new Exception()));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(false).when(offlineStore).isSyncSupported();
        Mockito.doReturn(null).when(offlineStore).getRequestCache();
        Mockito.doReturn(noConnectionResponse).when(offlineStore).newNoConnectionFailureResponse(KEY);

        assertEquals(noConnectionResponse, offlineStore.put(TOKEN, KEY, VALUE));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(offlineStore, Mockito.never()).getRequestCache();
    }

    public void testDeleteInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteSucceeds() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Response localResponse = new DataStore.Response(KEY, VALUE);
        final DataStore.Response remoteResponse = new DataStore.Response(KEY, VALUE);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.delete(TOKEN, KEY)).thenReturn(remoteResponse);
        Mockito.when(localStore.delete(TOKEN, KEY)).thenReturn(localResponse);

        assertEquals(localResponse, offlineStore.delete(TOKEN, KEY));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).delete(TOKEN, KEY);
        Mockito.verify(localStore).delete(TOKEN, KEY);
    }

    public void testDeleteInvokesRemoteStoreWhenConnectionIsAvailableAndRemoteFails() {
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Response remoteResponse = new DataStore.Response(KEY, new DataError(new Exception()));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.delete(TOKEN, KEY)).thenReturn(remoteResponse);

        assertEquals(remoteResponse, offlineStore.delete(TOKEN, KEY));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).delete(TOKEN, KEY);
    }

    public void testDeleteInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsSupported() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final DataStore.Response fallbackResponse = new DataStore.Response(KEY, FALLBACK);
        final DataStore.Response localResponse = new DataStore.Response(KEY, VALUE);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, null));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(true).when(offlineStore).isSyncSupported();
        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(fallbackResponse);
        Mockito.when(localStore.delete(TOKEN, KEY)).thenReturn(localResponse);
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();
        Mockito.doNothing().when(requestCache).queueDelete(TOKEN, COLLECTION, KEY, FALLBACK);

        assertEquals(localResponse, offlineStore.delete(TOKEN, KEY));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(localStore).get(TOKEN, KEY);
        Mockito.verify(localStore).delete(TOKEN, KEY);
        Mockito.verify(requestCache).queueDelete(TOKEN, COLLECTION, KEY, fallbackResponse.value);
    }

    public void testDeleteFailsWhenConnectionIsNotAvailableAndSyncIsNotSupported() {
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, null));
        final DataStore.Response noConnectionResponse = new DataStore.Response(KEY, new DataError(new Exception()));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(false).when(offlineStore).isSyncSupported();
        Mockito.doReturn(null).when(offlineStore).getRequestCache();
        Mockito.doReturn(noConnectionResponse).when(offlineStore).newNoConnectionFailureResponse(KEY);

        assertEquals(noConnectionResponse, offlineStore.delete(TOKEN, KEY));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(offlineStore, Mockito.never()).getRequestCache();
    }
}
