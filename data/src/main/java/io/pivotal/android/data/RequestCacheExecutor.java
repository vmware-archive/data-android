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

    public void execute(final RequestCache.QueuedRequest.List<T> requests, final String token) {
        for (final RequestCache.QueuedRequest<T> request : requests) {
            execute(request, token);
        }
    }

    private void execute(final RequestCache.QueuedRequest<T> request, final String token) {
        if (token != null) {
            request.accessToken = token;
        }

        switch (request.method) {
            case RequestCache.QueuedRequest.Methods.GET:
                mOfflineStore.get(request);
                break;

            case RequestCache.QueuedRequest.Methods.PUT:
                executePut(request);
                break;

            case RequestCache.QueuedRequest.Methods.DELETE:
                executeDelete(request);
                break;

            default:
                throw new UnsupportedOperationException();
        }
    }

    private void executePut(final RequestCache.QueuedRequest<T> request) {
        final Response<T> response = mOfflineStore.put(request);
        if (response.isFailure()) {
            request.object = request.fallback;
            mFallbackStore.put(request);
        }
    }

    private void executeDelete(final RequestCache.QueuedRequest<T> request) {
        final Response<T> response = mOfflineStore.delete(request);
        if (response.isFailure()) {
            request.object = request.fallback;
            mFallbackStore.put(request);
        }
    }
}
