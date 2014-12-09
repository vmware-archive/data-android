/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

public class DataStoreTest extends AndroidTestCase {

    public void testDataStoreResponseSuccess() {
        final DataStore.Response response = new DataStore.Response("key", "value");

        assertTrue(response.isSuccess());
        assertEquals("key", response.key);
        assertEquals("value", response.value);
        assertNull(response.error);
    }

    public void testDataStoreResponseFailure() {
        final DataError error = new DataError(new RuntimeException());
        final DataStore.Response response = new DataStore.Response("key", error);

        assertTrue(response.isFailure());
        assertEquals("key", response.key);
        assertEquals(error, response.error);
        assertNull(response.value);
    }

}