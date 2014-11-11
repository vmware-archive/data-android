/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

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
        Logger.d("Get: " + key);

        if (isConnected()) {
            mRemoteStore.get(accessToken, key, newLocalPutListener(accessToken, listener));

        } else {
            final Response local = mLocalStore.get(accessToken, key);

            if (isSyncSupported()) {
                getRequestCache().queueGet(accessToken, mCollection, key);
            }

            if (listener != null) {
                listener.onResponse(local);
            }
        }
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
        Logger.d("Put: " + key + ", " + value);

        if (isConnected()) {
            mRemoteStore.put(accessToken, key, value, newLocalPutListener(accessToken, listener));

        } else if (isSyncSupported()) {
            final Response fallback = mLocalStore.get(accessToken, key);
            final Response local = mLocalStore.put(accessToken, key, value);

            getRequestCache().queuePut(accessToken, mCollection, key, value, fallback.value);

            if (listener != null) {
                listener.onResponse(local);
            }

        } else if (listener != null) {
            listener.onResponse(newNoConnectionFailureResponse(key));
        }
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
        Logger.d("Delete: " + key);

        if (isConnected()) {
            mRemoteStore.delete(accessToken, key, newLocalDeleteListener(accessToken, listener));

        } else if (isSyncSupported()) {
            final Response fallback = mLocalStore.get(accessToken, key);
            final Response local = mLocalStore.delete(accessToken, key);

            getRequestCache().queueDelete(accessToken, mCollection, key, fallback.value);

            if (listener != null) {
                listener.onResponse(local);
            }

        } else if (listener != null) {
            listener.onResponse(newNoConnectionFailureResponse(key));
        }
    }

    protected Response newNoConnectionFailureResponse(final String key) {
        final RuntimeException exception = new RuntimeException("No connection.");
        return Response.failure(key, new DataError(exception));
    }

    protected Listener newLocalPutListener(final String accessToken, final Listener listener) {
        return new LocalPutListener(accessToken, listener);
    }

    protected Listener newLocalDeleteListener(final String accessToken, final Listener listener) {
        return new LocalDeleteListener(accessToken, listener);
    }

    // ======================================



    protected class LocalPutListener implements Listener {

        private final String mAccessToken;
        private final Listener mListener;

        public LocalPutListener(final String accessToken, final Listener listener) {
            mAccessToken = accessToken;
            mListener = listener;
        }

        @Override
        public void onResponse(final Response remote) {

            if (remote.status == Response.Status.SUCCESS) {
                mLocalStore.put(mAccessToken, remote.key, remote.value, mListener);

            } else if (mListener != null) {
                mListener.onResponse(remote);
            }
        }
    }

    protected class LocalDeleteListener implements Listener {

        private final String mAccessToken;
        private final Listener mListener;

        public LocalDeleteListener(final String accessToken, final Listener listener) {
            mAccessToken = accessToken;
            mListener = listener;
        }

        @Override
        public void onResponse(final Response remote) {

            if (remote.status == Response.Status.SUCCESS) {
                mLocalStore.delete(mAccessToken, remote.key, mListener);

            } else if (mListener != null) {
                mListener.onResponse(remote);
            }
        }
    }
}