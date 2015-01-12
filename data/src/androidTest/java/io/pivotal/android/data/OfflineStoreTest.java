/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.Random;

@SuppressWarnings("unchecked")
public class OfflineStoreTest extends AndroidTestCase {

    private static final boolean RESULT = new Random().nextBoolean();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testGetInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteSucceeds() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response localResponse = new Response(new Object(), null);
        final Response remoteResponse = new Response(new Object(), null);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.get(request)).thenReturn(remoteResponse);
        Mockito.when(keyValueStore.put(request)).thenReturn(localResponse);

        assertEquals(localResponse, offlineStore.get(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).get(request);
        Mockito.verify(keyValueStore).put(request);
    }

    public void testGetInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteFails() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response localResponse = new Response(new Object(), null);
        final Response remoteResponse = new Response(new Object(), new DataError(new Exception()));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.get(request)).thenReturn(remoteResponse);
        Mockito.when(keyValueStore.put(request)).thenReturn(localResponse);

        assertEquals(remoteResponse, offlineStore.get(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).get(request);
        Mockito.verify(keyValueStore, Mockito.never()).put(request);
    }

    public void testGetInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteFailsWithNotModified() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response localResponse = new Response(new Object(), null);
        final Response remoteResponse = new Response(new Object(), new DataError(new DataHttpException(304, "")));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.get(request)).thenReturn(remoteResponse);
        Mockito.when(keyValueStore.get(request)).thenReturn(localResponse);

        assertEquals(localResponse, offlineStore.get(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).get(request);
        Mockito.verify(keyValueStore).get(request);
    }

    public void testGetInvokesRemoteAndLocalStoreWhenConnectionIsNotAvailable() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final Response localResponse = new Response(new Object(), null);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, null));
        final Request request = new Request();

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.when(keyValueStore.get(request)).thenReturn(localResponse);
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();

        assertEquals(localResponse, offlineStore.get(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(keyValueStore).get(request);
        Mockito.verify(requestCache).queueGet(request);
    }

    public void testPutInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteSucceeds() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response localResponse = new Response(new Object(), null);
        final Response remoteResponse = new Response(new Object(), null);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.put(request)).thenReturn(remoteResponse);
        Mockito.when(keyValueStore.put(request)).thenReturn(localResponse);

        assertEquals(localResponse, offlineStore.put(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).put(request);
        Mockito.verify(keyValueStore).put(request);
    }

    public void testPutInvokesRemoteStoreWhenConnectionIsAvailableAndRemoteFails() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response remoteResponse = new Response(new Object(), new DataError(new Exception()));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.put(request)).thenReturn(remoteResponse);

        assertEquals(remoteResponse, offlineStore.put(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).put(request);
        Mockito.verify(keyValueStore, Mockito.never()).put(request);
    }

    public void testPutInvokesRemoteAndLocalStoreWhenConnectionIsNotAvailable() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final Response fallbackResponse = new Response(new Object(), null);
        final Response localResponse = new Response(new Object(), null);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, null));
        final Request request = new Request();

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.when(keyValueStore.get(request)).thenReturn(fallbackResponse);
        Mockito.when(keyValueStore.put(request)).thenReturn(localResponse);
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();

        assertEquals(localResponse, offlineStore.put(request));
        assertEquals(fallbackResponse.object, request.fallback);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(keyValueStore).get(request);
        Mockito.verify(keyValueStore).put(request);
        Mockito.verify(requestCache).queuePut(request);
    }

    public void testDeleteInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteSucceeds() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response localResponse = new Response(new Object(), null);
        final Response remoteResponse = new Response(new Object(), null);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.delete(request)).thenReturn(remoteResponse);
        Mockito.when(keyValueStore.delete(request)).thenReturn(localResponse);

        assertEquals(localResponse, offlineStore.delete(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).delete(request);
        Mockito.verify(keyValueStore).delete(request);
    }

    public void testDeleteInvokesRemoteStoreWhenConnectionIsAvailableAndRemoteFails() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response remoteResponse = new Response(new Object(), new DataError(new Exception()));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.delete(request)).thenReturn(remoteResponse);

        assertEquals(remoteResponse, offlineStore.delete(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).delete(request);
        Mockito.verify(keyValueStore, Mockito.never()).delete(request);
    }

    public void testDeleteInvokesRemoteAndLocalStoreWhenConnectionIsNotAvailable() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final Response fallbackResponse = new Response(new Object(), null);
        final Response localResponse = new Response(new Object(), null);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, null));
        final Request request = new Request();

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.when(keyValueStore.get(request)).thenReturn(fallbackResponse);
        Mockito.when(keyValueStore.delete(request)).thenReturn(localResponse);
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();

        assertEquals(localResponse, offlineStore.delete(request));
        assertEquals(fallbackResponse.object, request.fallback);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(keyValueStore).get(request);
        Mockito.verify(keyValueStore).delete(request);
        Mockito.verify(requestCache).queueDelete(request);
    }

    public void testAddObserverInvokesLocalStoreAndRemoteStore() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Observer observer = Mockito.mock(DataStore.Observer.class);
        final OfflineStore offlineStore = new OfflineStore(null, keyValueStore, remoteStore);

        Mockito.when(keyValueStore.addObserver(observer)).thenReturn(true);
        Mockito.when(remoteStore.addObserver(observer)).thenReturn(RESULT);

        assertEquals(RESULT, offlineStore.addObserver(observer));

        Mockito.verify(keyValueStore).addObserver(observer);
        Mockito.verify(remoteStore).addObserver(observer);
    }

    public void testRemoveObserverInvokesLocalStoreAndRemoteStore() {
        final KeyValueStore keyValueStore = Mockito.mock(KeyValueStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Observer observer = Mockito.mock(DataStore.Observer.class);
        final OfflineStore offlineStore = new OfflineStore(null, keyValueStore, remoteStore);

        Mockito.when(keyValueStore.removeObserver(observer)).thenReturn(true);
        Mockito.when(remoteStore.removeObserver(observer)).thenReturn(RESULT);

        assertEquals(RESULT, offlineStore.removeObserver(observer));

        Mockito.verify(keyValueStore).removeObserver(observer);
        Mockito.verify(remoteStore).removeObserver(observer);
    }
}
