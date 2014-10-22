/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultStore implements DataStore {

    private final ObjectMapper mMapper = new ObjectMapper();

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
    public boolean contains(final String accessToken, final String key) {
        return mLocalStore.contains(accessToken, key);
    }

    @Override
    public boolean addObserver(final Observer observer) {
        return mRemoteStore.addObserver(observer);
    }

    @Override
    public boolean removeObserver(final Observer observer) {
        return mRemoteStore.removeObserver(observer);
    }

    @Override
    public Response get(final String accessToken, final String key) {
        Logger.d("Get: " + key);
        try {
            final Response response = mLocalStore.get(accessToken, key);
            final KeyValue result = decodeKeyValue(response.value);

            mRemoteStore.getAsync(accessToken, key, new UpdateListener(accessToken));

            return Response.pending(result.key, result.value);
        } catch (final Exception e) {
            Logger.ex(e);
            return Response.failure(key, new DataError(e));
        }
    }

    @Override
    public Response put(final String accessToken, final String key, final String value) {
        Logger.d("Put: " + key + ", " + value);
        try {
            final KeyValue result = new KeyValue(key, value, KeyValue.State.PENDING);

            mLocalStore.put(accessToken, key, encodeKeyValue(result));
            mRemoteStore.putAsync(accessToken, key, value, new UpdateListener(accessToken));

            return Response.pending(result.key, result.value);
        } catch (final Exception e) {
            return Response.failure(key, new DataError(e));
        }
    }

    @Override
    public Response delete(final String accessToken, final String key) {
        Logger.d("Delete: " + key);
        try {
            final KeyValue result = new KeyValue(key, null, KeyValue.State.PENDING);

            mLocalStore.put(accessToken, key, encodeKeyValue(result));
            mRemoteStore.deleteAsync(accessToken, key, new DeleteListener(accessToken));

            return Response.pending(result.key, result.value);
        } catch (final Exception e) {
            return Response.failure(key, new DataError(e));
        }
    }

    private String encodeKeyValue(final KeyValue result) throws Exception {
        final byte[] json = mMapper.writeValueAsBytes(result);
        final byte[] bytes = Base64.encode(json, Base64.DEFAULT);
        return new String(bytes);
    }

    private KeyValue decodeKeyValue(final String value) throws Exception {
        final byte[] bytes = Base64.decode(value, Base64.DEFAULT);
        return mMapper.readValue(bytes, KeyValue.class);
    }

    private static final class KeyValue {

        public static enum State {
            DEFAULT, PENDING;
        }

        public String key, value;
        public State state;

        public KeyValue() {
        }

        public KeyValue(final String key, final String value) {
            this(key, value, State.DEFAULT);
        }

        public KeyValue(final String key, final String value, final State state) {
            this.key = key;
            this.value = value;
            this.state = state;
        }
    }

    private final class UpdateListener implements RemoteStore.Listener {

        private final String mAccessToken;

        public UpdateListener(final String accessToken) {
            mAccessToken = accessToken;
        }

        @Override
        public void onResponse(final Response response) {
            if (response.status == Response.Status.SUCCESS) {
                try {
                    final KeyValue result = new KeyValue(response.key, response.value);
                    mLocalStore.put(mAccessToken, response.key, encodeKeyValue(result));
                } catch (final Exception e) {
                    Logger.ex(e);
                }
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
                try {
                    mLocalStore.delete(mAccessToken, response.key);
                } catch (final Exception e) {
                    Logger.ex(e);
                }
            }
        }
    }
}
