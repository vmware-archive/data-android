/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.os.Handler;
import android.os.Message;

import java.util.HashSet;
import java.util.Set;

public class ObserverHandler<T> extends Handler {

    private static final int MSG_NOTIFY = 1000;
    private static final int MSG_DELAY = 100;

    private final Object mLock = new Object();
    private final Set<DataStore.Observer<T>> mObservers = new HashSet<DataStore.Observer<T>>();

    public boolean addObserver(final DataStore.Observer<T> observer) {
        Logger.d("Add Observer: " + observer);
        synchronized (mLock) {
            return mObservers.add(observer);
        }
    }

    public boolean removeObserver(final DataStore.Observer<T> observer) {
        Logger.d("Remove Observer: " + observer);
        synchronized (mLock) {
            return mObservers.remove(observer);
        }
    }

    public Set<DataStore.Observer<T>> getObservers() {
        return mObservers;
    }

    public void notifyResponse(final Response<T> response) {
        final Message msg = obtainMessage(MSG_NOTIFY, response);

        removeMessages(MSG_NOTIFY);
        sendMessageDelayed(msg, MSG_DELAY);
    }

    @Override
    public void handleMessage(final Message msg) {
        if (msg.what == MSG_NOTIFY) {
            synchronized (mLock) {
                notifyObservers(msg);
            }
        }
    }

    private void notifyObservers(final Message msg) {
        for (final DataStore.Observer<T> observer : mObservers) {
            final Response<T> response = (Response<T>) msg.obj;
            Logger.d("Notify Observer response: " + response);
            observer.onResponse(response);
        }
    }
}
