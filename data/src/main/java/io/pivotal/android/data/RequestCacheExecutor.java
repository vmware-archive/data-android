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

    public void execute(final QueuedRequest.List<T> requests) {
        for (final QueuedRequest<T> request : requests) {
            execute(request);
        }
    }

    private void execute(final QueuedRequest<T> request) {

        switch (request.method) {
            case QueuedRequest.Methods.GET:
                executeGet(request);
                break;

            case QueuedRequest.Methods.PUT:
                executePut(request);
                break;

            case QueuedRequest.Methods.DELETE:
                executeDelete(request);
                break;

            default:
                throw new UnsupportedOperationException();
        }
    }

    private void executeGet(final QueuedRequest<T> request) {
        mOfflineStore.get(request);
    }

    private void executePut(final QueuedRequest<T> request) {
        final Response<T> response = mOfflineStore.put(request);
        if (response.isFailure()) {
            request.object = request.fallback;
            mFallbackStore.put(request);
        }
    }

    private void executeDelete(final QueuedRequest<T> request) {
        final Response<T> response = mOfflineStore.delete(request);
        if (response.isFailure()) {
            request.object = request.fallback;
            mFallbackStore.put(request);
        }
    }
}
