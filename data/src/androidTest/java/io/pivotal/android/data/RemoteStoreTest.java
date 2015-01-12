/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.Random;

@SuppressWarnings("unchecked")
public class RemoteStoreTest extends AndroidTestCase {

    private static final boolean RESULT = new Random().nextBoolean();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testGetInvokesRemoteClientAndObserverHandlerWithSuccessResponse() throws Exception {
        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(observerHandler, remoteClient));

        Mockito.when(remoteClient.get(Mockito.any(Request.class))).thenReturn(response);

        assertEquals(response, remoteStore.get(request));

        Mockito.verify(remoteClient).get(request);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testGetInvokesRemoteClientAndObserverHandlerWithFailureResponse() throws Exception {
        final Request request = Mockito.mock(Request.class);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(observerHandler, remoteClient));

        Mockito.doThrow(new RuntimeException()).when(remoteClient).get(Mockito.any(Request.class));

        final Response response = remoteStore.get(request);
        assertTrue(response.isFailure());
        assertEquals(request.object, response.object);

        Mockito.verify(remoteClient).get(request);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testPutInvokesRemoteClientAndObserverHandlerWithSuccessResponse() throws Exception {
        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(observerHandler, remoteClient));

        Mockito.when(remoteClient.put(Mockito.any(Request.class))).thenReturn(response);

        assertEquals(response, remoteStore.put(request));

        Mockito.verify(remoteClient).put(request);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testPutInvokesRemoteClientAndObserverHandlerWithFailureResponse() throws Exception {
        final Request request = Mockito.mock(Request.class);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(observerHandler, remoteClient));

        Mockito.doThrow(new RuntimeException()).when(remoteClient).put(Mockito.any(Request.class));

        final Response response = remoteStore.put(request);
        assertTrue(response.isFailure());
        assertEquals(request.object, response.object);

        Mockito.verify(remoteClient).put(request);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testDeleteInvokesRemoteClientAndObserverHandlerWithSuccessResponse() throws Exception {
        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(observerHandler, remoteClient));

        Mockito.when(remoteClient.delete(Mockito.any(Request.class))).thenReturn(response);

        assertEquals(response, remoteStore.delete(request));

        Mockito.verify(remoteClient).delete(request);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testDeleteInvokesRemoteClientAndObserverHandlerWithFailureResponse() throws Exception {
        final Request request = Mockito.mock(Request.class);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(observerHandler, remoteClient));

        Mockito.doThrow(new RuntimeException()).when(remoteClient).delete(Mockito.any(Request.class));

        final Response response = remoteStore.delete(request);
        assertTrue(response.isFailure());
        assertEquals(request.object, response.object);

        Mockito.verify(remoteClient).delete(request);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testAddObserverInvokesHandler() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final DataStore.Observer observer = Mockito.mock(DataStore.Observer.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(observerHandler, null));

        Mockito.when(observerHandler.addObserver(observer)).thenReturn(RESULT);

        assertEquals(RESULT, remoteStore.addObserver(observer));

        Mockito.verify(observerHandler).addObserver(observer);
    }

    public void testRemoveObserverInvokesHandler() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final DataStore.Observer observer = Mockito.mock(DataStore.Observer.class);
        final RemoteStore remoteStore = Mockito.spy(new RemoteStore(observerHandler, null));

        Mockito.when(observerHandler.removeObserver(observer)).thenReturn(RESULT);

        assertEquals(RESULT, remoteStore.removeObserver(observer));

        Mockito.verify(observerHandler).removeObserver(observer);
    }

}
