/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

import java.net.MalformedURLException;
import java.net.URL;

public class KeyValueRemoteStore extends RemoteStore<KeyValue> {

    public KeyValueRemoteStore(final Context context) {
        this(new ObserverHandler<KeyValue>(), new RemoteClient.Default(context));
    }

    public KeyValueRemoteStore(final ObserverHandler<KeyValue> handler, final RemoteClient client) {
        super(handler, client);
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

        final String url = getUrl(request.object);

        switch (request.method) {
            case Request.Methods.GET:
                Logger.d("Get: " + request.object);
                return getClient().get(url, request.force);

            case Request.Methods.PUT:
                Logger.d("Put: " + request.object);
                final byte[] entity = getEntity(request.object);
                return getClient().put(url, entity, request.force);

            case Request.Methods.DELETE:
                Logger.d("Delete: " + request.object);
                return getClient().delete(url, request.force);

            default:
                throw new UnsupportedOperationException();
        }
    }

    protected String getUrl(final KeyValue keyValue) throws MalformedURLException {
        return new URL(Pivotal.getServiceUrl() + "/" + keyValue.collection + "/" + keyValue.key).toString();
    }

    protected byte[] getEntity(final KeyValue keyValue) {
        return keyValue.value != null ? keyValue.value.getBytes() : null;
    }
}
