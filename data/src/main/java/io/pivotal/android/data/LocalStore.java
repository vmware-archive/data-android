/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.HashSet;
import java.util.Set;

public class LocalStore implements DataStore {

    private final Object mLock = new Object();
    private final Set<Observer> mObservers = new HashSet<Observer>();

    private final SharedPreferences mPreferences;
    private final ObserverHandler mHandler;

    public LocalStore(final Context context, final String collection) {
        mPreferences = context.getSharedPreferences(collection, Context.MODE_PRIVATE);
        mPreferences.registerOnSharedPreferenceChangeListener(mListener);
        mHandler = createObserverHandler(mObservers, mLock);
    }

    /* package */ ObserverHandler createObserverHandler(final Set<Observer> observers, final Object lock) {
        return new ObserverHandler(observers, lock);
    }

    @Override
    public boolean contains(final String accessToken, final String key) {
        Logger.d("Contains: " + key);
        return mPreferences.contains(key);
    }

    @Override
    public Response get(final String accessToken, final String key) {
        Logger.d("Get: " + key);
        final String value = mPreferences.getString(key, null);
        return Response.success(key, value);
    }

    @Override
    public Response put(final String accessToken, final String key, final String value) {
        Logger.d("Put: " + key + ", " + value);
        mPreferences.edit().putString(key, value).commit();
        return Response.success(key, value);
    }

    @Override
    public Response delete(final String accessToken, final String key) {
        Logger.d("Delete: " + key);
        mPreferences.edit().remove(key).commit();
        return Response.success(key, null);
    }

    @Override
    public boolean addObserver(final Observer observer) {
        Logger.d("Add Observer: " + observer);
        synchronized (mLock) {
            return mObservers.add(observer);
        }
    }

    @Override
    public boolean removeObserver(final Observer observer) {
        Logger.d("Remove Observer: " + observer);
        synchronized (mLock) {
            return mObservers.remove(observer);
        }
    }

    private final OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
            final String value = prefs.getString(key, null);
            Logger.d("Shared Preferences Changed: " + key + ", " + value);
            mHandler.postResponse(Response.success(key, value));
        }
    };
}