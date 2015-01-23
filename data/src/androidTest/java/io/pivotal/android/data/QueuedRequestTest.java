/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import java.util.UUID;

@SuppressWarnings("unchecked")
public class QueuedRequestTest extends AndroidTestCase {

    private static final String TOKEN = UUID.randomUUID().toString();
    private static final String COLLECTION = UUID.randomUUID().toString();
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final int METHOD = (int) (Math.random() * 3.0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testQueuedRequest() {
        final Request request = new Request(TOKEN, new KeyValue(COLLECTION, KEY, VALUE));
        final QueuedRequest queued = new QueuedRequest(request, METHOD);

        assertEquals(TOKEN, queued.accessToken);
        assertEquals(METHOD, queued.method);
        assertEquals(request.object, queued.object);
        assertEquals(request.force, queued.force);
    }
}