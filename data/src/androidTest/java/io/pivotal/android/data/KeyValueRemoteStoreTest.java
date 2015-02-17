/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.Random;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class KeyValueRemoteStoreTest extends AndroidTestCase {

    private static final String URL = UUID.randomUUID().toString();
    private static final String COLLECTION = UUID.randomUUID().toString();
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();

    private static final byte[] DATA = UUID.randomUUID().toString().getBytes();
    private static final boolean FORCE = new Random().nextBoolean();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testGetInvokesRemoteClientAndObserverHandlerWithSuccessResponse() throws Exception {
        final Request request = new Request.Get<KeyValue>(new KeyValue(COLLECTION, KEY, null), FORCE);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final KeyValueRemoteStore remoteStore = Mockito.spy(new KeyValueRemoteStore(observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getUrl(Mockito.any(KeyValue.class));
        Mockito.when(remoteClient.get(Mockito.anyString(), Mockito.eq(FORCE))).thenReturn(VALUE);

        final Response<KeyValue> response = remoteStore.execute(request);

        assertEquals(KEY, response.object.key);
        assertEquals(VALUE, response.object.value);
        assertEquals(COLLECTION, response.object.collection);

        Mockito.verify(remoteClient).get(URL, FORCE);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testGetInvokesRemoteClientAndObserverHandlerWithFailureResponse() throws Exception {
        final Request request = new Request.Get<KeyValue>(new KeyValue(COLLECTION, KEY, null), FORCE);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final KeyValueRemoteStore remoteStore = Mockito.spy(new KeyValueRemoteStore(observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getUrl(Mockito.any(KeyValue.class));
        Mockito.doThrow(new RuntimeException()).when(remoteClient).get(Mockito.anyString(), Mockito.eq(FORCE));

        final Response response = remoteStore.execute(request);

        assertTrue(response.isFailure());
        assertEquals(request.object, response.object);

        Mockito.verify(remoteClient).get(URL, FORCE);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testPutInvokesRemoteClientAndObserverHandlerWithSuccessResponse() throws Exception {
        final Request request = new Request.Put<KeyValue>(new KeyValue(COLLECTION, KEY, VALUE), FORCE);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final KeyValueRemoteStore remoteStore = Mockito.spy(new KeyValueRemoteStore(observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getUrl(Mockito.any(KeyValue.class));
        Mockito.doReturn(DATA).when(remoteStore).getEntity(Mockito.any(KeyValue.class));
        Mockito.when(remoteClient.put(Mockito.anyString(), Mockito.any(byte[].class), Mockito.eq(FORCE))).thenReturn(VALUE);

        final Response<KeyValue> response = remoteStore.execute(request);

        assertEquals(KEY, response.object.key);
        assertEquals(VALUE, response.object.value);
        assertEquals(COLLECTION, response.object.collection);

        Mockito.verify(remoteClient).put(URL, DATA, FORCE);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testPutInvokesRemoteClientAndObserverHandlerWithFailureResponse() throws Exception {
        final Request request = new Request.Put<KeyValue>(new KeyValue(COLLECTION, KEY, VALUE), FORCE);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final KeyValueRemoteStore remoteStore = Mockito.spy(new KeyValueRemoteStore(observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getUrl(Mockito.any(KeyValue.class));
        Mockito.doReturn(DATA).when(remoteStore).getEntity(Mockito.any(KeyValue.class));
        Mockito.doThrow(new RuntimeException()).when(remoteClient).put(Mockito.anyString(), Mockito.any(byte[].class), Mockito.eq(FORCE));

        final Response response = remoteStore.execute(request);

        assertTrue(response.isFailure());
        assertEquals(request.object, response.object);

        Mockito.verify(remoteClient).put(URL, DATA, FORCE);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testDeleteInvokesRemoteClientAndObserverHandlerWithSuccessResponse() throws Exception {
        final Request request = new Request.Delete<KeyValue>(new KeyValue(COLLECTION, KEY, null), FORCE);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final KeyValueRemoteStore remoteStore = Mockito.spy(new KeyValueRemoteStore(observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getUrl(Mockito.any(KeyValue.class));
        Mockito.when(remoteClient.delete(Mockito.anyString(), Mockito.eq(FORCE))).thenReturn("");

        final Response<KeyValue> response = remoteStore.execute(request);

        assertEquals(KEY, response.object.key);
        assertEquals(COLLECTION, response.object.collection);

        Mockito.verify(remoteClient).delete(URL, FORCE);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testDeleteInvokesRemoteClientAndObserverHandlerWithFailureResponse() throws Exception {
        final Request request = new Request.Delete<KeyValue>(new KeyValue(COLLECTION, KEY, null), FORCE);
        final RemoteClient remoteClient = Mockito.mock(RemoteClient.class);
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final KeyValueRemoteStore remoteStore = Mockito.spy(new KeyValueRemoteStore(observerHandler, remoteClient));

        Mockito.doReturn(URL).when(remoteStore).getUrl(Mockito.any(KeyValue.class));
        Mockito.doThrow(new RuntimeException()).when(remoteClient).delete(Mockito.anyString(), Mockito.eq(FORCE));

        final Response response = remoteStore.execute(request);

        assertTrue(response.isFailure());
        assertEquals(request.object, response.object);

        Mockito.verify(remoteClient).delete(URL, FORCE);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

}
