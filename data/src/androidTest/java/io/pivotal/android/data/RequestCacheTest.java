/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

@SuppressWarnings("unchecked")
public class RequestCacheTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testDefaultQueueRequest() {
        final Request request = Mockito.mock(Request.class);
        final PendingRequest queued = Mockito.mock(PendingRequest.class);
        final RequestCacheQueue queue = Mockito.mock(RequestCacheQueue.class);
        final RequestCache.Default defaultCache = Mockito.spy(new RequestCache.Default(queue, null));

        Mockito.doReturn(queued).when(defaultCache).createPendingRequest(Mockito.any(Request.class));

        defaultCache.queue(request);

        Mockito.verify(defaultCache).createPendingRequest(request);
        Mockito.verify(queue).add(queued);
    }

    public void testDefaultExecutePending() {
        final RequestCacheQueue queue = Mockito.mock(RequestCacheQueue.class);
        final RequestCacheExecutor executor = Mockito.mock(RequestCacheExecutor.class);
        final PendingRequest.List list = new PendingRequest.List();
        final RequestCache.Default defaultCache = new RequestCache.Default(queue, executor);

        Mockito.when(queue.empty()).thenReturn(list);

        defaultCache.executePending();

        Mockito.verify(executor).execute(list);
    }
}