/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

public interface DataStore {

    public boolean contains(final String accessToken, final String key);

    public Response get(final String accessToken, final String key);

    public void get(final String accessToken, final String key, final Listener listener);

    public Response put(final String accessToken, final String key, final String value);

    public void put(final String accessToken, final String key, final String value, final Listener listener);

    public Response delete(final String accessToken, final String key);

    public void delete(final String accessToken, final String key, final Listener listener);

    public boolean addObserver(final Observer observer);

    public boolean removeObserver(final Observer observer);


    public static interface Observer {
        public void onChange(String key, String value);

        public void onError(String key, DataError error);
    }


    public static interface Listener {
        public void onResponse(Response response);
    }


    public static class Response {

        public final String key, value;
        public final DataError error;

        public Response(final String key, final String value) {
            this.key = key;
            this.value = value;
            this.error = null;
        }

        public Response(final String key, final DataError error) {
            this.key = key;
            this.value = null;
            this.error = error;
        }

        public boolean isSuccess() {
            return this.error == null;
        }

        public boolean isFailure() {
            return this.error != null;
        }

        public boolean isNotModified() {
            return this.error != null && this.error.isNotModified();
        }
    }
}
