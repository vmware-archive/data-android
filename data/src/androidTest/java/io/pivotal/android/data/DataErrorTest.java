/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import java.util.Random;
import java.util.UUID;

public class DataErrorTest extends AndroidTestCase {

    private static final int CODE = new Random().nextInt();
    private static final String MESSAGE = UUID.randomUUID().toString();

    public void testNullExceptionThrowsNullPointer() {
        try {
            new DataError(null);
            fail();
        } catch (final NullPointerException e) {
            assertNotNull(e);
        }
    }

    public void testWithStandardException() {
        final Exception exception = new Exception(MESSAGE);
        final DataError error = new DataError(exception);

        assertEquals(-1, error.getCode());
        assertEquals(MESSAGE, error.getMessage());
    }

    public void testWithDataException() {
        final Exception exception = new DataHttpException(CODE, MESSAGE);
        final DataError error = new DataError(exception);

        assertEquals(CODE, error.getCode());
        assertEquals(MESSAGE, error.getMessage());
    }

    public void testWith401Unauthorized() {
        final Exception exception = new DataHttpException(401, MESSAGE);
        final DataError error = new DataError(exception);

        assertTrue(error.isUnauthorized());
    }

    public void testWith304NotModified() {
        final Exception exception = new DataHttpException(304, MESSAGE);
        final DataError error = new DataError(exception);

        assertTrue(error.isNotModified());
    }

    public void testWith412PreconditionFailed() {
        final Exception exception = new DataHttpException(412, MESSAGE);
        final DataError error = new DataError(exception);

        assertTrue(error.hasPreconditionFailed());
    }
}