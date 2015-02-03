/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import java.util.UUID;

@SuppressWarnings("unchecked")
public class QueuedRequestTest extends AndroidTestCase {

    private static final String COLLECTION = UUID.randomUUID().toString();
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final String FALLBACK = UUID.randomUUID().toString();
    private static final int METHOD = (int) (Math.random() * 3.0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testQueuedRequest() {
        final KeyValue object = new KeyValue(COLLECTION, KEY, VALUE);
        final KeyValue fallback = new KeyValue(COLLECTION, KEY, FALLBACK);
        final Request request = new Request(object, fallback, false);

        final QueuedRequest queued = new QueuedRequest(request, METHOD);

        assertEquals(METHOD, queued.method);
        assertEquals(request.object, queued.object);
        assertEquals(request.force, queued.force);
        assertEquals(request.fallback, queued.fallback);
    }
}