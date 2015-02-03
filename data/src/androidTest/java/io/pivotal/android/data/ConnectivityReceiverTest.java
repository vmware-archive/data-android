/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;


import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.test.AndroidTestCase;

import org.mockito.Mockito;

public class ConnectivityReceiverTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testOnReceiveWithAcquiredConnection() {
        final Context context = Mockito.mock(Context.class);
        final ConnectivityManager connManager = Mockito.mock(ConnectivityManager.class);
        final NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);
        final ConnectivityListener connectivityListener = Mockito.mock(ConnectivityListener.class);
        final PackageManager packageManager = Mockito.mock(PackageManager.class);

        Mockito.when(context.getPackageManager()).thenReturn(packageManager);
        Mockito.when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connManager);
        Mockito.when(connManager.getActiveNetworkInfo()).thenReturn(networkInfo);
        Mockito.when(networkInfo.isConnected()).thenReturn(true);

        ConnectivityReceiver receiver = new ConnectivityReceiver();
        receiver.setIsConnected(false);
        receiver.setConnectivityListener(connectivityListener);
        receiver.onReceive(context, null);

        Mockito.verify(context).getSystemService(Context.CONNECTIVITY_SERVICE);
        Mockito.verify(connManager).getActiveNetworkInfo();
        Mockito.verify(networkInfo).isConnected();
        Mockito.verify(connectivityListener).onNetworkConnected(context);
    }

    public void testOnReceiveWithLostConnection() {
        final Context context = Mockito.mock(Context.class);
        final ConnectivityManager connManager = Mockito.mock(ConnectivityManager.class);
        final NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);
        final ConnectivityListener connectivityListener = Mockito.mock(ConnectivityListener.class);
        final PackageManager packageManager = Mockito.mock(PackageManager.class);

        Mockito.when(context.getPackageManager()).thenReturn(packageManager);
        Mockito.when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connManager);
        Mockito.when(connManager.getActiveNetworkInfo()).thenReturn(networkInfo);
        Mockito.when(networkInfo.isConnected()).thenReturn(false);

        ConnectivityReceiver receiver = new ConnectivityReceiver();
        receiver.setIsConnected(true);
        receiver.setConnectivityListener(connectivityListener);
        receiver.onReceive(context, null);

        Mockito.verify(context).getSystemService(Context.CONNECTIVITY_SERVICE);
        Mockito.verify(connManager).getActiveNetworkInfo();
        Mockito.verify(networkInfo).isConnected();
        Mockito.verify(connectivityListener, Mockito.never()).onNetworkConnected(context);
    }
}
