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
        final KeyValueLocalStore localStore = Mockito.mock(KeyValueLocalStore.class);
        final KeyValueRemoteStore remoteStore = Mockito.mock(KeyValueRemoteStore.class);
        final Response localResponse = new Response(new Object(), null);
        final Response remoteResponse = new Response(new Object(), null);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, localStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.execute(Mockito.any(Request.class))).thenReturn(remoteResponse);
        Mockito.when(localStore.execute(Mockito.any(Request.class))).thenReturn(localResponse);

        assertEquals(localResponse, offlineStore.get(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).execute(request);
        Mockito.verify(localStore).execute(Mockito.any(Request.class));
    }

    public void testGetInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteFails() {
        final KeyValueLocalStore keyValueStore = Mockito.mock(KeyValueLocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response localResponse = new Response(new Object(), null);
        final Response remoteResponse = new Response(new Object(), new DataError(new Exception()));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.execute(Mockito.any(Request.class))).thenReturn(remoteResponse);
        Mockito.when(keyValueStore.execute(Mockito.any(Request.class))).thenReturn(localResponse);

        assertEquals(remoteResponse, offlineStore.get(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).execute(request);
        Mockito.verify(keyValueStore, Mockito.never()).execute(Mockito.any(Request.class));
    }

    public void testGetInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteFailsWithNotFound() {
        final KeyValueLocalStore keyValueStore = Mockito.mock(KeyValueLocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response remoteResponse = new Response(new Object(), new DataError(new DataHttpException(404, "")));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.execute(Mockito.any(Request.class))).thenReturn(remoteResponse);

        assertEquals(remoteResponse, offlineStore.get(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).execute(request);
        Mockito.verify(keyValueStore).execute(Mockito.isA(Request.Delete.class));
    }

    public void testGetInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteFailsWithNotModified() {
        final KeyValueLocalStore keyValueStore = Mockito.mock(KeyValueLocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response localResponse = new Response(new Object(), null);
        final Response remoteResponse = new Response(new Object(), new DataError(new DataHttpException(304, "")));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.execute(Mockito.any(Request.class))).thenReturn(remoteResponse);
        Mockito.when(keyValueStore.execute(Mockito.any(Request.class))).thenReturn(localResponse);

        assertEquals(localResponse, offlineStore.get(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).execute(request);
        Mockito.verify(keyValueStore).execute(request);
    }

    public void testGetInvokesRemoteAndLocalStoreWhenConnectionIsNotAvailable() {
        final KeyValueLocalStore keyValueStore = Mockito.mock(KeyValueLocalStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final Response localResponse = new Response(new Object(), null);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, null));
        final Request request = new Request();

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.when(keyValueStore.execute(Mockito.any(Request.class))).thenReturn(localResponse);
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();

        assertEquals(localResponse, offlineStore.get(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(keyValueStore).execute(request);
        Mockito.verify(requestCache).queue(request);
    }

    public void testExecuteWithFallbackInvokesRemoteAndLocalStoreWhenConnectionIsAvailableAndRemoteSucceeds() {
        final KeyValueLocalStore keyValueStore = Mockito.mock(KeyValueLocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response localResponse = new Response(new Object(), null);
        final Response remoteResponse = new Response(new Object(), null);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.execute(Mockito.any(Request.class))).thenReturn(remoteResponse);
        Mockito.when(keyValueStore.execute(Mockito.any(Request.class))).thenReturn(localResponse);

        assertEquals(localResponse, offlineStore.executeWithFallback(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).execute(request);
        Mockito.verify(keyValueStore).execute(request);
    }

    public void testExecuteWithFallbackInvokesRemoteStoreWhenConnectionIsAvailableAndRemoteFails() {
        final KeyValueLocalStore keyValueStore = Mockito.mock(KeyValueLocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final Response remoteResponse = new Response(new Object(), new DataError(new Exception()));
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, remoteStore));
        final Request request = new Request();

        Mockito.doReturn(true).when(offlineStore).isConnected();
        Mockito.when(remoteStore.execute(Mockito.any(Request.class))).thenReturn(remoteResponse);

        assertEquals(remoteResponse, offlineStore.executeWithFallback(request));

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).execute(request);
        Mockito.verify(keyValueStore, Mockito.never()).execute(request);
    }

    public void testExecuteWithFallbackInvokesRemoteAndLocalStoreWhenConnectionIsNotAvailable() {
        final KeyValueLocalStore keyValueStore = Mockito.mock(KeyValueLocalStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final Response fallbackResponse = new Response(new Object(), null);
        final Response localResponse = new Response(new Object(), null);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(null, keyValueStore, null));
        final Request request = new Request();

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.when(keyValueStore.execute(Mockito.isA(Request.class))).thenReturn(localResponse);
        Mockito.when(keyValueStore.execute(Mockito.isA(Request.Get.class))).thenReturn(fallbackResponse);
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();

        assertEquals(localResponse, offlineStore.executeWithFallback(request));
        assertEquals(fallbackResponse.object, request.fallback);

        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(keyValueStore).execute(Mockito.isA(Request.Get.class));
        Mockito.verify(keyValueStore).execute(request);
        Mockito.verify(requestCache).queue(request);
    }

    public void testAddObserverInvokesLocalStoreAndRemoteStore() {
        final KeyValueLocalStore keyValueStore = Mockito.mock(KeyValueLocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Observer observer = Mockito.mock(DataStore.Observer.class);
        final OfflineStore offlineStore = new OfflineStore(null, keyValueStore, remoteStore);

        Mockito.when(keyValueStore.addObserver(Mockito.any(DataStore.Observer.class))).thenReturn(true);
        Mockito.when(remoteStore.addObserver(Mockito.any(DataStore.Observer.class))).thenReturn(RESULT);

        assertEquals(RESULT, offlineStore.addObserver(observer));

        Mockito.verify(keyValueStore).addObserver(observer);
        Mockito.verify(remoteStore).addObserver(observer);
    }

    public void testRemoveObserverInvokesLocalStoreAndRemoteStore() {
        final KeyValueLocalStore keyValueStore = Mockito.mock(KeyValueLocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DataStore.Observer observer = Mockito.mock(DataStore.Observer.class);
        final OfflineStore offlineStore = new OfflineStore(null, keyValueStore, remoteStore);

        Mockito.when(keyValueStore.removeObserver(Mockito.any(DataStore.Observer.class))).thenReturn(true);
        Mockito.when(remoteStore.removeObserver(Mockito.any(DataStore.Observer.class))).thenReturn(RESULT);

        assertEquals(RESULT, offlineStore.removeObserver(observer));

        Mockito.verify(keyValueStore).removeObserver(observer);
        Mockito.verify(remoteStore).removeObserver(observer);
    }
}
