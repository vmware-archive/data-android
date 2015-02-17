/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.os.AsyncTask;

public class OfflineStore<T> implements DataStore<T> {

    private final Context mContext;
    private final LocalStore<T> mLocalStore;
    private final RemoteStore<T> mRemoteStore;

    private RequestCache<T> mRequestCache;

    public OfflineStore(final Context context, final LocalStore<T> localStore, final RemoteStore<T> remoteStore) {
        mContext = context;
        mLocalStore = localStore;
        mRemoteStore = remoteStore;
    }

    @Override
    public Response<T> execute(final Request<T> request) {

        switch (request.method) {
            case Request.Methods.GET:
                Logger.d("Get: " + request.object);
                return get(request);

            case Request.Methods.PUT:
                Logger.d("Put: " + request.object);
                return executeWithFallback(request);

            case Request.Methods.DELETE:
                Logger.d("Delete: " + request.object);
                return executeWithFallback(request);

            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void execute(final Request<T> request, final Listener<T> listener) {
        new AsyncTask<Void, Void, Response<T>>() {

            @Override
            protected Response<T> doInBackground(final Void... params) {
                return OfflineStore.this.execute(request);
            }

            @Override
            protected void onPostExecute(final Response<T> resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    protected Response<T> get(final Request<T> request) {

        if (isConnected()) {
            final Response<T> response = mRemoteStore.execute(request);

            if (response.isSuccess()) {
                final Request<T> put = new Request.Put<T>(request);
                put.object = response.object;

                return mLocalStore.execute(put);

            } else if (response.isNotFound()) {
                final Request<T> delete = new Request.Delete<T>(request);

                mLocalStore.execute(delete);

                return response;

            } else if (response.isNotModified()) {
                return mLocalStore.execute(request);

            } else {
                return response;
            }

        } else {
            final Response<T> response = mLocalStore.execute(request);

            getRequestCache().queue(request);

            return response;
        }
    }

    protected Response<T> executeWithFallback(final Request<T> request) {

        if (isConnected()) {
            final Response<T> response = mRemoteStore.execute(request);

            if (response.isSuccess()) {
                return mLocalStore.execute(request);

            } else {
                return response;
            }

        } else {
            final Request<T> get = new Request.Get<T>(request);
            final Response<T> fallback = mLocalStore.execute(get);
            final Response<T> response = mLocalStore.execute(request);

            request.fallback = fallback.object;

            getRequestCache().queue(request);

            return response;
        }
    }

    @Override
    public boolean addObserver(final Observer<T> observer) {
        return mLocalStore.addObserver(observer)
                && mRemoteStore.addObserver(observer);
    }

    @Override
    public boolean removeObserver(final Observer<T> observer) {
        return mLocalStore.removeObserver(observer)
                && mRemoteStore.removeObserver(observer);
    }

    protected boolean isConnected() {
        return Connectivity.isConnected(mContext);
    }

    public RequestCache<T> getRequestCache() {
        if (mRequestCache == null) {
            synchronized (this) {
                mRequestCache = new RequestCache.Default<T>(mContext, this, mLocalStore);
            }
        }
        return mRequestCache;
    }
}