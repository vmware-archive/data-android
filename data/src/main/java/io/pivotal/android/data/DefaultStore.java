/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

public class DefaultStore implements DataStore {

    private final LocalStore mLocalStore;
    private final RemoteStore mRemoteStore;

    public static DefaultStore create(final Context context, final String collection) {
        final LocalStore localStore = new LocalStore(context, collection);
        final RemoteStore remoteStore = new RemoteStore(context, collection);
        return new DefaultStore(localStore, remoteStore);
    }

    public DefaultStore(final LocalStore localStore, final RemoteStore remoteStore) {
        mLocalStore = localStore;
        mRemoteStore = remoteStore;
    }

    @Override
    public Response get(final String accessToken, final String key) {
        final Response local = mLocalStore.get(accessToken, key);
        mRemoteStore.getAsync(accessToken, key, new RemoteStore.Listener() {
            @Override
            public void onResponse(final Response response) {
                if (response.status == Response.Status.SUCCESS) {
                    mLocalStore.put(accessToken, key, response.value);
                }
            }
        });
        return Response.pending(key, local.value);
    }

    @Override
    public Response put(final String accessToken, final String key, final String value) {
        mRemoteStore.putAsync(accessToken, key, value, new RemoteStore.Listener() {
            @Override
            public void onResponse(final Response response) {
                if (response.status == Response.Status.SUCCESS) {
                    mLocalStore.put(accessToken, key, response.value);
                }
            }
        });
        return Response.pending(key, value);
    }

    @Override
    public Response delete(final String accessToken, final String key) {
        mRemoteStore.deleteAsync(accessToken, key, new RemoteStore.Listener() {
            @Override
            public void onResponse(final Response response) {
                if (response.status == Response.Status.SUCCESS) {
                    mLocalStore.delete(accessToken, key);
                }
            }
        });
        return Response.pending(key, null);
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
}
