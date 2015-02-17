/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

public class RequestCacheExecutor<T> {

    private final OfflineStore<T> mOfflineStore;
    private final DataStore<T> mFallbackStore;

    public RequestCacheExecutor(final OfflineStore<T> offlineStore, final DataStore<T> fallbackStore) {
        mOfflineStore = offlineStore;
        mFallbackStore = fallbackStore;
    }

    public void execute(final PendingRequest.List<T> requests) {
        for (final PendingRequest<T> request : requests) {
            execute(request);
        }
    }

    private void execute(final PendingRequest<T> request) {

        switch (request.method) {
            case Request.Methods.GET:
                mOfflineStore.execute(request);
                break;

            case Request.Methods.PUT:
            case Request.Methods.DELETE:
                executeWithFallback(request);
                break;

            default:
                throw new UnsupportedOperationException();
        }
    }

    private void executeWithFallback(final PendingRequest<T> request) {
        final Response<T> response = mOfflineStore.execute(request);
        if (response.isFailure()) {
            final Request<T> put = new Request.Put<T>(request);
            put.object = request.fallback;
            mFallbackStore.execute(put);
        }
    }
}
