/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.os.AsyncTask;

public class OfflineStore<T> implements DataStore<T> {

    private final Context mContext;
    private final DataStore<T> mLocalStore;
    private final RemoteStore<T> mRemoteStore;

    private RequestCache<T> mRequestCache;

    public static OfflineStore<KeyValue> createKeyValue(final Context context) {
        final DataStore<KeyValue> localStore = new KeyValueStore(context);
        final RemoteStore<KeyValue> remoteStore = new RemoteStore<KeyValue>(context);
        return new OfflineStore<KeyValue>(context, localStore, remoteStore);
    }

    public OfflineStore(final Context context, final DataStore<T> localStore, final RemoteStore<T> remoteStore) {
        mContext = context;
        mLocalStore = localStore;
        mRemoteStore = remoteStore;
    }

    @Override
    public Response<T> get(final Request<T> request) {
        Logger.d("Get: " + request.object);

        if (isConnected()) {
            final Response<T> response = mRemoteStore.get(request);

            if (response.isSuccess()) {
                final Request<T> localRequest = new Request<T>(request);
                localRequest.object = response.object;

                return mLocalStore.put(localRequest);

            } else if (response.isNotFound()) {
                mLocalStore.delete(request);
                return response;

            } else if (response.isNotModified()) {
                return mLocalStore.get(request);

            } else {
                return response;
            }

        } else {
            final Response<T> response = mLocalStore.get(request);

            getRequestCache().queueGet(request);

            return response;
        }
    }

    @Override
    public void get(final Request<T> request, final Listener<T> listener) {
        new AsyncTask<Void, Void, Response<T>>() {

            @Override
            protected Response<T> doInBackground(final Void... params) {
                return OfflineStore.this.get(request);
            }

            @Override
            protected void onPostExecute(final Response<T> resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public Response<T> put(final Request<T> request) {
        Logger.d("Put: " + request.object);

        if (isConnected()) {
            final Response<T> response = mRemoteStore.put(request);

            if (response.isSuccess()) {
                return mLocalStore.put(request);

            } else {
                return response;
            }

        } else {
            final Response<T> fallback = mLocalStore.get(request);
            final Response<T> response = mLocalStore.put(request);

            request.fallback = fallback.object;

            getRequestCache().queuePut(request);

            return response;
        }
    }

    @Override
    public void put(final Request<T> request, final Listener<T> listener) {
        new AsyncTask<Void, Void, Response<T>>() {

            @Override
            protected Response<T> doInBackground(final Void... params) {
                return OfflineStore.this.put(request);
            }

            @Override
            protected void onPostExecute(final Response<T> resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public Response<T> delete(final Request<T> request) {
        Logger.d("Delete: " + request.object);

        if (isConnected()) {
            final Response<T> response = mRemoteStore.delete(request);

            if (response.isSuccess()) {
                return mLocalStore.delete(request);

            } else {
                return response;
            }

        } else {
            final Response<T> fallback = mLocalStore.get(request);
            final Response<T> response = mLocalStore.delete(request);

            request.fallback = fallback.object;

            getRequestCache().queueDelete(request);

            return response;
        }
    }

    @Override
    public void delete(final Request<T> request, final Listener<T> listener) {
        new AsyncTask<Void, Void, Response<T>>() {

            @Override
            protected Response<T> doInBackground(final Void... params) {
                return OfflineStore.this.delete(request);
            }

            @Override
            protected void onPostExecute(final Response<T> resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
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