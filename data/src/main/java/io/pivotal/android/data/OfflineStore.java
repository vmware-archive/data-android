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

        final Response response = mLocalStore.get(accessToken, key);

        if (isConnected()) {
            mRemoteStore.getAsync(accessToken, key, new UpdateListener(accessToken));
        } else {
            getRequestCache().addGetRequest(accessToken, mCollection, key);
        }

        return Response.pending(response.key, response.value);
    }

    @Override
    public Response put(final String accessToken, final String key, final String value) {
        Logger.d("Put: " + key + ", " + value);

        final Response response = mLocalStore.put(accessToken, key, value);

        if (isConnected()) {
            mRemoteStore.putAsync(accessToken, key, value, new UpdateListener(accessToken));
        } else {
            getRequestCache().addPutRequest(accessToken, mCollection, key, value);
        }

        return Response.pending(response.key, response.value);
    }

    @Override
    public Response delete(final String accessToken, final String key) {
        Logger.d("Delete: " + key);

        final Response response = mLocalStore.delete(accessToken, key);

        if (isConnected()) {
            mRemoteStore.deleteAsync(accessToken, key, new DeleteListener(accessToken));
        } else {
            getRequestCache().addDeleteRequest(accessToken, mCollection, key);
        }

        return Response.pending(response.key, response.value);
    }


    // =================================


    protected final class UpdateListener implements RemoteStore.Listener {

        private final String mAccessToken;

        public UpdateListener(final String accessToken) {
            mAccessToken = accessToken;
        }

        @Override
        public void onResponse(final Response response) {
            if (response.status == Response.Status.SUCCESS) {
                mLocalStore.put(mAccessToken, response.key, response.value);
            }
        }
    }

    protected final class DeleteListener implements RemoteStore.Listener {

        private final String mAccessToken;

        public DeleteListener(final String accessToken) {
            mAccessToken = accessToken;
        }

        @Override
        public void onResponse(final Response response) {
            if (response.status == Response.Status.SUCCESS) {
                mLocalStore.delete(mAccessToken, response.key);
            }
        }
    }
}