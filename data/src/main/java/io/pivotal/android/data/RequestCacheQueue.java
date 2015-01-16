/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestCacheQueue<T> {

    private static final String REQUEST_KEY = "PCFData:Requests";

    private final Object LOCK = new Object();

    private final DataPersistence mPersistence;

    public RequestCacheQueue(final DataPersistence persistence) {
        mPersistence = persistence;
    }

    public void add(final RequestCache.QueuedRequest<T> request) {
        final RequestCache.QueuedRequest.List<T> requests;

        synchronized (LOCK) {
            requests = getRequests();
            requests.add(request);
            putRequests(requests);
        }
    }

    public RequestCache.QueuedRequest.List<T> empty() {
        final RequestCache.QueuedRequest.List<T> requests;

        synchronized (LOCK) {
            requests = getRequests();
            deleteRequests();
        }

        return requests;
    }

    @SuppressWarnings("unchecked")
    protected RequestCache.QueuedRequest.List<T> getRequests() {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerSubtypes(KeyValue.class);
            final String serialized = mPersistence.getString(REQUEST_KEY);
            return mapper.readValue(serialized, RequestCache.QueuedRequest.List.class);
        } catch (final Exception e) {
            return new RequestCache.QueuedRequest.List<T>();
        }
    }

    protected void putRequests(final RequestCache.QueuedRequest.List<T> requests) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String data = mapper.writeValueAsString(requests);
            mPersistence.putString(REQUEST_KEY, data);
        } catch (final Exception e) {
            // do nothing
        }
    }

    protected void deleteRequests() {
        mPersistence.deleteString(REQUEST_KEY);
    }
}
