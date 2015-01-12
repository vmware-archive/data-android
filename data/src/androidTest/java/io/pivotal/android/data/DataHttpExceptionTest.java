/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import junit.framework.TestCase;

import java.util.Random;
import java.util.UUID;

public class DataHttpExceptionTest extends TestCase {

    private static final int CODE = new Random().nextInt();
    private static final String MESSAGE = UUID.randomUUID().toString();

    public void testConstructor() {
        final DataHttpException exception = new DataHttpException(CODE, MESSAGE);

        assertEquals(CODE, exception.getStatusCode());
        assertEquals(MESSAGE, exception.getMessage());
    }
}