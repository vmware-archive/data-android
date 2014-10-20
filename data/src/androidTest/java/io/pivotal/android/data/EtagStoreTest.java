/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.UUID;

public class EtagStoreTest extends AndroidTestCase {

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

        Mockito.when(context.getSharedPreferences("etags", Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.getString(KEY, null)).thenReturn(VALUE);

        final EtagStore store = new EtagStore.Default(context);

        assertEquals(VALUE, store.get(KEY));

        Mockito.verify(context).getSharedPreferences("etags", Context.MODE_PRIVATE);
        Mockito.verify(preferences).getString(KEY, null);
    }

    public void testPutInvokesSharedPreferences() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(context.getSharedPreferences("etags", Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.putString(KEY, VALUE)).thenReturn(editor);
        Mockito.when(editor.commit()).thenReturn(true);

        final EtagStore store = new EtagStore.Default(context);

        store.put(KEY, VALUE);

        Mockito.verify(context).getSharedPreferences("etags", Context.MODE_PRIVATE);
        Mockito.verify(preferences).edit();
        Mockito.verify(editor).putString(KEY, VALUE);
        Mockito.verify(editor).commit();
    }
}