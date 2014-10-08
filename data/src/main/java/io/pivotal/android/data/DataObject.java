/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class DataObject {

    public static interface Observer {
        public void onChange(String key, String value);
        public void onError(String key, DataError error);
    }

    public static DataObject create(final Context context, final String collection, final String key) {
        final DataStore dataStore = DefaultStore.create(context, collection);
        return new DataObject(dataStore, key);
    }

    private final Map<Observer, ObserverProxy> mObservers = new HashMap<Observer, ObserverProxy>();

    private final DataStore mDataStore;
    private final String mKey;

    public DataObject(final DataStore dataStore, final String key) {
        mDataStore = dataStore;
        mKey = key;
    }

    public String get(final String accessToken) {
        Logger.d("Get value for key: " + mKey);
        return mDataStore.get(accessToken, mKey).value;
    }

    public void put(final String accessToken, final String value) {
        Logger.d("Put value: " + value + " for key: " + mKey);
        mDataStore.put(accessToken, mKey, value);
    }

    public void delete(final String accessToken) {
        Logger.d("Delete value for key: " + mKey);
        mDataStore.delete(accessToken, mKey);
    }

    public boolean addObserver(final Observer observer) {
        Logger.d("Add observer: " + observer);
        if (!mObservers.containsKey(observer)) {
            final ObserverProxy proxy = new ObserverProxy(observer, mKey);
            mDataStore.addObserver(proxy);
            mObservers.put(observer, proxy);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeObserver(final Observer observer) {
        Logger.d("Remove observer: " + observer);
        if (mObservers.containsKey(observer)) {
            final ObserverProxy proxy = mObservers.remove(observer);
            mDataStore.removeObserver(proxy);
            return true;
        } else {
            return false;
        }
    }

    private static class ObserverProxy implements DataStore.Observer {

        private final Observer mObserver;
        private final String mKey;

        private ObserverProxy(final Observer observer, final String key) {
            mObserver = observer;
            mKey = key;
        }

        @Override
        public void onChange(final String key, final String value) {
            if (mObserver != null && mKey != null && mKey.equals(key)) {
                Logger.d("Observer Changed: " + key + ", " + value);
                mObserver.onChange(key, value);
            }
        }

        @Override
        public void onError(final String key, final DataError error) {
            if (mObserver != null && mKey != null && mKey.equals(key)) {
                Logger.d("Observer Error: " + key + ", " + error);
                mObserver.onError(key, error);
            }
        }
    }
}
