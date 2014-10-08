/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import junit.framework.TestCase;

public class DataExceptionTest extends TestCase {

    public void testWithNulMessage() {
        final DataException exception = new DataException(0, null);

        assertEquals(0, exception.getStatusCode());
        assertNull(exception.getMessage());
    }

    public void testWithStatusCodeAndMessage() {
        final DataException exception = new DataException(401, "Unauthorized");

        assertEquals(401, exception.getStatusCode());
        assertEquals("Unauthorized", exception.getMessage());
    }
}