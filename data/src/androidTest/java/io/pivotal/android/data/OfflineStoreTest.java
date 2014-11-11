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
        final DataStore.Response localResponse = DataStore.Response.success(KEY, VALUE);
        final DataStore.Response remoteResponse = DataStore.Response.success(KEY, VALUE);
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
        final DataStore.Response remoteResponse = DataStore.Response.failure(KEY, new DataError(new Exception()));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.get(TOKEN, KEY)).thenReturn(remoteResponse);

        assertEquals(remoteResponse, offlineStore.get(TOKEN, KEY));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).get(TOKEN, KEY);
    }

    public void testGetInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsNotSupported() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final DataStore.Response localResponse = DataStore.Response.success(KEY, VALUE);
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
        final DataStore.Response localResponse = DataStore.Response.success(KEY, VALUE);
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


    public void testAsyncGetInvokesRemoteStoreWithLocalPutListenerWhenConnectionIsAvailable() {
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Listener listener = Mockito.mock(DataStore.Listener.class);
        final DataStore.Listener putListener = Mockito.mock(OfflineStore.LocalPutListener.class);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.doNothing().when(remoteStore).get(TOKEN, KEY, putListener);
        Mockito.doReturn(putListener).when(offlineStore).newLocalPutListener(TOKEN, listener);

        offlineStore.get(TOKEN, KEY, listener);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).newLocalPutListener(TOKEN, listener);
        Mockito.verify(remoteStore).get(TOKEN, KEY, putListener);
    }

    public void testAsyncGetInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsSupported() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final DataStore.Listener listener = Mockito.mock(DataStore.Listener.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final DataStore.Response localResponse = DataStore.Response.success(KEY, VALUE);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, null));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(true).when(offlineStore).isSyncSupported();
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();
        Mockito.doNothing().when(requestCache).queueGet(TOKEN, COLLECTION, KEY);
        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(localResponse);

        offlineStore.get(TOKEN, KEY, listener);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(requestCache).queueGet(TOKEN, COLLECTION, KEY);
        Mockito.verify(listener).onResponse(localResponse);
    }

    public void testAsyncGetInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsNotSupported() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final DataStore.Listener listener = Mockito.mock(DataStore.Listener.class);
        final DataStore.Response localResponse = DataStore.Response.success(KEY, VALUE);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, null));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(false).when(offlineStore).isSyncSupported();
        Mockito.doReturn(null).when(offlineStore).getRequestCache();
        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(localResponse);

        offlineStore.get(TOKEN, KEY, listener);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(offlineStore, Mockito.never()).getRequestCache();
        Mockito.verify(listener).onResponse(localResponse);
    }


    public void testPutInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteSucceeds() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Response localResponse = DataStore.Response.success(KEY, VALUE);
        final DataStore.Response remoteResponse = DataStore.Response.success(KEY, VALUE);
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
        final DataStore.Response remoteResponse = DataStore.Response.failure(KEY, new DataError(new Exception()));
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
        final DataStore.Response fallbackResponse = DataStore.Response.success(KEY, FALLBACK);
        final DataStore.Response localResponse = DataStore.Response.success(KEY, VALUE);
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

    public void testPutInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsNotSupported() {
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, null));
        final DataStore.Response noConnectionResponse = DataStore.Response.failure(KEY, new DataError(new Exception()));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(false).when(offlineStore).isSyncSupported();
        Mockito.doReturn(null).when(offlineStore).getRequestCache();
        Mockito.doReturn(noConnectionResponse).when(offlineStore).newNoConnectionFailureResponse(KEY);

        assertEquals(noConnectionResponse, offlineStore.put(TOKEN, KEY, VALUE));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(offlineStore, Mockito.never()).getRequestCache();
    }

    public void testAsyncPutInvokesRemoteStoreWithLocalPutListenerWhenConnectionIsAvailable() {
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Listener listener = Mockito.mock(DataStore.Listener.class);
        final DataStore.Listener putListener = Mockito.mock(OfflineStore.LocalPutListener.class);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.doNothing().when(remoteStore).put(TOKEN, KEY, VALUE, putListener);
        Mockito.doReturn(putListener).when(offlineStore).newLocalPutListener(TOKEN, listener);

        offlineStore.put(TOKEN, KEY, VALUE, listener);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).newLocalPutListener(TOKEN, listener);
        Mockito.verify(remoteStore).put(TOKEN, KEY, VALUE, putListener);
    }

    public void testAsyncPutInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsSupported() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final DataStore.Listener listener = Mockito.mock(DataStore.Listener.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final DataStore.Response fallbackResponse = DataStore.Response.success(KEY, FALLBACK);
        final DataStore.Response localResponse = DataStore.Response.success(KEY, VALUE);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, null));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(true).when(offlineStore).isSyncSupported();
        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(fallbackResponse);
        Mockito.when(localStore.put(TOKEN, KEY, VALUE)).thenReturn(localResponse);
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();
        Mockito.doNothing().when(requestCache).queuePut(TOKEN, COLLECTION, KEY, VALUE, FALLBACK);

        offlineStore.put(TOKEN, KEY, VALUE, listener);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(localStore).get(TOKEN, KEY);
        Mockito.verify(localStore).put(TOKEN, KEY, VALUE);
        Mockito.verify(requestCache).queuePut(TOKEN, COLLECTION, KEY, VALUE, FALLBACK);
        Mockito.verify(listener).onResponse(localResponse);
    }

    public void testAsyncPutInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsNotSupported() {
        final DataStore.Listener listener = Mockito.mock(DataStore.Listener.class);
        final DataStore.Response noConnectionResponse = DataStore.Response.failure(KEY, new DataError(new Exception()));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, null));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(false).when(offlineStore).isSyncSupported();
        Mockito.doReturn(null).when(offlineStore).getRequestCache();
        Mockito.doReturn(noConnectionResponse).when(offlineStore).newNoConnectionFailureResponse(KEY);

        offlineStore.put(TOKEN, KEY, VALUE, listener);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(offlineStore, Mockito.never()).getRequestCache();
        Mockito.verify(listener).onResponse(noConnectionResponse);
    }


    public void testDeleteInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteSucceeds() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Response localResponse = DataStore.Response.success(KEY, VALUE);
        final DataStore.Response remoteResponse = DataStore.Response.success(KEY, VALUE);
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
        final DataStore.Response remoteResponse = DataStore.Response.failure(KEY, new DataError(new Exception()));
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
        final DataStore.Response fallbackResponse = DataStore.Response.success(KEY, FALLBACK);
        final DataStore.Response localResponse = DataStore.Response.success(KEY, VALUE);
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

    public void testDeleteInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsNotSupported() {
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, null));
        final DataStore.Response noConnectionResponse = DataStore.Response.failure(KEY, new DataError(new Exception()));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(false).when(offlineStore).isSyncSupported();
        Mockito.doReturn(null).when(offlineStore).getRequestCache();
        Mockito.doReturn(noConnectionResponse).when(offlineStore).newNoConnectionFailureResponse(KEY);

        assertEquals(noConnectionResponse, offlineStore.delete(TOKEN, KEY));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(offlineStore, Mockito.never()).getRequestCache();
    }

    public void testAsyncDeleteInvokesRemoteStoreWithLocalDeleteListenerWhenConnectionIsAvailable() {
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Listener listener = Mockito.mock(DataStore.Listener.class);
        final DataStore.Listener deleteListener = Mockito.mock(OfflineStore.LocalDeleteListener.class);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.doNothing().when(remoteStore).delete(TOKEN, KEY, deleteListener);
        Mockito.doReturn(deleteListener).when(offlineStore).newLocalDeleteListener(TOKEN, listener);

        offlineStore.delete(TOKEN, KEY, listener);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).newLocalDeleteListener(TOKEN, listener);
        Mockito.verify(remoteStore).delete(TOKEN, KEY, deleteListener);
    }

    public void testAsyncDeleteInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsSupported() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final DataStore.Listener listener = Mockito.mock(DataStore.Listener.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final DataStore.Response fallbackResponse = DataStore.Response.success(KEY, FALLBACK);
        final DataStore.Response localResponse = DataStore.Response.success(KEY, VALUE);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, localStore, null));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(true).when(offlineStore).isSyncSupported();
        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(fallbackResponse);
        Mockito.when(localStore.delete(TOKEN, KEY)).thenReturn(localResponse);
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();
        Mockito.doNothing().when(requestCache).queueDelete(TOKEN, COLLECTION, KEY, FALLBACK);

        offlineStore.delete(TOKEN, KEY, listener);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(localStore).get(TOKEN, KEY);
        Mockito.verify(localStore).delete(TOKEN, KEY);
        Mockito.verify(requestCache).queueDelete(TOKEN, COLLECTION, KEY, FALLBACK);
        Mockito.verify(listener).onResponse(localResponse);
    }

    public void testAsyncDeleteInvokesLocalStoreWhenConnectionIsNotAvailableAndSyncIsNotSupported() {
        final DataStore.Listener listener = Mockito.mock(DataStore.Listener.class);
        final DataStore.Response noConnectionResponse = DataStore.Response.failure(KEY, new DataError(new Exception()));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, COLLECTION, null, null));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(false).when(offlineStore).isSyncSupported();
        Mockito.doReturn(null).when(offlineStore).getRequestCache();
        Mockito.doReturn(noConnectionResponse).when(offlineStore).newNoConnectionFailureResponse(KEY);

        offlineStore.delete(TOKEN, KEY, listener);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(offlineStore).isSyncSupported();
        Mockito.verify(offlineStore, Mockito.never()).getRequestCache();
        Mockito.verify(listener).onResponse(noConnectionResponse);
    }

    public void testLocalPutListenerInvokesLocalStoreOnSuccessResponse() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final OfflineStore offlineStore = new OfflineStore(null, COLLECTION, localStore, null);
        final DataStore.Listener mockListener = Mockito.mock(DataStore.Listener.class);
        final OfflineStore.LocalPutListener listener = offlineStore.new LocalPutListener(TOKEN, mockListener);

        Mockito.doNothing().when(localStore).put(TOKEN, KEY, VALUE, mockListener);

        listener.onResponse(DataStore.Response.success(KEY, VALUE));

        Mockito.verify(localStore).put(TOKEN, KEY, VALUE, mockListener);
    }

    public void testLocalPutListenerInvokesListenerOnFailureResponse() {
        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);
        final DataStore.Listener mockListener = Mockito.mock(DataStore.Listener.class);
        final OfflineStore.LocalPutListener listener = offlineStore.new LocalPutListener(TOKEN, mockListener);
        final DataStore.Response failureResponse = DataStore.Response.failure(KEY, new DataError(new Exception()));

        listener.onResponse(failureResponse);

        Mockito.verify(mockListener).onResponse(failureResponse);
    }

    public void testLocalDeleteListenerInvokesLocalStoreOnSuccessResponse() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final OfflineStore offlineStore = new OfflineStore(null, COLLECTION, localStore, null);
        final DataStore.Listener mockListener = Mockito.mock(DataStore.Listener.class);
        final OfflineStore.LocalDeleteListener listener = offlineStore.new LocalDeleteListener(TOKEN, mockListener);

        Mockito.doNothing().when(localStore).delete(TOKEN, KEY, mockListener);

        listener.onResponse(DataStore.Response.success(KEY, VALUE));

        Mockito.verify(localStore).delete(TOKEN, KEY, mockListener);
    }

    public void testLocalDeleteListenerInvokesListenerOnFailureResponse() {
        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);
        final DataStore.Listener mockListener = Mockito.mock(DataStore.Listener.class);
        final OfflineStore.LocalDeleteListener listener = offlineStore.new LocalDeleteListener(TOKEN, mockListener);
        final DataStore.Response failureResponse = DataStore.Response.failure(KEY, new DataError(new Exception()));

        listener.onResponse(failureResponse);

        Mockito.verify(mockListener).onResponse(failureResponse);
    }
}
