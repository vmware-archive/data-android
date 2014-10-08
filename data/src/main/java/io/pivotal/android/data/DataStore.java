/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

public interface DataStore {

    public boolean contains(final String accessToken, final String key);

    public Response get(final String accessToken, final String key);

    public Response put(final String accessToken, final String key, final String value);

    public Response delete(final String accessToken, final String key);

    public boolean addObserver(final Observer observer);

    public boolean removeObserver(final Observer observer);

    public static class Response {
        public final Status status;
        public final String key, value;
        public final DataError error;

        private Response(final Status status, final String key, final String value) {
            this.status = status;
            this.key = key;
            this.value = value;
            this.error = null;
        }

        private Response(final Status status, final String key, final DataError error) {
            this.status = status;
            this.key = key;
            this.value = null;
            this.error = error;
        }

        public static Response failure(final String key, final DataError error) {
            return new Response(Status.FAILURE, key, error);
        }

        public static Response success(final String key, final String value) {
            return new Response(Status.SUCCESS, key, value);
        }

        public static Response pending(final String key, final String value) {
            return new Response(Status.PENDING, key, value);
        }

        public static enum Status {
            FAILURE, SUCCESS, PENDING
        }
    }

    public static interface Observer {
        public void onChange(String key, String value);

        public void onError(String key, DataError error);
    }
}
