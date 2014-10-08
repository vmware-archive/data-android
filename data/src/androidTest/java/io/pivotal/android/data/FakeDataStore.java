/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

public class FakeDataStore implements DataStore {

    private String mKey;
    private String mValue;

    private Observer mObserver;

    public FakeDataStore() {
        mKey = null;
        mValue = null;
    }

    public FakeDataStore(final String key, final String value) {
        mKey = key;
        mValue = value;
    }

    @Override
    public boolean contains(final String token, final String key) {
        return mKey != null && mKey.equals(key);
    }

    @Override
    public Response get(final String token, final String key) {
        if (contains(token, key)) {
            return Response.success(mKey, mValue);
        } else {
            return Response.failure(key, null);
        }
    }

    @Override
    public Response put(final String token, final String key, final String value) {
        try {
            mKey = key;
            mValue = value;

            return Response.success(key, value);

        } finally {
            if (mObserver != null) {
                mObserver.onChange(key, value);
            }
        }
    }

    @Override
    public Response delete(final String token, final String key) {
        if (contains(token, key)) {
            mKey = null;
            mValue = null;
            return Response.success(key, null);
        } else {
            return Response.failure(key, null);
        }
    }

    @Override
    public boolean addObserver(final Observer observer) {
        mObserver = observer;
        return true;
    }

    @Override
    public boolean removeObserver(final Observer observer) {
        mObserver = null;
        return true;
    }
}
