/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.Random;
import java.util.UUID;

public class KeyValueStoreTest extends AndroidTestCase {

    public static class KeyValueObserverHandler extends ObserverHandler<KeyValue> {}
    public interface KeyValueObserver extends DataStore.Observer<KeyValue> {}

    private static final String COLLECTION = UUID.randomUUID().toString();
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final boolean RESULT = new Random().nextBoolean();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testGetInvokesPersistence() {
        final ObserverHandler<KeyValue> observerHandler = Mockito.mock(KeyValueObserverHandler.class);
        final DataPersistence persistence = Mockito.mock(DataPersistence.class);
        final Request<KeyValue> request = new Request<KeyValue>(null, new KeyValue(COLLECTION, KEY, null));
        final KeyValueStore store = new KeyValueStore(observerHandler, persistence);

        Mockito.when(persistence.getString(Mockito.anyString())).thenReturn(VALUE);

        final Response<KeyValue> response = store.get(request);

        assertTrue(response.isSuccess());
        assertEquals(KEY, response.object.key);
        assertEquals(VALUE, response.object.value);

        Mockito.verify(persistence).getString(COLLECTION + ":" + KEY);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testPutInvokesPersistence() {
        final ObserverHandler<KeyValue> observerHandler = Mockito.mock(KeyValueObserverHandler.class);
        final DataPersistence persistence = Mockito.mock(DataPersistence.class);
        final Request<KeyValue> request = new Request<KeyValue>(null, new KeyValue(COLLECTION, KEY, VALUE));
        final KeyValueStore store = new KeyValueStore(observerHandler, persistence);

        Mockito.when(persistence.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(VALUE);

        final Response<KeyValue> response = store.put(request);

        assertTrue(response.isSuccess());
        assertEquals(KEY, response.object.key);
        assertEquals(VALUE, response.object.value);

        Mockito.verify(persistence).putString(COLLECTION + ":" + KEY, VALUE);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testDeleteInvokesPersistence() {
        final ObserverHandler<KeyValue> observerHandler = Mockito.mock(KeyValueObserverHandler.class);
        final DataPersistence persistence = Mockito.mock(DataPersistence.class);
        final Request<KeyValue> request = new Request<KeyValue>(null, new KeyValue(COLLECTION, KEY, null));
        final KeyValueStore store = new KeyValueStore(observerHandler, persistence);

        Mockito.when(persistence.deleteString(Mockito.anyString())).thenReturn("");

        final Response<KeyValue> response = store.delete(request);

        assertTrue(response.isSuccess());
        assertEquals(KEY, response.object.key);
        assertEquals("", response.object.value);

        Mockito.verify(persistence).deleteString(COLLECTION + ":" + KEY);
        Mockito.verify(observerHandler).notifyResponse(response);
    }

    public void testAddObserverInvokesHandler() {
        final ObserverHandler<KeyValue> observerHandler = Mockito.mock(KeyValueObserverHandler.class);
        final DataStore.Observer<KeyValue> observer = Mockito.mock(KeyValueObserver.class);
        final KeyValueStore keyValueStore = Mockito.spy(new KeyValueStore(observerHandler, null));

        Mockito.when(observerHandler.addObserver(observer)).thenReturn(RESULT);

        assertEquals(RESULT, keyValueStore.addObserver(observer));

        Mockito.verify(observerHandler).addObserver(observer);
    }

    public void testRemoveObserverInvokesHandler() {
        final ObserverHandler<KeyValue> observerHandler = Mockito.mock(KeyValueObserverHandler.class);
        final DataStore.Observer<KeyValue> observer = Mockito.mock(KeyValueObserver.class);
        final KeyValueStore keyValueStore = Mockito.spy(new KeyValueStore(observerHandler, null));

        Mockito.when(observerHandler.removeObserver(observer)).thenReturn(RESULT);

        assertEquals(RESULT, keyValueStore.removeObserver(observer));

        Mockito.verify(observerHandler).removeObserver(observer);
    }
}
