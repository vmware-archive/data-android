/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;

public class KeyValueStore implements DataStore<KeyValue> {

    private static final String DATA_PREFIX = "PCFData:Data";

    private final ObserverHandler<KeyValue> mHandler;
    private final DataPersistence mPersistence;

    public KeyValueStore(final Context context) {
        this(new ObserverHandler<KeyValue>(), new DataPersistence(context, DATA_PREFIX));
    }

    public KeyValueStore(final ObserverHandler<KeyValue> handler, final DataPersistence persistence) {
        mHandler = handler;
        mPersistence = persistence;
    }

    private static String getIdentifier(final KeyValue object) {
        return object.collection + ":" + object.key;
    }

    @Override
    public Response<KeyValue> get(final Request<KeyValue> request) {
        Logger.d("Get: " + request.object);
        final Response<KeyValue> response = getResponse(request);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response<KeyValue> getResponse(final Request<KeyValue> request) {
        final String identifier = getIdentifier(request.object);
        final KeyValue responseObject = new KeyValue(request.object);
        responseObject.value = mPersistence.getString(identifier);
        return new Response<KeyValue>(responseObject);
    }

    @Override
    public void get(final Request<KeyValue> request, final Listener<KeyValue> listener) {
        new AsyncTask<Void, Void, Response<KeyValue>>() {

            @Override
            protected Response<KeyValue> doInBackground(final Void... params) {
                return KeyValueStore.this.get(request);
            }

            @Override
            protected void onPostExecute(final Response<KeyValue> resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public Response<KeyValue> put(final Request<KeyValue> request) {
        Logger.d("Put: " + request.object);
        final Response<KeyValue> response = putResponse(request);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response<KeyValue> putResponse(final Request<KeyValue> request) {
        final String identifier = getIdentifier(request.object);
        final KeyValue responseObject = new KeyValue(request.object);
        responseObject.value = mPersistence.putString(identifier, request.object.value);
        return new Response<KeyValue>(responseObject);
    }

    @Override
    public void put(final Request<KeyValue> request, final Listener<KeyValue> listener) {
        new AsyncTask<Void, Void, Response<KeyValue>>() {

            @Override
            protected Response<KeyValue> doInBackground(final Void... params) {
                return KeyValueStore.this.put(request);
            }

            @Override
            protected void onPostExecute(final Response<KeyValue> resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public Response<KeyValue> delete(final Request<KeyValue> request) {
        Logger.d("Delete: " + request.object);
        final Response<KeyValue> response = deleteResponse(request);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response<KeyValue> deleteResponse(final Request<KeyValue> request) {
        final String identifier = getIdentifier(request.object);
        final KeyValue responseObject = new KeyValue(request.object);
        responseObject.value = mPersistence.deleteString(identifier);
        return new Response<KeyValue>(responseObject);
    }

    @Override
    public void delete(final Request<KeyValue> request, final Listener<KeyValue> listener) {
        new AsyncTask<Void, Void, Response<KeyValue>>() {

            @Override
            protected Response<KeyValue> doInBackground(final Void... params) {
                return KeyValueStore.this.delete(request);
            }

            @Override
            protected void onPostExecute(final Response<KeyValue> resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public boolean addObserver(final Observer<KeyValue> observer) {
        return mHandler.addObserver(observer);
    }

    @Override
    public boolean removeObserver(final Observer<KeyValue> observer) {
        return mHandler.removeObserver(observer);
    }

    protected static class ObserverProxy implements OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

        }
    }

    // TODO change observers back to listening to shared prefs?
}
