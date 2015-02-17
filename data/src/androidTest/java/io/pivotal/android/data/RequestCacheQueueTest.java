/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.mockito.Mockito;

import java.util.UUID;

public class RequestCacheQueueTest extends AndroidTestCase {

    private static final String REQUEST_KEY = "PCFData:Requests";

    private static final String COLLECTION = UUID.randomUUID().toString();
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final int METHOD = (int)(Math.random() * 3);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    public void testGetRequests() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();

        final KeyValue keyValue = new KeyValue(COLLECTION, KEY, VALUE);
        final Request<KeyValue> request = new Request<KeyValue>(METHOD, keyValue, false);
        final PendingRequest<KeyValue> pendingRequest = new PendingRequest<KeyValue>(request);
        final PendingRequest.List<KeyValue> list = new PendingRequest.List<KeyValue>();
        list.add(pendingRequest);

        final DataPersistence persistence = Mockito.mock(DataPersistence.class);
        final String serialized = mapper.writeValueAsString(list);

        Mockito.when(persistence.getString(Mockito.anyString())).thenReturn(serialized);

        final RequestCacheQueue<KeyValue> queue = new RequestCacheQueue<KeyValue>(persistence);
        final PendingRequest.List<KeyValue> response = queue.getRequests();

        final PendingRequest<KeyValue> deserializedItem = response.get(0);

        assertEquals(METHOD, deserializedItem.method);
        assertEquals(keyValue.key, deserializedItem.object.key);
        assertEquals(keyValue.value, deserializedItem.object.value);
        assertEquals(keyValue.collection, deserializedItem.object.collection);

        Mockito.verify(persistence).getString(REQUEST_KEY);
    }

    public void testPutRequests() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();

        final KeyValue keyValue = new KeyValue(COLLECTION, KEY, VALUE);
        final Request<KeyValue> request = new Request<KeyValue>(METHOD, keyValue, false);
        final PendingRequest<KeyValue> pendingRequest = new PendingRequest<KeyValue>(request);
        final PendingRequest.List<KeyValue> list = new PendingRequest.List<KeyValue>();
        list.add(pendingRequest);

        final DataPersistence persistence = Mockito.mock(DataPersistence.class);
        final String serialized = mapper.writeValueAsString(list);

        final RequestCacheQueue<KeyValue> queue = new RequestCacheQueue<KeyValue>(persistence);
        queue.putRequests(list);

        Mockito.verify(persistence).putString(REQUEST_KEY, serialized);
    }

    @SuppressWarnings("unchecked")
    public void testAdd() {
        final Object object = new Object();
        final Request request = new Request(METHOD, object, false);
        final PendingRequest pendingRequest = new PendingRequest(request);
        final PendingRequest.List list = Mockito.mock(PendingRequest.List.class);

        final RequestCacheQueue queue = Mockito.spy(new RequestCacheQueue(null));

        Mockito.stub(queue.getRequests()).toReturn(list);
        Mockito.doNothing().when(queue).putRequests(Mockito.any(PendingRequest.List.class));

        queue.add(pendingRequest);

        Mockito.verify(queue).getRequests();
        Mockito.verify(list).add(pendingRequest);
        Mockito.verify(queue).putRequests(list);
    }

    public void testEmpty() {
        final PendingRequest.List list = Mockito.mock(PendingRequest.List.class);

        final RequestCacheQueue queue = Mockito.spy(new RequestCacheQueue(null));

        Mockito.stub(queue.getRequests()).toReturn(list);
        Mockito.doNothing().when(queue).deleteRequests();

        final PendingRequest.List result = queue.empty();

        assertEquals(list, result);

        Mockito.verify(queue).getRequests();
        Mockito.verify(queue).deleteRequests();
    }

    public void testDeleteRequests() {
        final DataPersistence persistence = Mockito.mock(DataPersistence.class);

        final RequestCacheQueue queue = new RequestCacheQueue(persistence);
        queue.deleteRequests();

        Mockito.verify(persistence).deleteString(REQUEST_KEY);
    }
}