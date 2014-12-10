/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import java.util.UUID;

public class DataStoreTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();

    public void testDataStoreResponseSuccess() {
        final DataStore.Response response = new DataStore.Response(KEY, VALUE);

        assertTrue(response.isSuccess());
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);
        assertNull(response.error);
    }

    public void testDataStoreResponseFailure() {
        final DataError error = new DataError(new RuntimeException());
        final DataStore.Response response = new DataStore.Response(KEY, error);

        assertTrue(response.isFailure());
        assertEquals(KEY, response.key);
        assertEquals(error, response.error);
        assertNull(response.value);
    }

    public void testDataStoreResponseFailureNotModified() {
        final DataError error = new DataError(new DataHttpException(304, null));
        final DataStore.Response response = new DataStore.Response(KEY, error);

        assertTrue(response.isNotModified());
    }

    public void testDataStoreResponseFailurePreconditionFailed() {
        final DataError error = new DataError(new DataHttpException(412, null));
        final DataStore.Response response = new DataStore.Response(KEY, error);

        assertTrue(response.hasPreconditionFailed());
    }
}