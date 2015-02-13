/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

@SuppressWarnings("unchecked")
public class RequestCacheExecutorTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testExecute() {
        try {
            final QueuedRequest.List list = new QueuedRequest.List();
            list.add(Mockito.mock(QueuedRequest.class));

            final RequestCacheExecutor executor = new RequestCacheExecutor(null, null);
            executor.execute(list);

            fail("Expected exception");
        } catch (final UnsupportedOperationException ex) {
            assertNotNull(ex);
        }
    }

    public void testExecuteGetSuccess() {
        final QueuedRequest request = Mockito.mock(QueuedRequest.class);
        request.method = QueuedRequest.Methods.GET;

        final QueuedRequest.List list = new QueuedRequest.List();
        list.add(request);

        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);
        final DataStore fallbackStore = Mockito.mock(DataStore.class);
        final RequestCacheExecutor executor = new RequestCacheExecutor(offlineStore, fallbackStore);

        executor.execute(list);

        Mockito.verify(offlineStore).get(request);
    }

    public void testExecutePutSuccess() {
        final Response response = Mockito.mock(Response.class);
        final QueuedRequest request = Mockito.mock(QueuedRequest.class);
        request.method = QueuedRequest.Methods.PUT;

        final QueuedRequest.List list = new QueuedRequest.List();
        list.add(request);

        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);
        final DataStore fallbackStore = Mockito.mock(DataStore.class);
        final RequestCacheExecutor executor = new RequestCacheExecutor(offlineStore, fallbackStore);

        Mockito.when(offlineStore.put(Mockito.any(QueuedRequest.class))).thenReturn(response);
        Mockito.when(response.isFailure()).thenReturn(false);

        executor.execute(list);

        Mockito.verify(offlineStore).put(request);
    }

    public void testExecutePutFailureWithFallback() {
        final Response response = Mockito.mock(Response.class);
        final QueuedRequest request = Mockito.mock(QueuedRequest.class);
        request.method = QueuedRequest.Methods.PUT;

        final QueuedRequest.List list = new QueuedRequest.List();
        list.add(request);

        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);
        final DataStore fallbackStore = Mockito.mock(DataStore.class);
        final RequestCacheExecutor executor = new RequestCacheExecutor(offlineStore, fallbackStore);

        Mockito.when(offlineStore.put(Mockito.any(QueuedRequest.class))).thenReturn(response);
        Mockito.when(response.isFailure()).thenReturn(true);

        executor.execute(list);

        Mockito.verify(offlineStore).put(request);
        Mockito.verify(fallbackStore).put(request);
    }

    public void testExecuteDeleteSuccess() {
        final Response response = Mockito.mock(Response.class);
        final QueuedRequest request = Mockito.mock(QueuedRequest.class);
        request.method = QueuedRequest.Methods.DELETE;

        final QueuedRequest.List list = new QueuedRequest.List();
        list.add(request);

        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);
        final DataStore fallbackStore = Mockito.mock(DataStore.class);
        final RequestCacheExecutor executor = new RequestCacheExecutor(offlineStore, fallbackStore);

        Mockito.when(offlineStore.delete(Mockito.any(QueuedRequest.class))).thenReturn(response);
        Mockito.when(response.isFailure()).thenReturn(false);

        executor.execute(list);

        Mockito.verify(offlineStore).delete(request);
    }

    public void testExecuteDeleteFailureWithFallback() {
        final Response response = Mockito.mock(Response.class);
        final QueuedRequest request = Mockito.mock(QueuedRequest.class);
        request.method = QueuedRequest.Methods.DELETE;

        final QueuedRequest.List list = new QueuedRequest.List();
        list.add(request);

        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);
        final DataStore fallbackStore = Mockito.mock(DataStore.class);
        final RequestCacheExecutor executor = new RequestCacheExecutor(offlineStore, fallbackStore);

        Mockito.when(offlineStore.delete(Mockito.any(QueuedRequest.class))).thenReturn(response);
        Mockito.when(response.isFailure()).thenReturn(true);

        executor.execute(list);

        Mockito.verify(offlineStore).delete(request);
        Mockito.verify(fallbackStore).put(request);
    }
}