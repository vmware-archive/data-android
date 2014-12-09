/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.os.Handler;
import android.os.Message;

import java.util.Set;

public class ObserverHandler extends Handler {

    private static final int MSG_NOTIFY = 1000;

    private final Object mLock;
    private final Set<DataStore.Observer> mObservers;

    public ObserverHandler(final Set<DataStore.Observer> observers, final Object lock) {
        mObservers = observers;
        mLock = lock;
    }

    public void notifyResponse(final DataStore.Response response) {
        removeMessages(MSG_NOTIFY);

        final Message msg = obtainMessage(MSG_NOTIFY, response);
        sendMessageDelayed(msg, 100);
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
            final DataStore.Response resp = (DataStore.Response) msg.obj;
            if (resp.isFailure()) {
                Logger.d("Notify Observer failure: " + resp.key + ", " + resp.error);
                observer.onError(resp.key, resp.error);
            } else {
                Logger.d("Notify Observer success: " + resp.key + ", " + resp.value);
                observer.onChange(resp.key, resp.value);
            }
        }
    }
}
