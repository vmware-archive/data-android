/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.os.AsyncTask;

public interface RequestCache<T> {

    public void queue(final Request<T> request);

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

        protected PendingRequest<T> createPendingRequest(final Request<T> request) {
            return new PendingRequest<T>(request);
        }

        @Override
        public void queue(final Request<T> request) {
            final PendingRequest<T> pendingRequest = createPendingRequest(request);
            mQueue.add(pendingRequest);
        }

        @Override
        public void executePending() {
            final PendingRequest.List<T> requests = mQueue.empty();

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