/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/* package */ interface EtagStore {

    public void put(final String uri, final String etag);

    public String get(final String uri);

    public static class Default implements EtagStore {

        private static final String ETAG_CACHE = "PCFData:EtagCache";

        private final SharedPreferences mPrefs;

        public Default(final Context context) {
            mPrefs = context.getSharedPreferences(ETAG_CACHE, Context.MODE_PRIVATE);
        }

        @SuppressLint("CommitPrefEdits")
        public void put(final String uri, final String etag) {
            mPrefs.edit().putString(uri, etag).commit();
        }

        public String get(final String uri) {
            return mPrefs.getString(uri, null);
        }
    }
}
