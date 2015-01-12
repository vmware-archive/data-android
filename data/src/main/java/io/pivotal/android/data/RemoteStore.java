/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.os.AsyncTask;

public class RemoteStore<T> implements DataStore<T> {

    private final RemoteClient<T> mClient;
    private final ObserverHandler<T> mHandler;

    public RemoteStore(final Context context) {
        this(new ObserverHandler<T>(), new RemoteClient.Default<T>(new EtagStore(context)));
    }

    public RemoteStore(final ObserverHandler<T> handler, final RemoteClient<T> client) {
        mHandler = handler;
        mClient = client;
    }

    @Override
    public Response<T> get(final Request<T> request) {
        final Response<T> response = getResponse(request);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response<T> getResponse(final Request<T> request) {
        try {
            return mClient.get(request);

        } catch (final Exception e) {
            Logger.ex(e);
            return new Response<T>(request.object, new DataError(e));
        }
    }

    @Override
    public void get(final Request<T> request, final Listener<T> listener) {
        new AsyncTask<Void, Void, Response<T>>() {

            @Override
            protected Response<T> doInBackground(final Void... params) {
                return RemoteStore.this.get(request);
            }

            @Override
            protected void onPostExecute(final Response<T> resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public Response<T> put(final Request<T> request) {
        final Response<T> response = putResponse(request);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response<T> putResponse(final Request<T> request) {
        try {
            return mClient.put(request);

        } catch (final Exception e) {
            Logger.ex(e);
            return new Response<T>(request.object, new DataError(e));
        }
    }

    @Override
    public void put(final Request<T> request, final Listener<T> listener) {
        new AsyncTask<Void, Void, Response<T>>() {

            @Override
            protected Response<T> doInBackground(final Void... params) {
                return RemoteStore.this.put(request);
            }

            @Override
            protected void onPostExecute(final Response<T> resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public Response<T> delete(final Request<T> request) {
        final Response<T> response = deleteResponse(request);
        mHandler.notifyResponse(response);
        return response;
    }

    private Response<T> deleteResponse(final Request<T> request) {
        try {
            return mClient.delete(request);

        } catch (final Exception e) {
            Logger.ex(e);
            return new Response<T>(request.object, new DataError(e));
        }
    }

    @Override
    public void delete(final Request<T> request, final Listener<T> listener) {
        new AsyncTask<Void, Void, Response<T>>() {

            @Override
            protected Response<T> doInBackground(final Void... params) {
                return RemoteStore.this.delete(request);
            }

            @Override
            protected void onPostExecute(final Response<T> resp) {
                if (listener != null) {
                    listener.onResponse(resp);
                }
            }
        }.execute();
    }

    @Override
    public boolean addObserver(final Observer<T> observer) {
        return mHandler.addObserver(observer);
    }

    @Override
    public boolean removeObserver(final Observer<T> observer) {
        return mHandler.removeObserver(observer);
    }
}
