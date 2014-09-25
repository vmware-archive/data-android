/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;

/* package */ class AuthStore {

    private final SharedPreferences mPrefs;

    public AuthStore(final Context context) {
        mPrefs = context.getSharedPreferences("account", Context.MODE_PRIVATE);
    }

    public void setAccountName(final String name) {
        mPrefs.edit().putString("name", name).commit();
    }

    public String getAccountName() {
        return mPrefs.getString("name", null);
    }
}
