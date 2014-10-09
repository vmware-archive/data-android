/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import java.util.HashMap;
import java.util.Map;

public class FakeSharedPreferences extends MockSharedPreferences {

    private final Map<String, Object> mMap = new HashMap<String, Object>();

    private OnSharedPreferenceChangeListener mListener;

    public FakeSharedPreferences(final String key, final String value) {
        mMap.put(key, value);
    }

    @Override
    public String getString(final String key, final String defValue) {
        return mMap.containsKey(key) ? (String) mMap.get(key) : defValue;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        mListener = null;
    }

    @Override
    public Editor edit() {
        return this;
    }

    @Override
    public Editor putString(final String key, final String value) {
        mMap.put(key, value);

        if (mListener != null) {
            mListener.onSharedPreferenceChanged(this, key);
        }

        return this;
    }

    @Override
    public boolean commit() {
        return true;
    }
}
