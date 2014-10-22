/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.os.AsyncTask;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class RemoteStore implements DataStore {

    public static interface Listener {
        public void onResponse(Response response);
    }

    private final Object mLock = new Object();
    private final Set<Observer> mObservers = new HashSet<Observer>();

    private final String mCollection;
    private final ObserverHandler mHandler;
    private final RemoteClient mClient;

    public RemoteStore(final Context context, final String collection) {
        mHandler = createObserverHandler(mObservers, mLock);
        mClient = createRemoteClient(context);
        mCollection = collection;
    }

    protected ObserverHandler createObserverHandler(final Set<Observer> observers, final Object lock) {
        return new ObserverHandler(observers, lock);
    }

    protected RemoteClient createRemoteClient(final Context context) {
        return new RemoteClient.Default(new EtagStore.Default(context));
    }

    protected String getCollectionUrl(final String key) throws MalformedURLException {
        return new URL(Pivotal.getServiceUrl() + "/" + mCollection + "/" + key).toString();
    }

    @Override
    public boolean contains(final String accessToken, final String key) {
        return get(accessToken, key).status == Response.Status.SUCCESS;
    }

    @Override
    public Response get(final String accessToken, final String key) {
        final Response response = getResponse(accessToken, key);
        mHandler.postResponse(response);
        return response;
    }

    private Response getResponse(final String accessToken, final String key) {
        try {
            final String url = getCollectionUrl(key);
            final String result = mClient.get(accessToken, url);
            return Response.success(key, result);
        } catch (final Exception e) {
            Logger.ex(e);
            return Response.failure(key, new DataError(e));
        }
    }

    public void getAsync(final String accessToken, final String key, final Listener listener) {
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
        mHandler.postResponse(response);
        return response;
    }

    private Response putResponse(final String accessToken, final String key, final String value) {
        try {
            final String url = getCollectionUrl(key);
            final String result = mClient.put(accessToken, url, value);
            return Response.success(key, result);
        } catch (final Exception e) {
            Logger.ex(e);
            return Response.failure(key, new DataError(e));
        }
    }

    public void putAsync(final String accessToken, final String key, final String value, final Listener listener) {
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
        mHandler.postResponse(response);
        return response;
    }

    private Response deleteResponse(final String accessToken, final String key) {
        try {
            final String url = getCollectionUrl(key);
            final String result = mClient.delete(accessToken, url);
            return Response.success(key, result);
        } catch (final Exception e) {
            Logger.ex(e);
            return Response.failure(key, new DataError(e));
        }
    }

    public void deleteAsync(final String accessToken, final String key, final Listener listener) {
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
        synchronized (mLock) {
            return mObservers.add(observer);
        }
    }

    @Override
    public boolean removeObserver(final Observer observer) {
        synchronized (mLock) {
            return mObservers.remove(observer);
        }
    }

    protected Set<Observer> getObservers() {
        synchronized (mLock) {
            return mObservers;
        }
    }
}
