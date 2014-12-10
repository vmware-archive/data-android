/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.os.Handler;
import android.os.Message;

import java.util.HashSet;
import java.util.Set;

public class ObserverHandler extends Handler {

    private static final int MSG_NOTIFY = 1000;
    private static final int MSG_DELAY = 100;

    private final Object mLock = new Object();
    private final Set<DataStore.Observer> mObservers = new HashSet<DataStore.Observer>();

    public boolean addObserver(final DataStore.Observer observer) {
        Logger.d("Add Observer: " + observer);
        synchronized (mLock) {
            return mObservers.add(observer);
        }
    }

    public boolean removeObserver(final DataStore.Observer observer) {
        Logger.d("Remove Observer: " + observer);
        synchronized (mLock) {
            return mObservers.remove(observer);
        }
    }

    public Set<DataStore.Observer> getObservers() {
        return mObservers;
    }

    public void notifyResponse(final DataStore.Response response) {
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
        for (final DataStore.Observer observer : mObservers) {
            final DataStore.Response response = (DataStore.Response) msg.obj;
            Logger.d("Notify Observer response: " + response);
            observer.onResponse(response);
        }
    }
}
