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
        mContext = context; mCollection = collection; mLocalStore = localStore; mRemoteStore = remoteStore;
    }

    @Override
    public boolean contains(final String accessToken, final String key) {
        return mLocalStore.contains(accessToken, key);
    }

    @Override
    public boolean addObserver(final Observer observer) {
        return mLocalStore.addObserver(observer);
    }

    @Override
    public boolean removeObserver(final Observer observer) {
        return mLocalStore.removeObserver(observer);
    }

    protected boolean isConnected() {
        return ConnectivityReceiver.isConnected(mContext);
    }

    protected boolean hasReceiver() {
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

        final Response local = mLocalStore.get(accessToken, key);

        if (isConnected()) {
            mRemoteStore.get(accessToken, key, new UpdateListener(accessToken, null));

        } else if (hasReceiver()) {
            getRequestCache().addGetRequest(accessToken, mCollection, key);
        }

        return Response.pending(local.key, local.value);
    }

//    @Override
//    public void get(final String accessToken, final String key, final Listener listener) {
//        Logger.d("Get: " + key);
//    }

    @Override
    public Response put(final String accessToken, final String key, final String value) {
        Logger.d("Put: " + key + ", " + value);

        final Response fallback = mLocalStore.get(accessToken, key);
        final Response local = mLocalStore.put(accessToken, key, value);

        if (isConnected()) {
            mRemoteStore.put(accessToken, key, value, new UpdateListener(accessToken, fallback));

        } else if (hasReceiver()) {
            getRequestCache().addPutRequest(accessToken, mCollection, key, value);
        }

        return Response.pending(local.key, local.value);
    }

//    @Override
//    public void put(final String accessToken, final String key, final String value, final Listener listener) {
//        Logger.d("Put: " + key + ", " + value);
//    }


    @Override
    public Response delete(final String accessToken, final String key) {
        Logger.d("Delete: " + key);

        final Response fallback = mLocalStore.get(accessToken, key);
        final Response local = mLocalStore.delete(accessToken, key);

        if (isConnected()) {
            mRemoteStore.delete(accessToken, key, new DeleteListener(accessToken, fallback));

        } else if (hasReceiver()) {
            getRequestCache().addDeleteRequest(accessToken, mCollection, key);
        }

        return Response.pending(local.key, local.value);
    }

//    @Override
//    public void delete(final String accessToken, final String key, final Listener listener) {
//        Logger.d("Delete: " + key);
//    }


    // =================================


    protected final class UpdateListener extends FallbackListener {

        public UpdateListener(final String accessToken, final Response fallback) {
            super(accessToken, fallback);
        }

        @Override
        protected void onSuccess(final String accessToken, final Response response) {
            mLocalStore.put(accessToken, response.key, response.value);
        }
    }

    protected final class DeleteListener extends FallbackListener {

        public DeleteListener(final String accessToken, final Response fallback) {
            super(accessToken, fallback);
        }

        @Override
        protected void onSuccess(final String accessToken, final Response response) {
            mLocalStore.delete(accessToken, response.key);
        }
    }

    private abstract class FallbackListener implements RemoteStore.Listener {

        private final String mAccessToken;
        private final Response mFallback;

        public FallbackListener(final String accessToken, final Response fallback) {
            mAccessToken = accessToken;
            mFallback = fallback;
        }

        @Override
        public void onResponse(final Response response) {
            if (response.status == Response.Status.SUCCESS) {
                onSuccess(mAccessToken, response);

            } else if (mFallback != null && isNotHttpNotModified(response)) {
                mLocalStore.put(mAccessToken, mFallback.key, mFallback.value);
            }
        }

        private boolean isNotHttpNotModified(final Response response) {
            return response.error != null && !response.error.isNotModified();
        }

        protected abstract void onSuccess(final String accessToken, final Response response);
    }
}