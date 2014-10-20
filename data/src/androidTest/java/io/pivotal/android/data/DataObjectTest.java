/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.UUID;

public class DataObjectTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final String TOKEN = UUID.randomUUID().toString();

    private static final DataObject.Observer OBSERVER = new DataObject.Observer() {
        @Override
        public void onChange(final String key, final String value) {}

        @Override
        public void onError(final String key, final DataError error) {}
    };

    private static final DataObject.Observer OBSERVER2 = new DataObject.Observer() {
        @Override
        public void onChange(final String key, final String value) {}

        @Override
        public void onError(final String key, final DataError error) {}
    };

    public void testGetInvokesDataStore() {
        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, KEY);
        Mockito.when(dataStore.get(TOKEN, KEY)).thenReturn(DataStore.Response.success(KEY, VALUE));

        assertEquals(VALUE, dataObject.get(TOKEN));

        Mockito.verify(dataStore).get(TOKEN, KEY);
    }

    public void testPutInvokesDataStore() {
        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, KEY);
        Mockito.when(dataStore.put(TOKEN, KEY, VALUE)).thenReturn(DataStore.Response.success(KEY, VALUE));

        dataObject.put(TOKEN, VALUE);

        Mockito.verify(dataStore).put(TOKEN, KEY, VALUE);
    }

    public void testDeleteInvokesDataStore() {
        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, KEY);
        Mockito.when(dataStore.delete(TOKEN, KEY)).thenReturn(DataStore.Response.success(KEY, null));

        dataObject.delete(TOKEN);

        Mockito.verify(dataStore).delete(TOKEN, KEY);
    }

    public void testAddObservers() {
        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject object = new DataObject(dataStore, null);

        assertTrue(object.addObserver(OBSERVER));
        assertTrue(object.addObserver(OBSERVER2));

        assertFalse(object.addObserver(OBSERVER));
        assertFalse(object.addObserver(OBSERVER2));
    }

    public void testAddObserversInvokesDataStoreIfObserverNotRegistered() {
        final DataObject.ObserverProxy proxy = new DataObject.ObserverProxy(OBSERVER, null);

        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, null) {

            @Override
            protected ObserverProxy createProxy(final Observer observer) {
                return proxy;
            }
        };

        assertTrue(dataObject.addObserver(OBSERVER));

        Mockito.verify(dataStore).addObserver(proxy);
    }

    public void testAddObserversDoesNotInvokesDataStoreIfObserverRegistered() {
        final DataObject.ObserverProxy proxy = new DataObject.ObserverProxy(OBSERVER, null);

        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, null);
        dataObject.getObservers().put(OBSERVER, proxy);

        assertFalse(dataObject.addObserver(OBSERVER));

        Mockito.verify(dataStore, Mockito.never()).addObserver(proxy);
    }

    public void testRemoveObservers() {
        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject object = new DataObject(dataStore, null);

        assertTrue(object.addObserver(OBSERVER));
        assertTrue(object.addObserver(OBSERVER2));

        assertTrue(object.removeObserver(OBSERVER));
        assertTrue(object.removeObserver(OBSERVER2));

        assertFalse(object.removeObserver(OBSERVER));
        assertFalse(object.removeObserver(OBSERVER2));
    }

    public void testRemoveObserversInvokesDataStoreIfObserverRegistered() {
        final DataObject.ObserverProxy proxy = new DataObject.ObserverProxy(OBSERVER, null);

        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, null);
        dataObject.getObservers().put(OBSERVER, proxy);

        assertTrue(dataObject.removeObserver(OBSERVER));

        Mockito.verify(dataStore).removeObserver(proxy);
    }

    public void testRemoveObserversDoesNotInvokesDataStoreIfObserverNotRegistered() {
        final DataObject.ObserverProxy proxy = new DataObject.ObserverProxy(OBSERVER, null);

        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, null);

        assertFalse(dataObject.removeObserver(OBSERVER));

        Mockito.verify(dataStore, Mockito.never()).removeObserver(proxy);
    }


}
