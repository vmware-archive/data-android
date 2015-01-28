/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

import io.pivotal.android.data.DataStore.Observer;
import io.pivotal.android.data.DataStore.Listener;

public class KeyValueObject {

    public static KeyValueObject create(final Context context, final String collection, final String key) {
        final OfflineStore<KeyValue> dataStore = OfflineStore.createKeyValue(context);
        return new KeyValueObject(dataStore, collection, key);
    }

    private final DataStore<KeyValue> mDataStore;
    private final String mCollection, mKey;

    public KeyValueObject(final DataStore<KeyValue> dataStore, final String collection, final String key) {
        mDataStore = dataStore;
        mCollection = collection;
        mKey = key;
    }

    public Response<KeyValue> get(final String accessToken) {
        return get(accessToken, false);
    }

    public Response<KeyValue> get(final String accessToken, final boolean force) {
        Logger.d("Get: " + mKey);
        final Request<KeyValue> request = createRequest(accessToken, null, force);
        return mDataStore.get(request);
    }

    public void get(final String accessToken, final Listener<KeyValue> listener) {
        get(accessToken, false, listener);
    }

    public void get(final String accessToken, final boolean force, final Listener<KeyValue> listener) {
        Logger.d("Get: " + mKey);
        final Request<KeyValue> request = createRequest(accessToken, null, force);
        mDataStore.get(request, listener);
    }

    public Response<KeyValue> put(final String accessToken, final String value) {
        return put(accessToken, value, false);
    }

    public Response<KeyValue> put(final String accessToken, final String value, final boolean force) {
        Logger.d("Put: " + mKey + ", " + value);
        final Request<KeyValue> request = createRequest(accessToken, value, force);
        return mDataStore.put(request);
    }

    public void put(final String accessToken, final String value, final Listener<KeyValue> listener) {
        put(accessToken, value, false, listener);
    }

    public void put(final String accessToken, final String value, final boolean force, final Listener<KeyValue> listener) {
        Logger.d("Put: " + mKey + ", " + value);
        final Request<KeyValue> request = createRequest(accessToken, value, force);
        mDataStore.put(request, listener);
    }

    public Response<KeyValue> delete(final String accessToken) {
        return delete(accessToken, false);
    }

    public Response<KeyValue> delete(final String accessToken, final boolean force) {
        Logger.d("Delete: " + mKey);
        final Request<KeyValue> request = createRequest(accessToken, null, force);
        return mDataStore.delete(request);
    }

    public void delete(final String accessToken, final Listener<KeyValue> listener) {
        delete(accessToken, false, listener);
    }

    public void delete(final String accessToken, final boolean force, final Listener<KeyValue> listener) {
        Logger.d("Delete: " + mKey);
        final Request<KeyValue> request = createRequest(accessToken, null, force);
        mDataStore.delete(request, listener);
    }

    protected Request<KeyValue> createRequest(final String accessToken, final String value, final boolean force) {
        final KeyValue object = new KeyValue(mCollection, mKey, value);
        return new Request<KeyValue>(accessToken, object, force);
    }

    public boolean addObserver(final Observer<KeyValue> observer) {
        Logger.d("Add observer: " + observer);
        return mDataStore.addObserver(observer);
    }

    public boolean removeObserver(final Observer<KeyValue> observer) {
        Logger.d("Remove observer: " + observer);
        return mDataStore.removeObserver(observer);
    }
}
