/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.os.Message;

import java.util.Set;

public class FakeObserverHandler extends ObserverHandler {

    public FakeObserverHandler(final Set<DataStore.Observer> observers, final Object lock) {
        super(observers, lock);
    }

    @Override
    public boolean sendMessageAtTime(final Message msg, final long uptimeMillis) {
        handleMessage(msg);
        return true;
    }
}
