/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

public interface RequestCache {

    public void queueGet(final String token, final String collection, final String key);

    public void queuePut(final String token, final String collection, final String key, final String value, final String fallback);

    public void queueDelete(final String token, final String collection, final String key, final String fallback);

    public void executePending(final String token);

    public void executePendingAsync(final String token);


    public static class Default implements RequestCache {

        private static final Object LOCK = new Object();

        private static final String EMPTY = "";
        private static final String REQUEST_CACHE = "PCFData:RequestCache";
        private static final String REQUEST_KEY = "PCFData:Requests";

        private static final class Methods {
            public static final int GET = 0;
            public static final int PUT = 1;
            public static final int DELETE = 2;
        }

        private final Context mContext;
        private final SharedPreferences mPrefs;

        public Default(final Context context) {
            mPrefs = context.getSharedPreferences(REQUEST_CACHE, Context.MODE_PRIVATE);
            mContext = context;
        }

        protected OfflineStore getOfflineStore(final Context context, final String collection) {
            return OfflineStore.create(context, collection);
        }

        protected LocalStore getLocalStore(final Context context, final String collection) {
            return new LocalStore(context, collection);
        }

        @Override
        public void queueGet(final String token, final String collection, final String key) {
            queuePending(new PendingRequest(Methods.GET, token, collection, key, null, null));
        }

        @Override
        public void queuePut(final String token, final String collection, final String key, final String value, final String fallback) {
            queuePending(new PendingRequest(Methods.PUT, token, collection, key, value, fallback));
        }

        @Override
        public void queueDelete(final String token, final String collection, final String key, final String fallback) {
            queuePending(new PendingRequest(Methods.DELETE, token, collection, key, null, fallback));
        }

        protected void queuePending(final PendingRequest request) {
            final PendingRequest.List requests;

            synchronized (LOCK) {
                requests = getRequests();
                requests.add(request);
                putRequests(requests);
            }
        }

        // =============================================


        private PendingRequest.List getRequests() {
            try {
                final byte[] bytes = mPrefs.getString(REQUEST_KEY, EMPTY).getBytes();
                final ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(bytes, PendingRequest.List.class);
            } catch (final Exception e) {
                return new PendingRequest.List();
            }
        }

        private void putRequests(final PendingRequest.List requests) {
            try {
                final ObjectMapper mapper = new ObjectMapper();
                final String data = mapper.writeValueAsString(requests);
                final SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString(REQUEST_KEY, data);
                editor.apply();
            } catch (final Exception e) {
                // do nothing
            }
        }

        private void clearRequests() {
            final SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(REQUEST_KEY, EMPTY);
            editor.apply();
        }


        // =============================================



        @Override
        public void executePending(final String token) {
            final PendingRequest.List requests;

            synchronized (LOCK) {
                requests = getRequests();
                clearRequests();
            }

            execute(requests, token);
        }

        public void executePendingAsync(final String token) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(final Void... params) {
                    executePending(token);
                    return null;
                }

            }.execute();
        }

        private void execute(final PendingRequest.List requests, final String token) {
            for (final PendingRequest request: requests) {
                execute(request, token);
            }
        }

        private void execute(final PendingRequest request, final String token) {
            final OfflineStore store = getOfflineStore(mContext, request.collection);
            final String accessToken = token != null ? token : request.token;

            switch (request.method) {
                case Methods.GET: {
                    store.get(accessToken, request.key);
                    break;
                }

                case Methods.PUT: {
                    final DataStore.Response response = store.put(accessToken, request.key, request.value);
                    if (response.error != null) {
                        final LocalStore localStore = getLocalStore(mContext, request.collection);
                        localStore.put(accessToken, request.key, request.fallback);
                    }
                    break;
                }

                case Methods.DELETE: {
                    final DataStore.Response response = store.delete(accessToken, request.key);
                    if (response.error != null) {
                        final LocalStore localStore = getLocalStore(mContext, request.collection);
                        localStore.put(accessToken, request.key, request.fallback);
                    }
                    break;
                }
            }
        }

        public static final class PendingRequest {

            public int method;
            public String token, collection, key, value, fallback;

            public PendingRequest() {}

            public PendingRequest(final int method, final String token, final String collection, final String key, final String value, final String fallback) {
                this.method = method; this.token = token; this.collection = collection; this.key = key; this.value = value; this.fallback = fallback;
            }

            public static final class List extends ArrayList<PendingRequest> {
                public static final long serialVersionUID = 0L;
            }
        }
    }
}