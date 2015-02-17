/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

import io.pivotal.android.data.DataStore.Listener;
import io.pivotal.android.data.DataStore.Observer;

public class KeyValueObject {

    public static KeyValueObject create(final Context context, final String collection, final String key) {
        final DataStore<KeyValue> dataStore = KeyValueOfflineStore.create(context);
        return new KeyValueObject(dataStore, collection, key);
    }

    private final DataStore<KeyValue> mDataStore;
    private final String mCollection, mKey;

    private boolean mForce;

    public KeyValueObject(final DataStore<KeyValue> dataStore, final String collection, final String key) {
        mDataStore = dataStore;
        mCollection = collection;
        mKey = key;
    }

    public void setShouldForceRequest(final boolean force) {
        mForce = force;
    }

    protected Request<KeyValue> createRequest(final int method, final String value) {
        Logger.d("REQUEST: Collection: " + mCollection + ", Key: " + mKey + ", Value: " + value + ", Force: " + mForce);
        final KeyValue object = new KeyValue(mCollection, mKey, value);
        return new Request<KeyValue>(method, object, mForce);
    }

    public Response<KeyValue> get() {
        Logger.d("Get: " + mKey);
        final Request<KeyValue> request = createRequest(Request.Methods.GET, null);
        return mDataStore.execute(request);
    }

    public void get(final Listener<KeyValue> listener) {
        Logger.d("Get: " + mKey);
        final Request<KeyValue> request = createRequest(Request.Methods.GET, null);
        mDataStore.execute(request, listener);
    }

    public Response<KeyValue> put(final String value) {
        Logger.d("Put: " + mKey + ", " + value);
        final Request<KeyValue> request = createRequest(Request.Methods.PUT, value);
        return mDataStore.execute(request);
    }

    public void put(final String value, final Listener<KeyValue> listener) {
        Logger.d("Put: " + mKey + ", " + value);
        final Request<KeyValue> request = createRequest(Request.Methods.PUT, value);
        mDataStore.execute(request, listener);
    }

    public Response<KeyValue> delete() {
        Logger.d("Delete: " + mKey);
        final Request<KeyValue> request = createRequest(Request.Methods.DELETE, null);
        return mDataStore.execute(request);
    }

    public void delete(final Listener<KeyValue> listener) {
        Logger.d("Delete: " + mKey);
        final Request<KeyValue> request = createRequest(Request.Methods.DELETE, null);
        mDataStore.execute(request, listener);
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
