/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

public interface ConnectivityListener {
    public void onNetworkConnected(final Context context);
    public void onNetworkDisconnected(final Context context);
}
