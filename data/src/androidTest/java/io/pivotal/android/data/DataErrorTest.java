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
        final Exception exception = new DataHttpException(400, message);
        final DataError error = new DataError(exception);

        assertFalse(error.isUnauthorized());
    }

    public void testWith401Unauthorized() {
        final String message = "error";
        final Exception exception = new DataHttpException(401, message);
        final DataError error = new DataError(exception);

        assertTrue(error.isUnauthorized());
    }

    public void testWith100NotConnected() {
        final String message = "error";
        final Exception exception = new DataHttpException(100, message);
        final DataError error = new DataError(exception);

        assertTrue(error.isNotConnected());
    }

    public void testWith304NotModified() {
        final String message = "error";
        final Exception exception = new DataHttpException(304, message);
        final DataError error = new DataError(exception);

        assertTrue(error.isNotModified());
    }

    public void testWith412PreconditionFailed() {
        final String message = "error";
        final Exception exception = new DataHttpException(412, message);
        final DataError error = new DataError(exception);

        assertTrue(error.hasPreconditionFailed());
    }
}