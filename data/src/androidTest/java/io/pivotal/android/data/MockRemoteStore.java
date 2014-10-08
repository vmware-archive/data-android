/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.test.mock.MockContext;

import java.util.Set;

public class MockRemoteStore extends RemoteStore {

    public MockRemoteStore() {
        super(new MockContext(), null);
    }

    @Override
    protected ObserverHandler createObserverHandler(Set<Observer> observers, Object lock) {
        return null;
    }

    @Override
    protected RemoteClient createRemoteClient(Context context) {
        return null;
    }

    @Override
    public boolean contains(final String token, final String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response get(final String token, final String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response put(final String token, final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response delete(final String token, final String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addObserver(final Observer observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeObserver(final Observer observer) {
        throw new UnsupportedOperationException();
    }
}
