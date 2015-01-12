/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import java.util.UUID;

public class KeyValueTest extends AndroidTestCase {

    private static final String COLLECTION = UUID.randomUUID().toString();
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testConstructors() {
        final KeyValue original = new KeyValue(COLLECTION, KEY, VALUE);

        assertEquals(COLLECTION, original.collection);
        assertEquals(KEY, original.key);
        assertEquals(VALUE, original.value);

        final KeyValue copy = new KeyValue(original);

        assertEquals(COLLECTION, copy.collection);
        assertEquals(KEY, copy.key);
        assertEquals(VALUE, copy.value);
    }

    public void testGetUrl() throws Exception {
        final KeyValue keyValue = new KeyValue(COLLECTION, KEY, VALUE);

        assertEquals(Pivotal.getServiceUrl() + "/" + COLLECTION + "/" + KEY, keyValue.getUrl());
    }

}