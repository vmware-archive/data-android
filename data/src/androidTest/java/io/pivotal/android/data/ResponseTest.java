/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import java.util.UUID;

public class ResponseTest extends AndroidTestCase {

    public void testIsSuccess() {
        final Response<Object> response = new Response<Object>();

        assertTrue(response.isSuccess());

        response.error = new DataError(new Exception());

        assertFalse(response.isSuccess());
    }

    public void testIsFailure() {
        final Response<Object> response = new Response<Object>();

        assertFalse(response.isFailure());

        response.error = new DataError(new Exception());

        assertTrue(response.isFailure());
    }

    public void testIsNotModified() {
        final Response<Object> response = new Response<Object>();

        assertFalse(response.isNotModified());

        response.error = new DataError(new Exception());

        assertFalse(response.isNotModified());

        DataHttpException exception = new DataHttpException(304, UUID.randomUUID().toString());
        response.error = new DataError(exception);

        assertTrue(response.isNotModified());
    }

    public void testIsNotFound() {
        final Response<Object> response = new Response<Object>();

        assertFalse(response.isNotFound());

        response.error = new DataError(new Exception());

        assertFalse(response.isNotFound());

        final DataHttpException exception = new DataHttpException(404, UUID.randomUUID().toString());
        response.error = new DataError(exception);

        assertTrue(response.isNotFound());
    }

    public void testHasPreconditionFailed() {
        final Response<Object> response = new Response<Object>();

        assertFalse(response.hasPreconditionFailed());

        response.error = new DataError(new Exception());

        assertFalse(response.hasPreconditionFailed());

        final DataHttpException exception = new DataHttpException(412, UUID.randomUUID().toString());
        response.error = new DataError(exception);

        assertTrue(response.hasPreconditionFailed());
    }

    public void testIsUnauthorized() {
        final Response<Object> response = new Response<Object>();

        assertFalse(response.isUnauthorized());

        response.error = new DataError(new Exception());

        assertFalse(response.isUnauthorized());

        final DataHttpException exception = new DataHttpException(401, UUID.randomUUID().toString());
        response.error = new DataError(exception);

        assertTrue(response.isUnauthorized());
    }
}