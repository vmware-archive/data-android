/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.Set;
import java.util.UUID;

public class LocalStoreTest extends AndroidTestCase {

    private static final String COLLECTION = UUID.randomUUID().toString();
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

    public void testContainsInvokesSharedPreferences() {
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        Mockito.when(preferences.contains(KEY)).thenReturn(true);

        final LocalStore store = getLocalStore(null, preferences);

        assertTrue(store.contains(TOKEN, KEY));

        Mockito.verify(preferences).contains(KEY);
    }

    public void testGetInvokesSharedPreferences() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        Mockito.when(preferences.getString(KEY, "")).thenReturn(VALUE);
        Mockito.doNothing().when(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));

        final LocalStore store = getLocalStore(observerHandler, preferences);
        final DataStore.Response response = store.get(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(preferences).getString(KEY, "");
        Mockito.verify(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));
    }

    public void testPutInvokesSharedPreferences() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.putString(KEY, VALUE)).thenReturn(editor);
        Mockito.doNothing().when(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));

        final LocalStore store = getLocalStore(observerHandler, preferences);
        final DataStore.Response response = store.put(TOKEN, KEY, VALUE);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
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

        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.remove(KEY)).thenReturn(editor);
        Mockito.doNothing().when(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));

        final LocalStore store = getLocalStore(observerHandler, preferences);
        final DataStore.Response response = store.delete(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals(KEY, response.key);

        Mockito.verify(preferences).edit();
        Mockito.verify(editor).remove(KEY);
        Mockito.verify(editor).apply();
        Mockito.verify(observerHandler).notifyResponse(Mockito.any(DataStore.Response.class));
    }

    public void testAddObserverIfNotAlreadyRegistered() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        final LocalStore store = getLocalStore(observerHandler, preferences);

        assertTrue(store.addObserver(OBSERVER));
    }

    public void testAddObserverIfAlreadyRegistered() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        final LocalStore store = getLocalStore(observerHandler, preferences);
        store.getObservers().add(OBSERVER);

        assertFalse(store.addObserver(OBSERVER));
    }

    public void testRemoveObserverIfAlreadyRegistered() {
        final ObserverHandler observerHandler = Mockito.mock(ObserverHandler.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        final LocalStore store = getLocalStore(observerHandler, preferences);
        store.getObservers().add(OBSERVER);

        assertTrue(store.removeObserver(OBSERVER));
    }

    public void testRemoveObserverIfNotAlreadyRegistered() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        Mockito.when(context.getSharedPreferences(COLLECTION, Context.MODE_PRIVATE)).thenReturn(preferences);

        final LocalStore store = new LocalStore(context, COLLECTION);

        assertFalse(store.removeObserver(OBSERVER));
    }


    private LocalStore getLocalStore(final ObserverHandler observerHandler, final SharedPreferences preferences) {
        return new LocalStore(null, COLLECTION) {

            @Override
            protected SharedPreferences createSharedPreferences(final Context context, final String collection) {
                return preferences;
            }

            @Override
            protected ObserverHandler createObserverHandler(final Set<Observer> observers, final Object lock) {
                return observerHandler;
            }
        };
    }
}
