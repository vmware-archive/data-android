/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

@SuppressWarnings("unchecked")
public class RequestCacheExecutorTest extends AndroidTestCase {

    private static final int METHOD_PUT_OR_DELETE = (int)(Math.random() * 2) + 2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testExecute() {
        try {
            final PendingRequest.List list = new PendingRequest.List();
            list.add(Mockito.mock(PendingRequest.class));

            final RequestCacheExecutor executor = new RequestCacheExecutor(null, null);
            executor.execute(list);

            fail("Expected exception");
        } catch (final UnsupportedOperationException ex) {
            assertNotNull(ex);
        }
    }

    public void testExecuteGetSuccess() {
        final PendingRequest request = Mockito.mock(PendingRequest.class);
        request.method = PendingRequest.Methods.GET;

        final PendingRequest.List list = new PendingRequest.List();
        list.add(request);

        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);
        final DataStore fallbackStore = Mockito.mock(DataStore.class);
        final RequestCacheExecutor executor = new RequestCacheExecutor(offlineStore, fallbackStore);

        executor.execute(list);

        Mockito.verify(offlineStore).execute(request);
    }

    public void testExecuteWithFallbackSuccess() {
        final Response response = Mockito.mock(Response.class);
        final PendingRequest request = Mockito.mock(PendingRequest.class);
        request.method = METHOD_PUT_OR_DELETE;

        final PendingRequest.List list = new PendingRequest.List();
        list.add(request);

        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);
        final DataStore fallbackStore = Mockito.mock(DataStore.class);
        final RequestCacheExecutor executor = new RequestCacheExecutor(offlineStore, fallbackStore);

        Mockito.when(offlineStore.execute(Mockito.any(PendingRequest.class))).thenReturn(response);
        Mockito.when(response.isFailure()).thenReturn(false);

        executor.execute(list);

        Mockito.verify(offlineStore).execute(request);
    }

    public void testExecuteFailureWithFallback() {
        final Response response = Mockito.mock(Response.class);
        final PendingRequest request = Mockito.mock(PendingRequest.class);
        request.method = METHOD_PUT_OR_DELETE;

        final PendingRequest.List list = new PendingRequest.List();
        list.add(request);

        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);
        final DataStore fallbackStore = Mockito.mock(DataStore.class);
        final RequestCacheExecutor executor = new RequestCacheExecutor(offlineStore, fallbackStore);

        Mockito.when(offlineStore.execute(Mockito.any(PendingRequest.class))).thenReturn(response);
        Mockito.when(response.isFailure()).thenReturn(true);

        executor.execute(list);

        Mockito.verify(offlineStore).execute(request);
        Mockito.verify(fallbackStore).execute(Mockito.isA(Request.Put.class));
    }
}