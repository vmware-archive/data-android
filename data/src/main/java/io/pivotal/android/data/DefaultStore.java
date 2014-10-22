/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

public class DefaultStore implements DataStore {

    private final Context mContext;
    private final String mCollection;
    private final LocalStore mLocalStore;
    private final RemoteStore mRemoteStore;

    private RequestCache mRequestCache;

    public static DefaultStore create(final Context context, final String collection) {
        final LocalStore localStore = new LocalStore(context, collection);
        final RemoteStore remoteStore = new RemoteStore(context, collection);
        return new DefaultStore(context, collection, localStore, remoteStore);
    }

    public DefaultStore(final Context context, final String collection, final LocalStore localStore, final RemoteStore remoteStore) {
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

    @Override
    public Response get(final String accessToken, final String key) {
        Logger.d("Get: " + key);
        try {
            final Response response = mLocalStore.get(accessToken, key);

            getRemotelyOrStorePending(accessToken, key);

            return Response.pending(response.key, response.value);

        } catch (final Exception e) {
            Logger.ex(e);
            return Response.failure(key, new DataError(e));
        }
    }

    @Override
    public Response put(final String accessToken, final String key, final String value) {
        Logger.d("Put: " + key + ", " + value);
        try {

            mLocalStore.put(accessToken, key, value);

            putRemotelyOrStorePending(accessToken, key, value);

            return Response.pending(key, value);

        } catch (final Exception e) {
            return Response.failure(key, new DataError(e));
        }
    }

    @Override
    public Response delete(final String accessToken, final String key) {
        Logger.d("Delete: " + key);
        try {

            mLocalStore.delete(accessToken, key);

            deleteRemotelyOrStorePending(accessToken, key);

            return Response.pending(key, null);

        } catch (final Exception e) {
            return Response.failure(key, new DataError(e));
        }
    }


    private RequestCache getRequestCache() {
        if (mRequestCache == null) {
            mRequestCache = new RequestCache(mContext);
        }
        return mRequestCache;
    }


    private void getRemotelyOrStorePending(final String accessToken, final String key) {
        if (ConnectivityReceiver.isConnected(mContext)) {
            mRemoteStore.getAsync(accessToken, key, new UpdateListener(accessToken));
        } else {
            getRequestCache().storeGetRequest(accessToken, mCollection, key);
        }
    }

    private void putRemotelyOrStorePending(final String accessToken, final String key, final String value) {
        if (ConnectivityReceiver.isConnected(mContext)) {
            mRemoteStore.putAsync(accessToken, key, value, new UpdateListener(accessToken));
        } else {
            getRequestCache().storePutRequest(accessToken, mCollection, key, value);
        }
    }

    private void deleteRemotelyOrStorePending(final String accessToken, final String key) {
        if (ConnectivityReceiver.isConnected(mContext)) {
            mRemoteStore.deleteAsync(accessToken, key, new DeleteListener(accessToken));
        } else {
            getRequestCache().storeDeleteRequest(accessToken, mCollection, key);
        }
    }


    // =================================


    private final class UpdateListener implements RemoteStore.Listener {

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

    private final class DeleteListener implements RemoteStore.Listener {

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