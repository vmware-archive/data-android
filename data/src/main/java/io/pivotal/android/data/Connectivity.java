/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Connectivity {

    public static boolean isConnected(final Context context) {
        final String service = Context.CONNECTIVITY_SERVICE;
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(service);

        final NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
