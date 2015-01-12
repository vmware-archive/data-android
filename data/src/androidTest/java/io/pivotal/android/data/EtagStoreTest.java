/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

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

    public void testGetInvokesPersistence() {
        final DataPersistence persistence = Mockito.mock(DataPersistence.class);
        final EtagStore store = new EtagStore(persistence);

        Mockito.when(persistence.getString(Mockito.anyString())).thenReturn(VALUE);

        assertEquals(VALUE, store.get(KEY));

        Mockito.verify(persistence).getString(KEY);
    }

    public void testPutInvokesPersistence() {
        final DataPersistence persistence = Mockito.mock(DataPersistence.class);
        final EtagStore store = new EtagStore(persistence);

        Mockito.when(persistence.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(VALUE);

        assertEquals(VALUE, store.put(KEY, VALUE));

        Mockito.verify(persistence).putString(KEY, VALUE);
    }
}