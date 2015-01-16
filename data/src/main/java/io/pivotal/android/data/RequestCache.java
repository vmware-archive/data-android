/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

public interface RequestCache<T> {

    public void queueGet(final Request<T> request);

    public void queuePut(final Request<T> request);

    public void queueDelete(final Request<T> request);

    public void executePending(final String token);

    public void executePendingAsync(final String token);

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
        public void executePending(final String token) {
            final QueuedRequest.List<T> requests = mQueue.empty();

            mExecutor.execute(requests, token);
        }

        @Override
        public void executePendingAsync(final String token) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(final Void... params) {
                    executePending(token);
                    return null;
                }

            }.execute();
        }
    }

    public static class QueuedRequest<T> extends Request<T> {

        public static final class Methods {
            public static final int GET = 1;
            public static final int PUT = 2;
            public static final int DELETE = 3;
        }

        public int method;

        public QueuedRequest() {}

        public QueuedRequest(final Request<T> request) {
            this(request, 0);
        }

        public QueuedRequest(final Request<T> request, final int method) {
            super(request.accessToken, request.object, request.force);
            this.method = method;
        }

        public static class List<T> extends ArrayList<QueuedRequest<T>> {
            public static final long serialVersionUID = 0L;
        }
    }
}