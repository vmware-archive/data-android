/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.UUID;

public class LocalStoreTest extends AndroidTestCase {

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

    public void testContainsInvokesSharedPreferences() {
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        Mockito.when(preferences.contains(KEY)).thenReturn(true);

        final LocalStore store = new LocalStore(COLLECTION, null, preferences);

        assertTrue(store.contains(TOKEN, KEY));

        Mockito.verify(preferences).contains(KEY);
    }

    public void testGetInvokesSharedPreferences() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final LocalStore store = new LocalStore(COLLECTION, observerHandler, preferences);

        Mockito.when(preferences.getString(KEY, "")).thenReturn(VALUE);

        final DataStore.Response response = store.get(TOKEN, KEY);

        assertTrue(response.isSuccess());
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(preferences).getString(KEY, "");
        Mockito.verify(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));
    }

    public void testPutInvokesSharedPreferences() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        final LocalStore store = new LocalStore(COLLECTION, observerHandler, preferences);

        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.putString(KEY, VALUE)).thenReturn(editor);

        final DataStore.Response response = store.put(TOKEN, KEY, VALUE);

        assertTrue(response.isSuccess());
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(preferences).edit();
        Mockito.verify(editor).putString(KEY, VALUE);
        Mockito.verify(editor).apply();
        Mockito.verify(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));
    }

    public void testDeleteInvokesSharedPreferences() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        final LocalStore store = new LocalStore(COLLECTION, observerHandler, preferences);

        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.remove(KEY)).thenReturn(editor);

        final DataStore.Response response = store.delete(TOKEN, KEY);

        assertTrue(response.isSuccess());
        assertEquals(KEY, response.key);
        assertEquals("", response.value);

        Mockito.verify(preferences).edit();
        Mockito.verify(editor).remove(KEY);
        Mockito.verify(editor).apply();
        Mockito.verify(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));
    }

    public void testAddObserverInvokesHandler() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final LocalStore localStore = Mockito.spy(new LocalStore(COLLECTION, observerHandler, null));

        Mockito.when(observerHandler.addObserver(OBSERVER)).thenReturn(RESULT);

        assertEquals(RESULT, localStore.addObserver(OBSERVER));

        Mockito.verify(observerHandler).addObserver(OBSERVER);
    }

    public void testRemoveObserverInvokesHandler() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final LocalStore localStore = Mockito.spy(new LocalStore(COLLECTION, observerHandler, null));

        Mockito.when(observerHandler.removeObserver(OBSERVER)).thenReturn(RESULT);

        assertEquals(RESULT, localStore.removeObserver(OBSERVER));

        Mockito.verify(observerHandler).removeObserver(OBSERVER);
    }
}
