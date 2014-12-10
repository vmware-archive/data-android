/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.Random;
import java.util.UUID;

public class RemoteStoreTest extends AndroidTestCase {

    private static final String URL = "http://example.com";
    private static final String COLLECTION = UUID.randomUUID().toString();
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final String TOKEN = UUID.randomUUID().toString();
    private static final boolean RESULT = new Random().nextBoolean();

    private static final DataStore.Observer OBSERVER = new DataStore.Observer() {
        @Override
        public void onResponse(final DataStore.Response response) {}
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testContainsInvokesGetWithSuccess() {
        final Context context = Mockito.mock(Context.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(context, COLLECTION));

        Mockito.doReturn(new DataStore.Response(KEY, VALUE)).when(remoteStore).get(TOKEN, KEY);

        assertTrue(remoteStore.contains(TOKEN, KEY));

        Mockito.verify(remoteStore).get(TOKEN, KEY);
    }

    public void testContainsInvokesGetWithFailure() {
        final Context context = Mockito.mock(Context.class);
        final DataError error = new DataError(new Exception());
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(context, COLLECTION));

        Mockito.doReturn(new DataStore.Response(KEY, error)).when(remoteStore).get(TOKEN, KEY);

        assertFalse(remoteStore.contains(TOKEN, KEY));

        Mockito.verify(remoteStore).get(TOKEN, KEY);
    }

    public void testGetInvokesRemoteClientAndObserverHandlerWithSuccessResponse() throws Exception {
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(COLLECTION, observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getCollectionUrl(KEY);
        Mockito.when(remoteClient.get(TOKEN, URL)).thenReturn(VALUE);

        final DataStore.Response response = remoteStore.get(TOKEN, KEY);

        assertTrue(response.isSuccess());
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));
        Mockito.verify(remoteClient).get(TOKEN, URL);
    }

    public void testGetInvokesRemoteClientAndObserverHandlerWithFailureResponse() throws Exception {
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(COLLECTION, observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getCollectionUrl(KEY);
        Mockito.doThrow(new RuntimeException()).when(remoteClient).get(TOKEN, URL);

        final DataStore.Response response = remoteStore.get(TOKEN, KEY);

        assertTrue(response.isFailure());
        assertEquals(KEY, response.key);
        assertNotNull(response.error);

        Mockito.verify(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));
        Mockito.verify(remoteClient).get(TOKEN, URL);
    }

    public void testPutInvokesRemoteClientAndObserverHandlerWithSuccessResponse() throws Exception {
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(COLLECTION, observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getCollectionUrl(KEY);
        Mockito.when(remoteClient.put(TOKEN, URL, VALUE)).thenReturn(VALUE);

        final DataStore.Response response = remoteStore.put(TOKEN, KEY, VALUE);

        assertTrue(response.isSuccess());
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));
        Mockito.verify(remoteClient).put(TOKEN, URL, VALUE);
    }

    public void testPutInvokesRemoteClientAndObserverHandlerWithFailureResponse() throws Exception {
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(COLLECTION, observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getCollectionUrl(KEY);
        Mockito.doThrow(new RuntimeException()).when(remoteClient).put(TOKEN, URL, VALUE);

        final DataStore.Response response = remoteStore.put(TOKEN, KEY, VALUE);

        assertTrue(response.isFailure());
        assertEquals(KEY, response.key);
        assertNotNull(response.error);

        Mockito.verify(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));
        Mockito.verify(remoteClient).put(TOKEN, URL, VALUE);
    }

    public void testDeleteInvokesRemoteClientAndObserverHandlerWithSuccessResponse() throws Exception {
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(COLLECTION, observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getCollectionUrl(KEY);
        Mockito.when(remoteClient.delete(TOKEN, URL)).thenReturn(VALUE);

        final DataStore.Response response = remoteStore.delete(TOKEN, KEY);

        assertTrue(response.isSuccess());
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));
        Mockito.verify(remoteClient).delete(TOKEN, URL);
    }

    public void testDeleteInvokesRemoteClientAndObserverHandlerWithFailureResponse() throws Exception {
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(COLLECTION, observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getCollectionUrl(KEY);
        Mockito.doThrow(new RuntimeException()).when(remoteClient).delete(TOKEN, URL);

        final DataStore.Response response = remoteStore.delete(TOKEN, KEY);

        assertTrue(response.isFailure());
        assertEquals(KEY, response.key);
        assertNotNull(response.error);

        Mockito.verify(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));
        Mockito.verify(remoteClient).delete(TOKEN, URL);
    }

    public void testAddObserverInvokesHandler() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(COLLECTION, observerHandler, null));

        Mockito.when(observerHandler.addObserver(OBSERVER)).thenReturn(RESULT);

        assertEquals(RESULT, remoteStore.addObserver(OBSERVER));

        Mockito.verify(observerHandler).addObserver(OBSERVER);
    }

    public void testRemoveObserverInvokesHandler() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(COLLECTION, observerHandler, null));

        Mockito.when(observerHandler.removeObserver(OBSERVER)).thenReturn(RESULT);

        assertEquals(RESULT, remoteStore.removeObserver(OBSERVER));

        Mockito.verify(observerHandler).removeObserver(OBSERVER);
    }

}
