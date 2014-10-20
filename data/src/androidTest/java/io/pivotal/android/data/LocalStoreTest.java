/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import org.mockito.ArgumentCaptor;
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
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        Mockito.when(context.getSharedPreferences(COLLECTION, Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.contains(KEY)).thenReturn(true);

        final LocalStore store = new LocalStore(context, COLLECTION);

        assertTrue(store.contains(TOKEN, KEY));

        Mockito.verify(context).getSharedPreferences(COLLECTION, Context.MODE_PRIVATE);
        Mockito.verify(preferences).contains(KEY);
    }

    public void testGetInvokesSharedPreferences() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        Mockito.when(context.getSharedPreferences(COLLECTION, Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.getString(KEY, null)).thenReturn(VALUE);

        final LocalStore store = new LocalStore(context, COLLECTION);
        final DataStore.Response response = store.get(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(context).getSharedPreferences(COLLECTION, Context.MODE_PRIVATE);
        Mockito.verify(preferences).getString(KEY, null);
    }

    public void testPutInvokesSharedPreferences() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(context.getSharedPreferences(COLLECTION, Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.putString(KEY, VALUE)).thenReturn(editor);

        final LocalStore store = new LocalStore(context, COLLECTION);
        final DataStore.Response response = store.put(TOKEN, KEY, VALUE);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        Mockito.verify(context).getSharedPreferences(COLLECTION, Context.MODE_PRIVATE);
        Mockito.verify(preferences).edit();
        Mockito.verify(editor).putString(KEY, VALUE);
        Mockito.verify(editor).apply();
    }

    public void testDeleteInvokesSharedPreferences() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(context.getSharedPreferences(COLLECTION, Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.remove(KEY)).thenReturn(editor);

        final LocalStore store = new LocalStore(context, COLLECTION);
        final DataStore.Response response = store.delete(TOKEN, KEY);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals(KEY, response.key);

        Mockito.verify(context).getSharedPreferences(COLLECTION, Context.MODE_PRIVATE);
        Mockito.verify(preferences).edit();
        Mockito.verify(editor).remove(KEY);
        Mockito.verify(editor).apply();
    }

    public void testPreferencesListenerPostsResponseToHandler() {
        final Context context = Mockito.mock(Context.class);
        final ObserverHandler handler = Mockito.mock(ObserverHandler.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        Mockito.when(context.getSharedPreferences(COLLECTION, Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.getString(KEY, null)).thenReturn(VALUE);

        final LocalStore store = new LocalStore(context, COLLECTION) {
            @Override
            protected ObserverHandler createObserverHandler(final Set<Observer> observers, final Object lock) {
                return handler;
            }
        };

        final ArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> argument = ArgumentCaptor.forClass(SharedPreferences.OnSharedPreferenceChangeListener.class);
        Mockito.verify(preferences).registerOnSharedPreferenceChangeListener(argument.capture());
        argument.getValue().onSharedPreferenceChanged(preferences, KEY);

        Mockito.verify(handler).postResponse(Mockito.any(DataStore.Response.class));
    }

    public void testAddObserverIfNotAlreadyRegistered() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        Mockito.when(context.getSharedPreferences(COLLECTION, Context.MODE_PRIVATE)).thenReturn(preferences);

        final LocalStore store = new LocalStore(context, COLLECTION);

        assertTrue(store.addObserver(OBSERVER));
    }

    public void testAddObserverIfAlreadyRegistered() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        Mockito.when(context.getSharedPreferences(COLLECTION, Context.MODE_PRIVATE)).thenReturn(preferences);

        final LocalStore store = new LocalStore(context, COLLECTION);
        store.getObservers().add(OBSERVER);

        assertFalse(store.addObserver(OBSERVER));
    }

    public void testRemoveObserverIfAlreadyRegistered() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        Mockito.when(context.getSharedPreferences(COLLECTION, Context.MODE_PRIVATE)).thenReturn(preferences);

        final LocalStore store = new LocalStore(context, COLLECTION);
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
}
