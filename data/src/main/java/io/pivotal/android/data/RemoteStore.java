/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.os.AsyncTask;

import java.net.MalformedURLException;
import java.net.URL;

public class RemoteStore implements DataStore {

    private final String mCollection;
    private final RemoteClient mClient;
    private final ObserverHandler mHandler;

    public RemoteStore(final Context context, final String collection) {
        this(collection, new ObserverHandler(), new RemoteClient.Default(new EtagStore.Default(context)));
    }

    RemoteStore(final String collection, final ObserverHandler handler, final RemoteClient client) {
        mCollection = collection;
        mHandler = handler;
        mClient = client;
    }

    protected String getCollectionUrl(final String key) throws MalformedURLException {
        return new URL(Pivotal.getServiceUrl() + "/" + mCollection + "/" + key).toString();
    }

    @Override
    public boolean contains(final String accessToken, final String key) {
        final Response response = get(accessToken, key);
        return response.isSuccess();
    }

    @Override
    public Response get(final String accessToken, final String key) {
        final Response response = getResponse(accessToken, key);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response getResponse(final String accessToken, final String key) {
        try {
            final String url = getCollectionUrl(key);
            final String result = mClient.get(accessToken, url);
            return new Response(key, result);
        } catch (final Exception e) {
            Logger.ex(e);
            return new Response(key, new DataError(e));
        }
    }

    @Override
    public void get(final String accessToken, final String key, final Listener listener) {
        new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(final Void... params) {
                return RemoteStore.this.get(accessToken, key);
            }

            @Override
            protected void onPostExecute(final Response resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public Response put(final String accessToken, final String key, final String value) {
        final Response response = putResponse(accessToken, key, value);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response putResponse(final String accessToken, final String key, final String value) {
        try {
            final String url = getCollectionUrl(key);
            final String result = mClient.put(accessToken, url, value);
            return new Response(key, result);
        } catch (final Exception e) {
            Logger.ex(e);
            return new Response(key, new DataError(e));
        }
    }

    @Override
    public void put(final String accessToken, final String key, final String value, final Listener listener) {
        new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(final Void... params) {
                return RemoteStore.this.put(accessToken, key, value);
            }

            @Override
            protected void onPostExecute(final Response resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public Response delete(final String accessToken, final String key) {
        final Response response = deleteResponse(accessToken, key);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response deleteResponse(final String accessToken, final String key) {
        try {
            final String url = getCollectionUrl(key);
            final String result = mClient.delete(accessToken, url);
            return new Response(key, result);
        } catch (final Exception e) {
            Logger.ex(e);
            return new Response(key, new DataError(e));
        }
    }

    @Override
    public void delete(final String accessToken, final String key, final Listener listener) {
        new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(final Void... params) {
                return RemoteStore.this.delete(accessToken, key);
            }

            @Override
            protected void onPostExecute(final Response resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public boolean addObserver(final Observer observer) {
        return mHandler.addObserver(observer);
    }

    @Override
    public boolean removeObserver(final Observer observer) {
        return mHandler.removeObserver(observer);
    }
}
