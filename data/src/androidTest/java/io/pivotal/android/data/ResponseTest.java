/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import java.util.UUID;

public class ResponseTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testIsSuccess() {
        Response<Object> response = new Response<Object>();

        assertTrue(response.isSuccess());

        response.error = new DataError(new Exception());

        assertFalse(response.isSuccess());
    }

    public void testIsFailure() {
        Response<Object> response = new Response<Object>();

        assertFalse(response.isFailure());

        response.error = new DataError(new Exception());

        assertTrue(response.isFailure());
    }

    public void testIsNotModified() {
        Response<Object> response = new Response<Object>();

        assertFalse(response.isNotModified());

        response.error = new DataError(new Exception());

        assertFalse(response.isNotModified());

        DataHttpException exception = new DataHttpException(304, UUID.randomUUID().toString());
        response.error = new DataError(exception);

        assertTrue(response.isNotModified());
    }

    public void testHasPreconditionFailed() {
        Response<Object> response = new Response<Object>();

        assertFalse(response.hasPreconditionFailed());

        response.error = new DataError(new Exception());

        assertFalse(response.hasPreconditionFailed());

        DataHttpException exception = new DataHttpException(412, UUID.randomUUID().toString());
        response.error = new DataError(exception);

        assertTrue(response.hasPreconditionFailed());
    }
}