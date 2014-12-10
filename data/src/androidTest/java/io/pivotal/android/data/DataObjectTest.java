/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.Random;
import java.util.UUID;

public class DataObjectTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final String TOKEN = UUID.randomUUID().toString();
    private static final boolean RESULT = new Random().nextBoolean();

    private static final DataStore.Observer OBSERVER = new DataStore.Observer() {
        @Override
        public void onResponse(final DataStore.Response response) {}
    };

    public void testGetInvokesDataStore() {
        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, KEY);
        final DataStore.Response response = new DataStore.Response(KEY, VALUE);

        Mockito.when(dataStore.get(TOKEN, KEY)).thenReturn(response);

        assertEquals(response, dataObject.get(TOKEN));

        Mockito.verify(dataStore).get(TOKEN, KEY);
    }

    public void testPutInvokesDataStore() {
        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, KEY);
        final DataStore.Response response = new DataStore.Response(KEY, VALUE);

        Mockito.when(dataStore.put(TOKEN, KEY, VALUE)).thenReturn(response);

        assertEquals(response, dataObject.put(TOKEN, VALUE));

        Mockito.verify(dataStore).put(TOKEN, KEY, VALUE);
    }

    public void testDeleteInvokesDataStore() {
        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, KEY);
        final DataStore.Response response = new DataStore.Response(KEY, "");

        Mockito.when(dataStore.delete(TOKEN, KEY)).thenReturn(response);

        assertEquals(response, dataObject.delete(TOKEN));

        Mockito.verify(dataStore).delete(TOKEN, KEY);
    }

    public void testAddObserversInvokesDataStore() {
        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, null);

        Mockito.when(dataStore.addObserver(OBSERVER)).thenReturn(RESULT);

        assertEquals(RESULT, dataObject.addObserver(OBSERVER));

        Mockito.verify(dataStore).addObserver(OBSERVER);
    }

    public void testRemoveObserversInvokesDataStore() {
        final DataStore dataStore = Mockito.mock(DataStore.class);
        final DataObject dataObject = new DataObject(dataStore, null);

        Mockito.when(dataStore.removeObserver(OBSERVER)).thenReturn(RESULT);

        assertEquals(RESULT, dataObject.removeObserver(OBSERVER));

        Mockito.verify(dataStore).removeObserver(OBSERVER);
    }

}
