/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FakePreferences implements SharedPreferences, SharedPreferences.Editor {

    private final Map<String, Object> mMap = new HashMap<String, Object>();

    private OnSharedPreferenceChangeListener mListener;

    public FakePreferences(final String key, final String value) {
        mMap.put(key, value);
    }

    @Override
    public String getString(final String key, final String defValue) {
        return mMap.containsKey(key) ? (String) mMap.get(key) : defValue;
    }

    @Override
    public Map<String, ?> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getStringSet(final String key, final Set<String> defValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(final String key, final int defValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(final String key, final long defValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(final String key, final float defValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(final String key, final boolean defValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(final String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor edit() {
        return this;
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
    public Editor putString(final String key, final String value) {
        mMap.put(key, value);

        if (mListener != null) {
            mListener.onSharedPreferenceChanged(this, key);
        }

        return this;
    }

    @Override
    public Editor putStringSet(final String key, final Set<String> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor putInt(final String key, final int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor putLong(final String key, final long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor putFloat(final String key, final float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor putBoolean(final String key, final boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor remove(final String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean commit() {
        return true;
    }

    @Override
    public void apply() {
        throw new UnsupportedOperationException();
    }
}
