/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class DefaultStore implements DataStore {

    private final Object mLock = new Object();
    private final Map<Observer, ObserverProxy> mObservers = new HashMap<Observer, ObserverProxy>();

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

//    @Override
//    public boolean addObserver(final Observer observer) {
//        return mRemoteStore.addObserver(observer);
//    }
//
//    @Override
//    public boolean removeObserver(final Observer observer) {
//        return mRemoteStore.removeObserver(observer);
//    }

    public boolean addObserver(final Observer observer) {
        Logger.d("Add observer: " + observer);
        synchronized (mLock) {
            if (!mObservers.containsKey(observer)) {
                final ObserverProxy proxy = createProxy(observer);
                mLocalStore.addObserver(proxy);
                mObservers.put(observer, proxy);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean removeObserver(final Observer observer) {
        Logger.d("Remove observer: " + observer);
        synchronized (mLock) {
            if (mObservers.containsKey(observer)) {
                final ObserverProxy proxy = mObservers.remove(observer);
                mLocalStore.removeObserver(proxy);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public Response get(final String accessToken, final String key) {
        Logger.d("Get: " + key);
        try {
            final Response response = mLocalStore.get(accessToken, key);
            final KeyValue result = ByteUtils.decodeKeyValue(response.value);

            getRemotelyOrStorePending(accessToken, key);

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
            final String encodedValue = ByteUtils.encodeKeyValue(result);

            mLocalStore.put(accessToken, key, encodedValue);

            putRemotelyOrStorePending(accessToken, key, value);

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
            final String encodedValue = ByteUtils.encodeKeyValue(result);

            mLocalStore.put(accessToken, key, encodedValue);

            deleteRemotelyOrStorePending(accessToken, key);

            return Response.pending(result.key, result.value);

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


    private static final class KeyValue {

        public String key, value;
        public State state;

        public KeyValue() {

        }

        public KeyValue(final String key, final String value, final State state) {
            this.key = key; this.value = value; this.state = state;
        }

        public static enum State {
            DEFAULT, PENDING;
        }
    }

    private static final class ByteUtils {

        public static String encodeKeyValue(final KeyValue result) throws Exception {
            final ObjectMapper mapper = new ObjectMapper();
            final byte[] json = mapper.writeValueAsBytes(result);
            final byte[] bytes = Base64.encode(json, Base64.DEFAULT);
            return new String(bytes);
        }

        public static KeyValue decodeKeyValue(final String value) throws Exception {
            final byte[] bytes = Base64.decode(value, Base64.DEFAULT);
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(bytes, KeyValue.class);
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
                    final KeyValue result = new KeyValue(response.key, response.value, KeyValue.State.DEFAULT);
                    mLocalStore.put(mAccessToken, response.key, ByteUtils.encodeKeyValue(result));
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

    /* package */ ObserverProxy createProxy(final Observer observer) {
        return new ObserverProxy(observer);
    }

    /* package */ Map<Observer, ObserverProxy> getObservers() {
        synchronized (mLock) {
            return mObservers;
        }
    }

    /* package */ static class ObserverProxy implements DataStore.Observer {

        private final Observer mObserver;

        public ObserverProxy(final Observer observer) {
            mObserver = observer;
        }

        @Override
        public void onChange(final String key, final String value) {
            if (mObserver != null) {
                Logger.d("Observer Changed: " + key + ", " + value);
                try {
                    final KeyValue result = ByteUtils.decodeKeyValue(value);
                    mObserver.onChange(key, result.value);
                } catch (final Exception e) {
                    Logger.ex(e);
                }
            }
        }

        @Override
        public void onError(final String key, final DataError error) {
            if (mObserver != null) {
                Logger.d("Observer Error: " + key + ", " + error);
                mObserver.onError(key, error);
            }
        }
    }
}