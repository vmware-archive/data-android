/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.UUID;

@SuppressLint("CommitPrefEdits")
public class DataPersistenceTest extends AndroidTestCase {

    private static final String EMPTY = "";

    private static final String NAME = UUID.randomUUID().toString();
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testGetInvokesSharedPreferences() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);

        Mockito.when(context.getSharedPreferences(Mockito.anyString(), Mockito.anyInt())).thenReturn(preferences);
        Mockito.when(preferences.getString(Mockito.anyString(), Mockito.anyString())).thenReturn(VALUE);

        final DataPersistence persistence = new DataPersistence(context, NAME);

        assertEquals(VALUE, persistence.getString(KEY));

        Mockito.verify(context).getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Mockito.verify(preferences).getString(KEY, EMPTY);
    }

    public void testPutInvokesSharedPreferences() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(context.getSharedPreferences(Mockito.anyString(), Mockito.anyInt())).thenReturn(preferences);
        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(editor);

        final DataPersistence persistence = new DataPersistence(context, NAME);

        assertEquals(VALUE, persistence.putString(KEY, VALUE));

        Mockito.verify(context).getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Mockito.verify(editor).putString(KEY, VALUE);
    }

    public void testDeleteInvokesSharedPreferences() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(context.getSharedPreferences(Mockito.anyString(), Mockito.anyInt())).thenReturn(preferences);
        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.remove(Mockito.anyString())).thenReturn(editor);

        final DataPersistence persistence = new DataPersistence(context, NAME);

        assertEquals(EMPTY, persistence.deleteString(KEY));

        Mockito.verify(context).getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Mockito.verify(editor).remove(KEY);
    }

    public void testClearInvokesSharedPreferences() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(context.getSharedPreferences(Mockito.anyString(), Mockito.anyInt())).thenReturn(preferences);
        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.clear()).thenReturn(editor);

        final DataPersistence persistence = new DataPersistence(context, NAME);

        persistence.clear();

        Mockito.verify(context).getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Mockito.verify(editor).clear();
    }
}