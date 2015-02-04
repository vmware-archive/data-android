/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.Random;
import java.util.UUID;

public class KeyValueObjectTest extends AndroidTestCase {

    public static class KeyValueRequest extends Request<KeyValue> {}
    public static class KeyValueResponse extends Response<KeyValue> {}
    public static interface KeyValueDataStore extends DataStore<KeyValue> {}
    public static interface KeyValueListener extends DataStore.Listener<KeyValue> {}
    public static interface KeyValueObserver extends DataStore.Observer<KeyValue> {}

    private static final String COLLECTION = UUID.randomUUID().toString();
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final boolean RESULT = new Random().nextBoolean();


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testGetInvokesDataStore() {
        final DataStore<KeyValue> dataStore = Mockito.mock(KeyValueDataStore.class);
        final Request<KeyValue> request = Mockito.mock(KeyValueRequest.class);
        final Response<KeyValue> response = Mockito.mock(KeyValueResponse.class);
        final KeyValueObject keyValueObject = Mockito.spy(new KeyValueObject(dataStore, COLLECTION, KEY));

        Mockito.stub(keyValueObject.createRequest(Mockito.anyString())).toReturn(request);
        Mockito.when(dataStore.get(request)).thenReturn(response);

        assertEquals(response, keyValueObject.get());

        Mockito.verify(keyValueObject).createRequest(null);
        Mockito.verify(dataStore).get(request);
    }

    public void testGetAsyncInvokesDataStore() {
        final DataStore<KeyValue> dataStore = Mockito.mock(KeyValueDataStore.class);
        final Request<KeyValue> request = Mockito.mock(KeyValueRequest.class);
        final DataStore.Listener<KeyValue> listener = Mockito.mock(KeyValueListener.class);
        final KeyValueObject keyValueObject = Mockito.spy(new KeyValueObject(dataStore, COLLECTION, KEY));

        Mockito.stub(keyValueObject.createRequest(Mockito.anyString())).toReturn(request);

        keyValueObject.get(listener);

        Mockito.verify(keyValueObject).createRequest(null);
        Mockito.verify(dataStore).get(request, listener);
    }

    public void testPutInvokesDataStore() {
        final DataStore<KeyValue> dataStore = Mockito.mock(KeyValueDataStore.class);
        final Request<KeyValue> request = Mockito.mock(KeyValueRequest.class);
        final Response<KeyValue> response = Mockito.mock(KeyValueResponse.class);
        final KeyValueObject keyValueObject = Mockito.spy(new KeyValueObject(dataStore, COLLECTION, KEY));

        Mockito.stub(keyValueObject.createRequest(Mockito.anyString())).toReturn(request);
        Mockito.when(dataStore.put(request)).thenReturn(response);

        assertEquals(response, keyValueObject.put(VALUE));

        Mockito.verify(keyValueObject).createRequest(VALUE);
        Mockito.verify(dataStore).put(request);
    }

    public void testPutAsyncInvokesDataStore() {
        final DataStore<KeyValue> dataStore = Mockito.mock(KeyValueDataStore.class);
        final Request<KeyValue> request = Mockito.mock(KeyValueRequest.class);
        final DataStore.Listener<KeyValue> listener = Mockito.mock(KeyValueListener.class);
        final KeyValueObject keyValueObject = Mockito.spy(new KeyValueObject(dataStore, COLLECTION, KEY));

        Mockito.stub(keyValueObject.createRequest(Mockito.anyString())).toReturn(request);

        keyValueObject.put(VALUE, listener);

        Mockito.verify(keyValueObject).createRequest(VALUE);
        Mockito.verify(dataStore).put(request, listener);
    }

    public void testDeleteInvokesDataStore() {
        final DataStore<KeyValue> dataStore = Mockito.mock(KeyValueDataStore.class);
        final Request<KeyValue> request = Mockito.mock(KeyValueRequest.class);
        final Response<KeyValue> response = Mockito.mock(KeyValueResponse.class);
        final KeyValueObject keyValueObject = Mockito.spy(new KeyValueObject(dataStore, COLLECTION, KEY));

        Mockito.stub(keyValueObject.createRequest(Mockito.anyString())).toReturn(request);
        Mockito.when(dataStore.delete(request)).thenReturn(response);

        assertEquals(response, keyValueObject.delete());

        Mockito.verify(keyValueObject).createRequest(null);
        Mockito.verify(dataStore).delete(request);
    }

    public void testDeleteAsyncInvokesDataStore() {
        final DataStore<KeyValue> dataStore = Mockito.mock(KeyValueDataStore.class);
        final Request<KeyValue> request = Mockito.mock(KeyValueRequest.class);
        final DataStore.Listener<KeyValue> listener = Mockito.mock(KeyValueListener.class);
        final KeyValueObject keyValueObject = Mockito.spy(new KeyValueObject(dataStore, COLLECTION, KEY));

        Mockito.stub(keyValueObject.createRequest(Mockito.anyString())).toReturn(request);

        keyValueObject.delete(listener);

        Mockito.verify(keyValueObject).createRequest(null);
        Mockito.verify(dataStore).delete(request, listener);
    }

    public void testAddObserversInvokesDataStore() {
        final DataStore<KeyValue> dataStore = Mockito.mock(KeyValueDataStore.class);
        final DataStore.Observer<KeyValue> observer = Mockito.mock(KeyValueObserver.class);
        final KeyValueObject keyValueObject = new KeyValueObject(dataStore, null, null);

        Mockito.when(dataStore.addObserver(observer)).thenReturn(RESULT);

        assertEquals(RESULT, keyValueObject.addObserver(observer));

        Mockito.verify(dataStore).addObserver(observer);
    }

    public void testRemoveObserversInvokesDataStore() {
        final DataStore<KeyValue> dataStore = Mockito.mock(KeyValueDataStore.class);
        final DataStore.Observer<KeyValue> observer = Mockito.mock(KeyValueObserver.class);
        final KeyValueObject keyValueObject = new KeyValueObject(dataStore, null, null);

        Mockito.when(dataStore.removeObserver(observer)).thenReturn(RESULT);

        assertEquals(RESULT, keyValueObject.removeObserver(observer));

        Mockito.verify(dataStore).removeObserver(observer);
    }

}
