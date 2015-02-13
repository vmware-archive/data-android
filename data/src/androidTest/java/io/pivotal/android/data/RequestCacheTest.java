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

    public void testDefaultGet() {
        final Request request = Mockito.mock(Request.class);
        final QueuedRequest queued = Mockito.mock(QueuedRequest.class);
        final RequestCacheQueue queue = Mockito.mock(RequestCacheQueue.class);
        final RequestCache.Default defaultCache = Mockito.spy(new RequestCache.Default(queue, null));

        Mockito.doReturn(queued).when(defaultCache).createQueuedRequest(Mockito.any(Request.class), Mockito.anyInt());

        defaultCache.queueGet(request);

        Mockito.verify(defaultCache).createQueuedRequest(request, QueuedRequest.Methods.GET);
        Mockito.verify(queue).add(queued);
    }

    public void testDefaultPut() {
        final Request request = Mockito.mock(Request.class);
        final QueuedRequest queued = Mockito.mock(QueuedRequest.class);
        final RequestCacheQueue queue = Mockito.mock(RequestCacheQueue.class);
        final RequestCache.Default defaultCache = Mockito.spy(new RequestCache.Default(queue, null));

        Mockito.doReturn(queued).when(defaultCache).createQueuedRequest(Mockito.any(Request.class), Mockito.anyInt());

        defaultCache.queuePut(request);

        Mockito.verify(defaultCache).createQueuedRequest(request, QueuedRequest.Methods.PUT);
        Mockito.verify(queue).add(queued);
    }

    public void testDefaultDelete() {
        final Request request = Mockito.mock(Request.class);
        final QueuedRequest queued = Mockito.mock(QueuedRequest.class);
        final RequestCacheQueue queue = Mockito.mock(RequestCacheQueue.class);
        final RequestCache.Default defaultCache = Mockito.spy(new RequestCache.Default(queue, null));

        Mockito.doReturn(queued).when(defaultCache).createQueuedRequest(Mockito.any(Request.class), Mockito.anyInt());

        defaultCache.queueDelete(request);

        Mockito.verify(defaultCache).createQueuedRequest(request, QueuedRequest.Methods.DELETE);
        Mockito.verify(queue).add(queued);
    }

    public void testDefaultExecutePending() {
        final RequestCacheQueue queue = Mockito.mock(RequestCacheQueue.class);
        final RequestCacheExecutor executor = Mockito.mock(RequestCacheExecutor.class);
        final QueuedRequest.List list = new QueuedRequest.List();
        final RequestCache.Default defaultCache = new RequestCache.Default(queue, executor);

        Mockito.when(queue.empty()).thenReturn(list);

        defaultCache.executePending();

        Mockito.verify(executor).execute(list);
    }
}