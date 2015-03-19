/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class DataPersistence {

    private static final String EMPTY = "";

    private final SharedPreferences mPreferences;

    public DataPersistence(final Context context, final String namespace) {
        mPreferences = context.getSharedPreferences(namespace, Context.MODE_PRIVATE);
    }

    public String getString(final String key) {
        return mPreferences.getString(key, EMPTY);
    }

    @SuppressLint("CommitPrefEdits")
    public String putString(final String key, final String value) {
        mPreferences.edit().putString(key, value).commit();
        return value;
    }

    @SuppressLint("CommitPrefEdits")
    public String deleteString(final String key) {
        mPreferences.edit().remove(key).commit();
        return EMPTY;
    }

    @SuppressLint("CommitPrefEdits")
    public void clear() {
        mPreferences.edit().clear().commit();
    }
}
