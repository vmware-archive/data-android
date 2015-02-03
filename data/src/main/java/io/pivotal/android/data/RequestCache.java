/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.os.AsyncTask;

public interface RequestCache<T> {

    public void queueGet(final Request<T> request);

    public void queuePut(final Request<T> request);

    public void queueDelete(final Request<T> request);

    public void executePending();

    public void executePendingAsync();

    public static class Default<T> implements RequestCache<T> {

        private static final String REQUEST_CACHE = "PCFData:RequestCache";

        private final RequestCacheQueue<T> mQueue;
        private final RequestCacheExecutor<T> mExecutor;

        public Default(final Context context, final OfflineStore<T> offlineStore, final DataStore<T> fallbackStore) {
            mQueue = new RequestCacheQueue<T>(new DataPersistence(context, REQUEST_CACHE));
            mExecutor = new RequestCacheExecutor<T>(offlineStore, fallbackStore);
        }

        public Default(final RequestCacheQueue<T> queue, final RequestCacheExecutor<T> executor) {
            mQueue = queue;
            mExecutor = executor;
        }

        protected QueuedRequest<T> createQueuedRequest(final Request<T> request, final int method) {
            return new QueuedRequest<T>(request, method);
        }

        @Override
        public void queueGet(final Request<T> request) {
            final QueuedRequest<T> queuedRequest = createQueuedRequest(request, QueuedRequest.Methods.GET);
            mQueue.add(queuedRequest);
        }

        @Override
        public void queuePut(final Request<T> request) {
            final QueuedRequest<T> queuedRequest = createQueuedRequest(request, QueuedRequest.Methods.PUT);
            mQueue.add(queuedRequest);
        }

        @Override
        public void queueDelete(final Request<T> request) {
            final QueuedRequest<T> queuedRequest = createQueuedRequest(request, QueuedRequest.Methods.DELETE);
            mQueue.add(queuedRequest);
        }

        @Override
        public void executePending() {
            final QueuedRequest.List<T> requests = mQueue.empty();

            mExecutor.execute(requests);
        }

        @Override
        public void executePendingAsync() {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(final Void... params) {
                    executePending();
                    return null;
                }

            }.execute();
        }
    }

}