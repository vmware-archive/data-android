/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class KeyValueLocalStore extends LocalStore<KeyValue> {

    private static final String DATA_PREFIX = "PCFData:Data";

    public KeyValueLocalStore(final Context context) {
        this(new ObserverHandler<KeyValue>(), new DataPersistence(context, DATA_PREFIX));
    }

    public KeyValueLocalStore(final ObserverHandler<KeyValue> handler, final DataPersistence persistence) {
        super(handler, persistence);
    }

    @Override
    public Response<KeyValue> execute(final Request<KeyValue> request) {
        final Response<KeyValue> response = executeRequest(request);
        getHandler().notifyResponse(response);
        return response;
    }

    private Response<KeyValue> executeRequest(final Request<KeyValue> request) {
        try {

            final KeyValue responseObject = new KeyValue(request.object);
            responseObject.value = executeRequestForMethod(request);

            return new Response<KeyValue>(responseObject);

        } catch (final Exception e) {
            Logger.ex(e);
            return new Response<KeyValue>(request.object, new DataError(e));
        }
    }

    private String executeRequestForMethod(final Request<KeyValue> request) throws Exception {

        final String identifier = getIdentifier(request.object);

        switch (request.method) {
            case Request.Methods.GET:
                Logger.d("Get: " + request.object);
                return getPersistence().getString(identifier);

            case Request.Methods.PUT:
                Logger.d("Put: " + request.object);
                return getPersistence().putString(identifier, request.object.value);

            case Request.Methods.DELETE:
                Logger.d("Delete: " + request.object);
                return getPersistence().deleteString(identifier);

            default:
                throw new UnsupportedOperationException();
        }
    }

    private static String getIdentifier(final KeyValue object) {
        return object.collection + ":" + object.key;
    }

    // TODO make observers listen to shared prefs?
    protected static class ObserverProxy implements OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

        }
    }
}
