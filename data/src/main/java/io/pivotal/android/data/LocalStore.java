/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;

public class LocalStore implements DataStore {

    private static final String EMPTY = "";
    private static final String COLLECTION_PREFIX = "PCFData:Collection:";

    private final String mCollection;
    private final ObserverHandler mHandler;
    private final SharedPreferences mPreferences;

    public LocalStore(final Context context, final String collection) {
        this(collection, new ObserverHandler(), createSharedPreferences(context, collection));
    }

    private static SharedPreferences createSharedPreferences(final Context context, final String collection) {
        return context.getSharedPreferences(COLLECTION_PREFIX + collection, Context.MODE_PRIVATE);
    }

    LocalStore(final String collection, final ObserverHandler handler, final SharedPreferences preferences) {
        mCollection = collection;
        mHandler = handler;
        mPreferences = preferences;
    }

    @Override
    public boolean contains(final String accessToken, final String key) {
        Logger.d("Contains: " + key);
        return mPreferences.contains(key);
    }

    @Override
    public Response get(final String accessToken, final String key) {
        Logger.d("Get: " + key);
        final Response response = getResponse(key);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response getResponse(final String key) {
        final String value = mPreferences.getString(key, EMPTY);
        return new Response(key, value);
    }

    @Override
    public void get(final String accessToken, final String key, final Listener listener) {
        new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(final Void... params) {
                return LocalStore.this.get(accessToken, key);
            }

            @Override
            protected void onPostExecute(final Response resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public Response put(final String accessToken, final String key, final String value) {
        Logger.d("Put: " + key + ", " + value);
        final Response response = putResponse(key, value);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response putResponse(final String key, final String value) {
        mPreferences.edit().putString(key, value).apply();
        return new Response(key, value);
    }

    @Override
    public void put(final String accessToken, final String key, final String value, final Listener listener) {
        new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(final Void... params) {
                return LocalStore.this.put(accessToken, key, value);
            }

            @Override
            protected void onPostExecute(final Response resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public Response delete(final String accessToken, final String key) {
        Logger.d("Delete: " + key);
        final Response response = deleteResponse(key);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response deleteResponse(final String key) {
        mPreferences.edit().remove(key).apply();
        return new Response(key, EMPTY);
    }

    @Override
    public void delete(final String accessToken, final String key, final Listener listener) {
        new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(final Void... params) {
                return LocalStore.this.delete(accessToken, key);
            }

            @Override
            protected void onPostExecute(final Response resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public boolean addObserver(final Observer observer) {
        return mHandler.addObserver(observer);
    }

    @Override
    public boolean removeObserver(final Observer observer) {
        return mHandler.removeObserver(observer);
    }

    protected static class ObserverProxy implements OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

        }
    }

    // TODO change observers back to listening to shared prefs?
}
