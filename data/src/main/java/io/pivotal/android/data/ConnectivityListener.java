/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

public interface ConnectivityListener {
    public void onNetworkStatusChanged(final Context context, final boolean connected);
}
