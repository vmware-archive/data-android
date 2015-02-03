/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.UUID;

@SuppressWarnings("unchecked")
public class RequestCacheTest extends AndroidTestCase {

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