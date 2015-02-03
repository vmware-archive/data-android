/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class ConnectivityReceiver extends BroadcastReceiver {

    private static boolean sIsConnected;
    private static ConnectivityListener sConnectivityListener = new ConnectivityListener() {

        @Override
        public void onNetworkConnected(final Context context) {
            Data.syncInBackground(context);
        }

        @Override
        public void onNetworkDisconnected(final Context context) {
        }
    };

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (sConnectivityListener == null) {
            return;
        }

        final boolean connected = Connectivity.isConnected(context);

        if (sIsConnected && !connected) {
            sConnectivityListener.onNetworkDisconnected(context);

        } else if (!sIsConnected && connected) {

            // TODO check network for real
            // http://guides.cocoahero.com/determining-web-service-reachability-on-android.html

            sConnectivityListener.onNetworkConnected(context);
        }

        sIsConnected = connected;
    }

    static void registerConnectivityListener(final Context context, final ConnectivityListener connectivityListener) {
        setConnectivityListener(connectivityListener);
        setIsConnected(Connectivity.isConnected(context));

        if (connectivityListener != null) {
            enableBroadcastReceiver(context);
        } else {
            disableBroadcastReceiver(context);
        }
    }

    static void setConnectivityListener(final ConnectivityListener connectivityListener) {
        sConnectivityListener = connectivityListener;
    }

    static void setIsConnected(final boolean isConnected) {
        sIsConnected = isConnected;
    }

    private static void enableBroadcastReceiver(final Context context) {
        changeBroadcastReceiverState(context, PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    private static void disableBroadcastReceiver(final Context context) {
        changeBroadcastReceiverState(context, PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
    }

    private static void changeBroadcastReceiverState(final Context context, final int state) {
        final PackageManager manager = context.getPackageManager();
        final ComponentName receiver = new ComponentName(context, ConnectivityReceiver.class);
        manager.setComponentEnabledSetting(receiver, state, PackageManager.DONT_KILL_APP);
    }

}
