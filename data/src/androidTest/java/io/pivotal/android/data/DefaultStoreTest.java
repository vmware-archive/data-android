/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.UUID;

public class DefaultStoreTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final String TOKEN = UUID.randomUUID().toString();

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

    public void testGetInvokesLocalAndRemoteStoresPuttingBackToLocalStoreOnSuccess() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);

        final ArgumentCaptor<RemoteStore.Listener> argument = ArgumentCaptor.forClass(RemoteStore.Listener.class);
        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(DataStore.Response.success(KEY, VALUE));
        Mockito.when(localStore.put(TOKEN, KEY, VALUE)).thenReturn(DataStore.Response.success(KEY, VALUE));

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.get(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(localStore).get(TOKEN, KEY);
        Mockito.verify(remoteStore).getAsync(Mockito.eq(TOKEN), Mockito.eq(KEY), argument.capture());

        argument.getValue().onResponse(DataStore.Response.success(KEY, VALUE));

        Mockito.verify(localStore).put(TOKEN, KEY, VALUE);
    }

    public void testGetInvokesLocalAndRemoteStoresWithoutPuttingBackToLocalStoreOnFailure() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);

        final ArgumentCaptor<RemoteStore.Listener> argument = ArgumentCaptor.forClass(RemoteStore.Listener.class);
        Mockito.when(localStore.get(TOKEN, KEY)).thenReturn(DataStore.Response.success(KEY, VALUE));
        Mockito.when(localStore.put(TOKEN, KEY, VALUE)).thenReturn(DataStore.Response.success(KEY, VALUE));

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.get(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(localStore).get(TOKEN, KEY);
        Mockito.verify(remoteStore).getAsync(Mockito.eq(TOKEN), Mockito.eq(KEY), argument.capture());

        argument.getValue().onResponse(DataStore.Response.failure(KEY, null));

        Mockito.verify(localStore, Mockito.never()).put(TOKEN, KEY, VALUE);
    }

    public void testPutInvokesLocalAndRemoteStoreOnSuccess() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.put(TOKEN, KEY, VALUE);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        final ArgumentCaptor<RemoteStore.Listener> argument = ArgumentCaptor.forClass(RemoteStore.Listener.class);
        Mockito.verify(remoteStore).putAsync(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.eq(VALUE), argument.capture());
        argument.getValue().onResponse(DataStore.Response.success(KEY, VALUE));

        Mockito.verify(localStore).put(TOKEN, KEY, VALUE);
    }

    public void testPutInvokesRemoteStoreOnlyOnFailure() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.put(TOKEN, KEY, VALUE);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        final ArgumentCaptor<RemoteStore.Listener> argument = ArgumentCaptor.forClass(RemoteStore.Listener.class);
        Mockito.verify(remoteStore).putAsync(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.eq(VALUE), argument.capture());
        argument.getValue().onResponse(DataStore.Response.failure(KEY, null));

        Mockito.verify(localStore, Mockito.never()).put(TOKEN, KEY, VALUE);
    }

    public void testDeleteInvokesLocalAndRemoteStores() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.delete(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertNull(response.value);

        final ArgumentCaptor<RemoteStore.Listener> argument = ArgumentCaptor.forClass(RemoteStore.Listener.class);
        Mockito.verify(remoteStore).deleteAsync(Mockito.eq(TOKEN), Mockito.eq(KEY), argument.capture());
        argument.getValue().onResponse(DataStore.Response.success(KEY, null));

        Mockito.verify(localStore).delete(TOKEN, KEY);
    }

    public void testDeleteInvokesRemoteStoreOnlyOnFailure() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);
        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final DataStore.Response response = store.delete(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals(KEY, response.key);
        assertNull(response.value);

        final ArgumentCaptor<RemoteStore.Listener> argument = ArgumentCaptor.forClass(RemoteStore.Listener.class);
        Mockito.verify(remoteStore).deleteAsync(Mockito.eq(TOKEN), Mockito.eq(KEY), argument.capture());
        argument.getValue().onResponse(DataStore.Response.failure(KEY, null));

        Mockito.verify(localStore, Mockito.never()).delete(TOKEN, KEY);
    }

    public void testContainsInvokesLocalStoreNotRemoteStore() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);

        Mockito.when(localStore.contains(TOKEN, KEY)).thenReturn(true);

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final boolean response = store.contains(TOKEN, KEY);

        assertTrue(response);

        Mockito.verify(localStore).contains(TOKEN, KEY);
        Mockito.verify(remoteStore, Mockito.never()).contains(TOKEN, KEY);
    }

    public void testAddObserverInvokesLocalAndRemoteStores() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);

        Mockito.when(localStore.addObserver(OBSERVER)).thenReturn(true);
        Mockito.when(remoteStore.addObserver(OBSERVER)).thenReturn(true);

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final boolean response = store.addObserver(OBSERVER);

        assertTrue(response);

        Mockito.verify(localStore).addObserver(OBSERVER);
        Mockito.verify(remoteStore).addObserver(OBSERVER);
    }

    public void testRemoveObserverInvokesLocalAndRemoteStores() {
        final LocalStore localStore = Mockito.mock(LocalStore.class);
        final RemoteStore remoteStore = Mockito.mock(RemoteStore.class);

        Mockito.when(localStore.removeObserver(OBSERVER)).thenReturn(true);
        Mockito.when(remoteStore.removeObserver(OBSERVER)).thenReturn(true);

        final DefaultStore store = new DefaultStore(localStore, remoteStore);
        final boolean response = store.removeObserver(OBSERVER);

        assertTrue(response);

        Mockito.verify(localStore).removeObserver(OBSERVER);
        Mockito.verify(remoteStore).removeObserver(OBSERVER);
    }
}
