/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.Random;

public class ConnectivityTest extends AndroidTestCase {

    private static final boolean RESULT = new Random().nextBoolean();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testIsConnected() {
        final Context context = Mockito.mock(Context.class);
        final NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);
        final ConnectivityManager manager = Mockito.mock(ConnectivityManager.class);

        Mockito.when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        Mockito.when(manager.getActiveNetworkInfo()).thenReturn(networkInfo);
        Mockito.when(networkInfo.isConnected()).thenReturn(RESULT);

        assertEquals(RESULT, Connectivity.isConnected(context));

        Mockito.verify(networkInfo).isConnected();
    }

    public void testIsConnectedReturnsFalseWhenActiveNetworkInfoIsNull() {
        final Context context = Mockito.mock(Context.class);
        final ConnectivityManager manager = Mockito.mock(ConnectivityManager.class);

        Mockito.when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        Mockito.when(manager.getActiveNetworkInfo()).thenReturn(null);

        assertEquals(false, Connectivity.isConnected(context));
    }
}