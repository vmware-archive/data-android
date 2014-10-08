/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

public class MockDataStore implements DataStore {

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
