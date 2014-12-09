/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.os.AsyncTask;

public class OfflineStore implements DataStore {

    private final Context mContext;
    private final String mCollection;
    private final LocalStore mLocalStore;
    private final RemoteStore mRemoteStore;

    private RequestCache mRequestCache;

    public static OfflineStore create(final Context context, final String collection) {
        final LocalStore localStore = new LocalStore(context, collection);
        final RemoteStore remoteStore = new RemoteStore(context, collection);
        return new OfflineStore(context, collection, localStore, remoteStore);
    }

    public OfflineStore(final Context context, final String collection, final LocalStore localStore, final RemoteStore remoteStore) {
        mContext = context;
        mCollection = collection;
        mLocalStore = localStore;
        mRemoteStore = remoteStore;
    }

    @Override
    public boolean contains(final String accessToken, final String key) {
        return mLocalStore.contains(accessToken, key);
    }

    @Override
    public boolean addObserver(final Observer observer) {
        return mLocalStore.addObserver(observer)
            && mRemoteStore.addObserver(observer);
    }

    @Override
    public boolean removeObserver(final Observer observer) {
        return mLocalStore.removeObserver(observer)
            && mRemoteStore.removeObserver(observer);
    }

    protected boolean isConnected() {
        return ConnectivityReceiver.isConnected(mContext);
    }

    protected boolean isSyncSupported() {
        return ConnectivityReceiver.hasReceiver(mContext);
    }

    protected RequestCache getRequestCache() {
        if (mRequestCache == null) {
            synchronized (this) {
                mRequestCache = new RequestCache.Default(mContext);
            }
        }
        return mRequestCache;
    }

    @Override
    public Response get(final String accessToken, final String key) {
        Logger.d("Get: " + key);

        if (isConnected()) {
            final Response remote = mRemoteStore.get(accessToken, key);

            if (remote.status == Response.Status.SUCCESS) {
                return mLocalStore.put(accessToken, remote.key, remote.value);

            } else if (remote.error != null && remote.error.isNotModified()) {
                return mLocalStore.get(accessToken, remote.key);

            } else {
                return remote;
            }

        } else {
            final Response local = mLocalStore.get(accessToken, key);

            if (isSyncSupported()) {
                getRequestCache().queueGet(accessToken, mCollection, key);
            }

            return local;
        }
    }

    @Override
    public void get(final String accessToken, final String key, final Listener listener) {
        new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(final Void... params) {
                return OfflineStore.this.get(accessToken, key);
            }

            @Override
            protected void onPostExecute(final Response resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public Response put(final String accessToken, final String key, final String value) {
        Logger.d("Put: " + key + ", " + value);

        if (isConnected()) {
            final Response remote = mRemoteStore.put(accessToken, key, value);

            if (remote.status == Response.Status.SUCCESS) {
                return mLocalStore.put(accessToken, remote.key, remote.value);

            } else {
                return remote;
            }

        } else if (isSyncSupported()) {
            final Response fallback = mLocalStore.get(accessToken, key);
            final Response local = mLocalStore.put(accessToken, key, value);

            getRequestCache().queuePut(accessToken, mCollection, key, value, fallback.value);

            return local;

        } else {
            return newNoConnectionFailureResponse(key);
        }
    }

    @Override
    public void put(final String accessToken, final String key, final String value, final Listener listener) {
        new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(final Void... params) {
                return OfflineStore.this.put(accessToken, key, value);
            }

            @Override
            protected void onPostExecute(final Response resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }


    @Override
    public Response delete(final String accessToken, final String key) {
        Logger.d("Delete: " + key);

        if (isConnected()) {
            final Response remote = mRemoteStore.delete(accessToken, key);

            if (remote.status == Response.Status.SUCCESS) {
                return mLocalStore.delete(accessToken, remote.key);

            } else {
                return remote;
            }

        } else if (isSyncSupported()) {
            final Response fallback = mLocalStore.get(accessToken, key);
            final Response local = mLocalStore.delete(accessToken, key);

            getRequestCache().queueDelete(accessToken, mCollection, key, fallback.value);

            return local;

        } else {
            return newNoConnectionFailureResponse(key);
        }
    }

    @Override
    public void delete(final String accessToken, final String key, final Listener listener) {
        new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(final Void... params) {
                return OfflineStore.this.delete(accessToken, key);
            }

            @Override
            protected void onPostExecute(final Response resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    protected Response newNoConnectionFailureResponse(final String key) {
        final RuntimeException exception = new RuntimeException("No connection.");
        return Response.failure(key, new DataError(exception));
    }
}