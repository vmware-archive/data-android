/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

/* package */ class RequestCache {

    private static final class Methods {
        public static final int GET = 0;
        public static final int PUT = 1;
        public static final int DELETE = 2;
    }

    private static final String REQUESTS = "requests";

    private final ObjectMapper mMapper = new ObjectMapper();
    private final SharedPreferences mPrefs;


    public RequestCache(final Context context) {
        mPrefs = context.getSharedPreferences(REQUESTS, Context.MODE_PRIVATE);
    }

    public PendingRequest.List getRequests() {
        try {
            final byte[] bytes = mPrefs.getString(REQUESTS, "").getBytes();
            return mMapper.readValue(bytes, PendingRequest.List.class);
        } catch (final Exception e) {
            return new PendingRequest.List();
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void putRequests(final PendingRequest.List requests) {
        try {
            final String data = mMapper.writeValueAsString(requests);
            mPrefs.edit().putString(REQUESTS, data).commit();
        } catch (final Exception e) {
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void clearRequests() {
        mPrefs.edit().putString(REQUESTS, "").commit();
    }

    public void storeGetRequest(final String token, final String collection, final String key) {
        storePendingRequest(Methods.GET, token, collection, key, null);
    }

    public void storePutRequest(final String token, final String collection, final String key, final String value) {
        storePendingRequest(Methods.PUT, token, collection, key,  value);
    }

    public void storeDeleteRequest(final String token, final String collection, final String key) {
        storePendingRequest(Methods.DELETE, token, collection, key, null);
    }

    private void storePendingRequest(final int method, final String token, final String collection, final String key, final String value) {
        final PendingRequest.List requests = getRequests();

        requests.add(new PendingRequest(method, token, collection, key, value));

        putRequests(requests);
    }

    public void executePendingRequests(final Context context) {
        final PendingRequest.List requests = getRequests();

        clearRequests();

        for (final PendingRequest request: requests) {
            handleRequest(context, request);
        }
    }

    private void handleRequest(final Context context, final PendingRequest request) {
        switch (request.method) {
            case Methods.GET:
                executeGetRequest(context, request);
                break;
            case Methods.PUT:
                executePutRequest(context, request);
                break;
            case Methods.DELETE:
                executeDeleteRequest(context, request);
                break;
        }
    }

    private void executeDeleteRequest(final Context context, final PendingRequest request) {
        final DefaultStore store = DefaultStore.create(context, request.collection);
        store.delete(request.token, request.key);
    }

    private void executePutRequest(final Context context, final PendingRequest request) {
        final DefaultStore store = DefaultStore.create(context, request.collection);
        store.put(request.token, request.key, request.value);
    }

    private void executeGetRequest(final Context context, final PendingRequest request) {
        final DefaultStore store = DefaultStore.create(context, request.collection);
        store.get(request.token, request.key);
    }

    private static final class PendingRequest {

        public int method;
        public String token, collection, key, value;

        public PendingRequest() {

        }

        public PendingRequest(final int method, final String token, final String collection, final String key, final String value) {
            this.method = method; this.token = token; this.collection = collection; this.key = key; this.value = value;
        }

        private static final class List extends ArrayList<PendingRequest> {
            public static final long serialVersionUID = 0L;
        }
    }
}