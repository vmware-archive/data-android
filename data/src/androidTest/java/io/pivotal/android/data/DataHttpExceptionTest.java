/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import junit.framework.TestCase;

public class DataHttpExceptionTest extends TestCase {

    public void testWithNullMessage() {
        final DataHttpException exception = new DataHttpException(0, null);

        assertEquals(0, exception.getStatusCode());
        assertNull(exception.getMessage());
    }

    public void testWithStatusCodeAndMessage() {
        final DataHttpException exception = new DataHttpException(401, "Unauthorized");

        assertEquals(401, exception.getStatusCode());
        assertEquals("Unauthorized", exception.getMessage());
    }
}