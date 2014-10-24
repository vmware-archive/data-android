/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

public interface RequestCache {

    public void addGetRequest(final String token, final String collection, final String key);

    public void addPutRequest(final String token, final String collection, final String key, final String value);

    public void addDeleteRequest(final String token, final String collection, final String key);

    public void executePendingRequests(final Context context);


    public static class Default implements RequestCache {

        private static final String REQUEST_CACHE = "request_cache";
        private static final String REQUESTS = "requests";
        private static final String EMPTY = "";

        private static final class Methods {
            public static final int GET = 0;
            public static final int PUT = 1;
            public static final int DELETE = 2;
        }

        private final SharedPreferences mPrefs;

        public Default(final Context context) {
            mPrefs = context.getSharedPreferences(REQUEST_CACHE, Context.MODE_PRIVATE);
        }


        public void addGetRequest(final String token, final String collection, final String key) {
            addPendingRequest(Methods.GET, token, collection, key, null);
        }

        public void addPutRequest(final String token, final String collection, final String key, final String value) {
            addPendingRequest(Methods.PUT, token, collection, key, value);
        }

        public void addDeleteRequest(final String token, final String collection, final String key) {
            addPendingRequest(Methods.DELETE, token, collection, key, null);
        }

        protected void addPendingRequest(final int method, final String token, final String collection, final String key, final String value) {
            final PendingRequest.List requests = getRequests();
            requests.add(new PendingRequest(method, token, collection, key, value));
            putRequests(requests);
        }


        // =============================================


        private PendingRequest.List getRequests() {
            try {
                final byte[] bytes = mPrefs.getString(REQUESTS, EMPTY).getBytes();
                final ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(bytes, PendingRequest.List.class);
            } catch (final Exception e) {
                return new PendingRequest.List();
            }
        }

        @SuppressLint("CommitPrefEdits")
        private void putRequests(final PendingRequest.List requests) {
            try {
                final ObjectMapper mapper = new ObjectMapper();
                final String data = mapper.writeValueAsString(requests);
                mPrefs.edit().putString(REQUESTS, data).commit();
            } catch (final Exception e) {
                // do nothing
            }
        }

        @SuppressLint("CommitPrefEdits")
        private void clearRequests() {
            mPrefs.edit().putString(REQUESTS, EMPTY).commit();
        }


        // =============================================


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

        protected OfflineStore createOfflineStore(final Context context, final String collection) {
            return OfflineStore.create(context, collection);
        }

        private void executeGetRequest(final Context context, final PendingRequest request) {
            final OfflineStore store = createOfflineStore(context, request.collection);
            store.get(request.token, request.key);
        }

        private void executePutRequest(final Context context, final PendingRequest request) {
            final OfflineStore store = createOfflineStore(context, request.collection);
            store.put(request.token, request.key, request.value);
        }

        private void executeDeleteRequest(final Context context, final PendingRequest request) {
            final OfflineStore store = createOfflineStore(context, request.collection);
            store.delete(request.token, request.key);
        }

        private static final class PendingRequest {

            public int method;
            public String token, collection, key, value;

            public PendingRequest() {}

            public PendingRequest(final int method, final String token, final String collection, final String key, final String value) {
                this.method = method; this.token = token; this.collection = collection; this.key = key; this.value = value;
            }

            public static final class List extends ArrayList<PendingRequest> {
                public static final long serialVersionUID = 0L;
            }
        }
    }
}