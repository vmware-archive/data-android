/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (isConnected(context)) {
            onConnected(context, intent);
        }
    }

    public void onConnected(final Context context, final Intent intent) {
        executePendingRequests(context, null);
    }

    public void executePendingRequests(final Context context, final String token) {
        final RequestCache cache = getRequestCache(context);
        cache.executePendingRequests(context, token);
    }

    protected RequestCache getRequestCache(final Context context) {
        return new RequestCache.Default(context);
    }

    public static boolean isConnected(final Context context) {
        final String service = Context.CONNECTIVITY_SERVICE;
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(service);

        final NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static boolean hasReceiver(final Context context) {
        try {
            final int flags = PackageManager.GET_RECEIVERS;
            final String packageName = context.getPackageName();
            final PackageManager manager = context.getPackageManager();
            return hasReceiver(manager.getPackageInfo(packageName, flags));
        } catch (final Exception e) {
            return false;
        }
    }

    private static boolean hasReceiver(final PackageInfo info) throws Exception {
        for (int i = 0; i < info.receivers.length; i++) {
            final Class<?> klass = Class.forName(info.receivers[i].name);
            if (ConnectivityReceiver.class.isAssignableFrom(klass)) {
                return true;
            }
        }
        return false;
    }

}
