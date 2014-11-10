/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.Random;
import java.util.UUID;

public class RequestCacheTest extends AndroidTestCase {

    private static final int METHOD = new Random().nextInt();
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final String TOKEN = UUID.randomUUID().toString();
    private static final String FALLBACK = UUID.randomUUID().toString();

    private static final String COLLECTION = UUID.randomUUID().toString();

    private static final String SINGLE_LIST = "[{"
            + "\"collection\":\"" + COLLECTION + "\""
            + ",\"fallback\":\"" + FALLBACK + "\""
            + ",\"key\":\"" + KEY + "\""
            + ",\"token\":\"" + TOKEN + "\""
            + ",\"value\":\"" + VALUE + "\""
            + ",\"method\":" + METHOD
        + "}]";

    private static final String STATIC_LIST = "[{"
            + "\"collection\":\"" + "collection" + "\""
            + ",\"fallback\":\"" + "fallback" + "\""
            + ",\"key\":\"" + "key" + "\""
            + ",\"token\":\"" + "token" + "\""
            + ",\"value\":\"" + "value" + "\""
            + ",\"method\":" + 1
        + "}]";

    private static final String MERGED_LIST = "[{"
            + "\"collection\":\"" + "collection" + "\""
            + ",\"fallback\":\"" + "fallback" + "\""
            + ",\"key\":\"" + "key" + "\""
            + ",\"token\":\"" + "token" + "\""
            + ",\"value\":\"" + "value" + "\""
            + ",\"method\":" + 1
        + "},{"
            + "\"collection\":\"" + COLLECTION + "\""
            + ",\"fallback\":\"" + FALLBACK + "\""
            + ",\"key\":\"" + KEY + "\""
            + ",\"token\":\"" + TOKEN + "\""
            + ",\"value\":\"" + VALUE + "\""
            + ",\"method\":" + METHOD
        + "}]";

    private static final String GET_LIST = "[{"
            + "\"collection\":\"" + COLLECTION + "\""
            + ",\"fallback\":\"" + FALLBACK + "\""
            + ",\"key\":\"" + KEY + "\""
            + ",\"token\":\"" + TOKEN + "\""
            + ",\"value\":\"" + VALUE + "\""
            + ",\"method\":" + 0
        + "}]";

    private static final String PUT_LIST = "[{"
            + "\"collection\":\"" + COLLECTION + "\""
            + ",\"fallback\":\"" + FALLBACK + "\""
            + ",\"key\":\"" + KEY + "\""
            + ",\"token\":\"" + TOKEN + "\""
            + ",\"value\":\"" + VALUE + "\""
            + ",\"method\":" + 1
        + "}]";

    private static final String DELETE_LIST = "[{"
            + "\"collection\":\"" + COLLECTION + "\""
            + ",\"fallback\":\"" + FALLBACK + "\""
            + ",\"key\":\"" + KEY + "\""
            + ",\"token\":\"" + TOKEN + "\""
            + ",\"value\":\"" + VALUE + "\""
            + ",\"method\":" + 2
        + "}]";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testAddGetRequestInvokesAddPendingRequest() {
        final Context context = Mockito.mock(Context.class);
        final RequestCache.Default requestCache = Mockito.spy(new RequestCache.Default(context));

        Mockito.doNothing().when(requestCache).queuePending(Mockito.any(RequestCache.Default.PendingRequest.class));

        requestCache.queueGet(TOKEN, COLLECTION, KEY);

        Mockito.verify(requestCache).queuePending(Mockito.any(RequestCache.Default.PendingRequest.class));
    }

    public void testAddPutRequestInvokesAddPendingRequest() {
        final Context context = Mockito.mock(Context.class);
        final RequestCache.Default requestCache = Mockito.spy(new RequestCache.Default(context));

        Mockito.doNothing().when(requestCache).queuePending(Mockito.any(RequestCache.Default.PendingRequest.class));

        requestCache.queuePut(TOKEN, COLLECTION, KEY, VALUE, FALLBACK);

        Mockito.verify(requestCache).queuePending(Mockito.any(RequestCache.Default.PendingRequest.class));
    }

    public void testAddDeleteRequestInvokesAddPendingRequest() {
        final Context context = Mockito.mock(Context.class);
        final RequestCache.Default requestCache = Mockito.spy(new RequestCache.Default(context));

        Mockito.doNothing().when(requestCache).queuePending(Mockito.any(RequestCache.Default.PendingRequest.class));

        requestCache.queueDelete(TOKEN, COLLECTION, KEY, FALLBACK);

        Mockito.verify(requestCache).queuePending(Mockito.any(RequestCache.Default.PendingRequest.class));
    }

    @SuppressLint("CommitPrefEdits")
    public void testAddPendingRequestStoresObjectInSharedPreferencesWhenEmpty() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(context.getSharedPreferences("request_cache", Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.getString("requests", "")).thenReturn("");
        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.putString("requests", SINGLE_LIST)).thenReturn(editor);
        Mockito.doNothing().when(editor).apply();

        final RequestCache.Default requestCache = new RequestCache.Default(context);
        requestCache.queuePending(new RequestCache.Default.PendingRequest(METHOD, TOKEN, COLLECTION, KEY, VALUE, FALLBACK));

        Mockito.verify(preferences).edit();
        Mockito.verify(editor).putString("requests", SINGLE_LIST);
        Mockito.verify(editor).apply();
    }

    @SuppressLint("CommitPrefEdits")
    public void testAddPendingRequestStoresObjectInSharedPreferencesWhenNotEmpty() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(context.getSharedPreferences("request_cache", Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.getString("requests", "")).thenReturn(STATIC_LIST);
        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.putString("requests", MERGED_LIST)).thenReturn(editor);
        Mockito.doNothing().when(editor).apply();

        final RequestCache.Default requestCache = new RequestCache.Default(context);
        requestCache.queuePending(new RequestCache.Default.PendingRequest(METHOD, TOKEN, COLLECTION, KEY, VALUE, FALLBACK));

        Mockito.verify(preferences).edit();
        Mockito.verify(editor).putString("requests", MERGED_LIST);
        Mockito.verify(editor).apply();
    }

    public void testExecutePendingRequestsExecutesGetRequestAndClearsRequests() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(context.getSharedPreferences("request_cache", Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.getString("requests", "")).thenReturn(GET_LIST);
        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.putString("requests", "")).thenReturn(editor);
        Mockito.doNothing().when(editor).apply();

        final RequestCache.Default requestCache = Mockito.spy(new RequestCache.Default(context));
        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);

        Mockito.doReturn(offlineStore).when(requestCache).getOfflineStore(context, COLLECTION);
        Mockito.doNothing().when(offlineStore).get(TOKEN, KEY, null);

        requestCache.executePending(null);

        Mockito.verify(offlineStore).get(TOKEN, KEY, null);
        Mockito.verify(editor).putString("requests", "");
    }

    public void testExecutePendingRequestsExecutesPutRequestAndClearsRequests() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(context.getSharedPreferences("request_cache", Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.getString("requests", "")).thenReturn(PUT_LIST);
        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.putString("requests", "")).thenReturn(editor);
        Mockito.doNothing().when(editor).apply();

        final RequestCache.Default requestCache = Mockito.spy(new RequestCache.Default(context));
        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);

        Mockito.doReturn(offlineStore).when(requestCache).getOfflineStore(context, COLLECTION);
        Mockito.doNothing().when(offlineStore).put(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.eq(VALUE), Mockito.any(DataStore.Listener.class));

        requestCache.executePending(null);

        Mockito.verify(offlineStore).put(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.eq(VALUE), Mockito.any(DataStore.Listener.class));
        Mockito.verify(editor).putString("requests", "");
    }

    public void testExecutePendingRequestsExecutesDeleteRequestAndClearsRequests() {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(context.getSharedPreferences("request_cache", Context.MODE_PRIVATE)).thenReturn(preferences);
        Mockito.when(preferences.getString("requests", "")).thenReturn(DELETE_LIST);
        Mockito.when(preferences.edit()).thenReturn(editor);
        Mockito.when(editor.putString("requests", "")).thenReturn(editor);
        Mockito.doNothing().when(editor).apply();

        final RequestCache.Default requestCache = Mockito.spy(new RequestCache.Default(context));
        final OfflineStore offlineStore = Mockito.mock(OfflineStore.class);

        Mockito.doReturn(offlineStore).when(requestCache).getOfflineStore(context, COLLECTION);
        Mockito.doNothing().when(offlineStore).delete(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.any(DataStore.Listener.class));

        requestCache.executePending(null);

        Mockito.verify(offlineStore).delete(Mockito.eq(TOKEN), Mockito.eq(KEY), Mockito.any(DataStore.Listener.class));
        Mockito.verify(editor).putString("requests", "");
    }
}