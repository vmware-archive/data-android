/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.os.Message;
import android.test.AndroidTestCase;

import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

import java.util.Set;
import java.util.UUID;

public class ObserverHandlerTest extends AndroidTestCase {

    private static final DataError ERROR = new DataError(new Exception());
    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();

    private static final Object LOCK = new Object();

    public void testNotifyResponseSuccess() {
        final DataStore.Observer observer = Mockito.mock(DataStore.Observer.class);
        final Set<DataStore.Observer> observers = Sets.newSet(observer);
        final ObserverHandler handler = new ObserverHandler(observers, LOCK);
        final DataStore.Response response = DataStore.Response.success(KEY, VALUE);

        final Message message = handler.obtainMessage(1000, response);
        handler.handleMessage(message);

        Mockito.verify(observer).onChange(KEY, VALUE);
    }

    public void testNotifyResponsePending() {
        final DataStore.Observer observer = Mockito.mock(DataStore.Observer.class);
        final Set<DataStore.Observer> observers = Sets.newSet(observer);
        final ObserverHandler handler = new ObserverHandler(observers, LOCK);
        final DataStore.Response response = DataStore.Response.pending(KEY, VALUE);

        final Message message = handler.obtainMessage(1000, response);
        handler.handleMessage(message);

        Mockito.verify(observer).onChange(KEY, VALUE);
    }

    public void testNotifyResponseFailure() {
        final DataStore.Observer observer = Mockito.mock(DataStore.Observer.class);
        final Set<DataStore.Observer> observers = Sets.newSet(observer);
        final ObserverHandler handler = new ObserverHandler(observers, LOCK);
        final DataStore.Response response = DataStore.Response.failure(KEY, ERROR);

        final Message message = handler.obtainMessage(1000, response);
        handler.handleMessage(message);

        Mockito.verify(observer).onError(KEY, ERROR);
    }
}