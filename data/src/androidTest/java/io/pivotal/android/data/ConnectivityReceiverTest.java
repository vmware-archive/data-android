/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
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

    public void testOnReceiveWithNoNetworkConnection() {
        final Context context = Mockito.mock(Context.class);
        final NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);
        final ConnectivityManager manager = Mockito.mock(ConnectivityManager.class);
        final ConnectivityReceiver receiver = Mockito.spy(new ConnectivityReceiver());

        Mockito.when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        Mockito.when(manager.getActiveNetworkInfo()).thenReturn(networkInfo);
        Mockito.when(networkInfo.isConnectedOrConnecting()).thenReturn(false);

        receiver.onReceive(context, null);

        Mockito.verify(context).getSystemService(Context.CONNECTIVITY_SERVICE);
        Mockito.verify(manager).getActiveNetworkInfo();
        Mockito.verify(networkInfo).isConnectedOrConnecting();
        Mockito.verify(receiver, Mockito.never()).getRequestCache(context);
    }

    public void testOnReceiveWithNetworkConnection() {
        final Context context = Mockito.mock(Context.class);
        final RequestCache requestCache = Mockito.mock(RequestCache.class);
        final NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);
        final ConnectivityManager manager = Mockito.mock(ConnectivityManager.class);
        final ConnectivityReceiver receiver = Mockito.spy(new ConnectivityReceiver());

        Mockito.when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        Mockito.when(manager.getActiveNetworkInfo()).thenReturn(networkInfo);
        Mockito.when(networkInfo.isConnectedOrConnecting()).thenReturn(true);
        Mockito.doReturn(requestCache).when(receiver).getRequestCache(context);
        Mockito.doNothing().when(requestCache).executePending(null);

        receiver.onReceive(context, null);

        Mockito.verify(context).getSystemService(Context.CONNECTIVITY_SERVICE);
        Mockito.verify(manager).getActiveNetworkInfo();
        Mockito.verify(networkInfo).isConnectedOrConnecting();
        Mockito.verify(receiver).getRequestCache(context);
        Mockito.verify(requestCache).executePending(null);
    }
}