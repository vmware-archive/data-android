/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

import io.pivotal.android.data.DataStore.Observer;
import io.pivotal.android.data.DataStore.Response;
import io.pivotal.android.data.DataStore.Listener;

public class DataObject {

    public static DataObject create(final Context context, final String collection, final String key) {
        final DataStore dataStore = OfflineStore.create(context, collection);
        return new DataObject(dataStore, key);
    }

    private final DataStore mDataStore;
    private final String mKey;

    public DataObject(final DataStore dataStore, final String key) {
        mDataStore = dataStore;
        mKey = key;
    }

    public Response get(final String accessToken) {
        Logger.d("Get: " + mKey);
        return mDataStore.get(accessToken, mKey);
    }

    public void get(final String accessToken, final Listener listener) {
        Logger.d("Get: " + mKey);
        mDataStore.get(accessToken, mKey, listener);
    }

    public Response put(final String accessToken, final String value) {
        Logger.d("Put: " + mKey + ", " + value);
        return mDataStore.put(accessToken, mKey, value);
    }

    public void put(final String accessToken, final String value, final Listener listener) {
        Logger.d("Put: " + mKey + ", " + value);
        mDataStore.put(accessToken, mKey, value, listener);
    }

    public Response delete(final String accessToken) {
        Logger.d("Delete: " + mKey);
        return mDataStore.delete(accessToken, mKey);
    }

    public void delete(final String accessToken, final Listener listener) {
        Logger.d("Delete: " + mKey);
        mDataStore.delete(accessToken, mKey, listener);
    }

    public boolean addObserver(final Observer observer) {
        Logger.d("Add observer: " + observer);
        return mDataStore.addObserver(observer);
    }

    public boolean removeObserver(final Observer observer) {
        Logger.d("Remove observer: " + observer);
        return mDataStore.removeObserver(observer);
    }
}
