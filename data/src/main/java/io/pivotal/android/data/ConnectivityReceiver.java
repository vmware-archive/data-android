/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

//        final String service = Context.CONNECTIVITY_SERVICE;
//        final ConnectivityManager manager = (ConnectivityManager)context.getSystemService(service);
//
//        final NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
//        final boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        debugIntent(intent);
    }

    private void debugIntent(final Intent intent) {
        Logger.v("action: " + intent.getAction());
        Logger.v("component: " + intent.getComponent());

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key: extras.keySet()) {
                Logger.v("key [" + key + "]: " + extras.get(key));
            }
        } else {
            Logger.v("no extras");
        }
    }

}
