/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.UUID;

public class OfflineStoreTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
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
        final Context context = Mockito.mock(Context.class);
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);

        Mockito.when(localStore.contains(TOKEN, KEY)).thenReturn(true);

        final OfflineStore offlineStore = new OfflineStore(context, COLLECTION, localStore, remoteStore);
        final boolean response = offlineStore.contains(TOKEN, KEY);

        assertTrue(response);

        Mockito.verify(localStore).contains(TOKEN, KEY);
        Mockito.verify(remoteStore, Mockito.never()).contains(TOKEN, KEY);
    }

    public void testAddObserverInvokesLocalStoreNotRemoteStore() {
        final Context context = Mockito.mock(Context.class);
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);

        Mockito.when(localStore.addObserver(OBSERVER)).thenReturn(true);
        Mockito.when(remoteStore.addObserver(OBSERVER)).thenReturn(true);

        final OfflineStore offlineStore = new OfflineStore(context, COLLECTION, localStore, remoteStore);
        final boolean response = offlineStore.addObserver(OBSERVER);

        assertTrue(response);

        Mockito.verify(localStore).addObserver(OBSERVER);
        Mockito.verify(remoteStore, Mockito.never()).addObserver(OBSERVER);
    }

    public void testRemoveObserverInvokesLocalStoreNotRemoteStore() {
        final Context context = Mockito.mock(Context.class);
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);

        Mockito.when(localStore.removeObserver(OBSERVER)).thenReturn(true);
        Mockito.when(remoteStore.removeObserver(OBSERVER)).thenReturn(true);

        final OfflineStore offlineStore = new OfflineStore(context, COLLECTION, localStore, remoteStore);
        final boolean response = offlineStore.removeObserver(OBSERVER);

        assertTrue(response);

        Mockito.verify(localStore).removeObserver(OBSERVER);
        Mockito.verify(remoteStore, Mockito.never()).removeObserver(OBSERVER);
    }


    public void testGetInvokesLocalAndRemoteStoreWhenConnectionIsAvailable() {
        final Context context = Mockito.mock(Context.class);
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);

        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(DataStore.Response.success(KEY, VALUE));
        Mockito.doNothing().when(remoteStore).get(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.any(OfflineStore.UpdateListener.class));

        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(context, COLLECTION, localStore, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();

        final DataStore.Response response = offlineStore.get(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(localStore).get(TOKEN, KEY);
        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).get(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.any(OfflineStore.UpdateListener.class));
    }

    public void testGetInvokesLocalStoreAndCachesRequestWhenConnectionIsNotAvailable() {
        final Context context = Mockito.mock(Context.class);
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);

        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(DataStore.Response.success(KEY, VALUE));

        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(context, COLLECTION, localStore, remoteStore));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(true).when(offlineStore).hasReceiver();
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();
        Mockito.doNothing().when(requestCache).addGetRequest(TOKEN, COLLECTION, KEY);

        final DataStore.Response response = offlineStore.get(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(localStore).get(TOKEN, KEY);
        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(requestCache).addGetRequest(TOKEN, COLLECTION, KEY);
    }

    public void testPutInvokesLocalAndRemoteStoreWhenConnectionIsAvailable() {
        final Context context = Mockito.mock(Context.class);
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);

        Mockito.when(localStore.put(TOKEN, KEY, VALUE)).thenReturn(DataStore.Response.success(KEY, VALUE));
        Mockito.doNothing().when(remoteStore).put(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.eq(VALUE), Mockito.any(OfflineStore.UpdateListener.class));

        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(context, COLLECTION, localStore, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();

        final DataStore.Response response = offlineStore.put(TOKEN, KEY, VALUE);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(localStore).put(TOKEN, KEY, VALUE);
        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).put(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.eq(VALUE), Mockito.any(OfflineStore.UpdateListener.class));
    }

    public void testPutInvokesLocalStoreAndCachesRequestWhenConnectionIsNotAvailable() {
        final Context context = Mockito.mock(Context.class);
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);

        Mockito.when(localStore.put(TOKEN, KEY, VALUE)).thenReturn(DataStore.Response.success(KEY, VALUE));

        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(context, COLLECTION, localStore, remoteStore));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(true).when(offlineStore).hasReceiver();
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();
        Mockito.doNothing().when(requestCache).addPutRequest(TOKEN, COLLECTION, KEY, VALUE);

        final DataStore.Response response = offlineStore.put(TOKEN, KEY, VALUE);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(localStore).put(TOKEN, KEY, VALUE);
        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(requestCache).addPutRequest(TOKEN, COLLECTION, KEY, VALUE);
    }

    public void testDeleteInvokesLocalAndRemoteStoreWhenConnectionIsAvailable() {
        final Context context = Mockito.mock(Context.class);
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);

        Mockito.when(localStore.delete(TOKEN, KEY)).thenReturn(DataStore.Response.success(KEY, VALUE));
        Mockito.doNothing().when(remoteStore).delete(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.any(OfflineStore.DeleteListener.class));

        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(context, COLLECTION, localStore, remoteStore));

        Mockito.doReturn(true).when(offlineStore).isConnected();

        final DataStore.Response response = offlineStore.delete(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(localStore).delete(TOKEN, KEY);
        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(remoteStore).delete(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.any(OfflineStore.DeleteListener.class));
    }

    public void testDeleteInvokesLocalStoreAndCachesRequestWhenConnectionIsNotAvailable() {
        final Context context = Mockito.mock(Context.class);
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);

        Mockito.when(localStore.delete(TOKEN, KEY)).thenReturn(DataStore.Response.success(KEY, VALUE));

        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(context, COLLECTION, localStore, remoteStore));

        Mockito.doReturn(false).when(offlineStore).isConnected();
        Mockito.doReturn(true).when(offlineStore).hasReceiver();
        Mockito.doReturn(requestCache).when(offlineStore).getRequestCache();
        Mockito.doNothing().when(requestCache).addDeleteRequest(TOKEN, COLLECTION, KEY);

        final DataStore.Response response = offlineStore.delete(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(localStore).delete(TOKEN, KEY);
        Mockito.verify(offlineStore).isConnected();
        Mockito.verify(requestCache).addDeleteRequest(TOKEN, COLLECTION, KEY);
    }


    public void testIsConnectedChecksActiveNetworkStateFromConnectivityManagerAndReturnsTrue() {
        final Context context = Mockito.mock(Context.class);
        final NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);
        final ConnectivityManager manager = Mockito.mock(ConnectivityManager.class);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(context, COLLECTION, null, null));

        Mockito.when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        Mockito.when(manager.getActiveNetworkInfo()).thenReturn(networkInfo);
        Mockito.when(networkInfo.isConnectedOrConnecting()).thenReturn(true);

        assertTrue(offlineStore.isConnected());

        Mockito.verify(context).getSystemService(Context.CONNECTIVITY_SERVICE);
        Mockito.verify(manager).getActiveNetworkInfo();
        Mockito.verify(networkInfo).isConnectedOrConnecting();
    }

    public void testIsConnectedChecksActiveNetworkStateFromConnectivityManagerAndReturnsFalse() {
        final Context context = Mockito.mock(Context.class);
        final NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);
        final ConnectivityManager manager = Mockito.mock(ConnectivityManager.class);
        final OfflineStore offlineStore = Mockito.spy(new OfflineStore(context, COLLECTION, null, null));

        Mockito.when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        Mockito.when(manager.getActiveNetworkInfo()).thenReturn(networkInfo);
        Mockito.when(networkInfo.isConnectedOrConnecting()).thenReturn(false);

        assertFalse(offlineStore.isConnected());

        Mockito.verify(context).getSystemService(Context.CONNECTIVITY_SERVICE);
        Mockito.verify(manager).getActiveNetworkInfo();
        Mockito.verify(networkInfo).isConnectedOrConnecting();
    }
}
