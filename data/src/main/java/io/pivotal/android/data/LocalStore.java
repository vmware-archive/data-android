/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;

public abstract class LocalStore<T> implements DataStore<T> {

    static final String DATA_PREFIX = "PCFData:Data";

    private final ObserverHandler<T> mHandler;
    private final DataPersistence mPersistence;

    public LocalStore(final Context context) {
        this(new ObserverHandler<T>(), new DataPersistence(context, DATA_PREFIX));
    }

    public LocalStore(final ObserverHandler<T> handler, final DataPersistence persistence) {
        mHandler = handler;
        mPersistence = persistence;
    }

    protected ObserverHandler<T> getHandler() {
        return mHandler;
    }

    protected DataPersistence getPersistence() {
        return mPersistence;
    }

    @Override
    public void execute(final Request<T> request, final Listener<T> listener) {
        new AsyncTask<Void, Void, Response<T>>() {

            @Override
            protected Response<T> doInBackground(final Void... params) {
                return LocalStore.this.execute(request);
            }

            @Override
            protected void onPostExecute(final Response<T> resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public boolean addObserver(final Observer<T> observer) {
        return mHandler.addObserver(observer);
    }

    @Override
    public boolean removeObserver(final Observer<T> observer) {
        return mHandler.removeObserver(observer);
    }

    protected static class ObserverProxy implements OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

        }
    }

    // TODO change observers back to listening to shared prefs?
}
