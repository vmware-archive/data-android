/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

public class EtagStore {

    static final String ETAG_CACHE = "PCFData:EtagCache";

    private final DataPersistence mPersistence;

    public EtagStore(final Context context) {
        this(new DataPersistence(context, ETAG_CACHE));
    }

    public EtagStore(final DataPersistence persistence) {
        mPersistence = persistence;
    }

    public String put(final String url, final String etag) {
        return mPersistence.putString(url, etag);
    }

    public String get(final String url) {
        return mPersistence.getString(url);
    }
}
