/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

public class DataStoreTest extends AndroidTestCase {

    public void testDataStoreResponseSuccess() {
        final DataStore.Response response = DataStore.Response.success("key", "value");

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals("key", response.key);
        assertEquals("value", response.value);
        assertNull(response.error);
    }

    public void testDataStoreResponseFailure() {
        final DataError error = new DataError(new RuntimeException());
        final DataStore.Response response = DataStore.Response.failure("key", error);

        assertEquals(DataStore.Response.Status.FAILURE, response.status);
        assertEquals("key", response.key);
        assertEquals(error, response.error);
        assertNull(response.value);
    }

    public void testDataStoreResponsePending() {
        final DataStore.Response response = DataStore.Response.pending("key", "value");

        assertEquals(DataStore.Response.Status.PENDING, response.status);
        assertEquals("key", response.key);
        assertEquals("value", response.value);
        assertNull(response.error);
    }

}