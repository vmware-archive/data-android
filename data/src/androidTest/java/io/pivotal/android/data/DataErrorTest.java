/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

public class DataErrorTest extends AndroidTestCase {

    public void testNullExceptionThrowsNullPointer() {
        try {
            new DataError(null);
            fail();
        } catch (final NullPointerException e) {
            assertNotNull(e);
        }
    }

    public void testWithStandardException() {
        final String message = "error";
        final Exception exception = new Exception(message);
        final DataError error = new DataError(exception);

        assertEquals(-1, error.getCode());
        assertEquals(message, error.getMessage());
    }

    public void testWithDataException() {
        final String message = "error";
        final Exception exception = new DataException(400, message);
        final DataError error = new DataError(exception);

        assertEquals(400, error.getCode());
        assertEquals(message, error.getMessage());
        assertFalse(error.isUnauthorized());
    }

    public void testWith401DataException() {
        final String message = "error";
        final Exception exception = new DataException(401, message);
        final DataError error = new DataError(exception);

        assertEquals(401, error.getCode());
        assertEquals(message, error.getMessage());
        assertTrue(error.isUnauthorized());
    }

}