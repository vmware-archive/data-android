/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

public class Data {

    public static void registerConnectivityListener(final Context context, final ConnectivityListener connectivityListener) {
        ConnectivityReceiver.registerConnectivityListener(context, connectivityListener);
    }

    public static void syncWithAccessToken(final Context context, final String accessToken) {
        final OfflineStore<KeyValue> offlineStore = OfflineStore.createKeyValue(context);
        offlineStore.getRequestCache().executePending(accessToken);
    }

    public static void syncInBackgroundWithAccessToken(final Context context, final String accessToken) {
        final OfflineStore<KeyValue> offlineStore = OfflineStore.createKeyValue(context);
        offlineStore.getRequestCache().executePendingAsync(accessToken);
    }
}
