/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.os.Message;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Set;

public class ObserverHandlerTest extends AndroidTestCase {

    public void testPostResponseSuccess() {
        final AssertionLatch latch = new AssertionLatch(1);
        final DataStore.Observer observer = new DataStore.Observer() {
            @Override
            public void onChange(final String key, final String value) {
                latch.countDown();
            }

            @Override
            public void onError(final String key, final DataError error) {
                fail();
            }
        };
        final ObserverHandler handler = new TestObserverHandler(observer);
        handler.postResponse(DataStore.Response.success(null, null));

        latch.assertComplete();
    }

    public void testPostResponsePending() {
        final AssertionLatch latch = new AssertionLatch(1);
        final DataStore.Observer observer = new DataStore.Observer() {
            @Override
            public void onChange(final String key, final String value) {
                latch.countDown();
            }

            @Override
            public void onError(final String key, final DataError error) {
                fail();
            }
        };
        final ObserverHandler handler = new TestObserverHandler(observer);
        handler.postResponse(DataStore.Response.pending(null, null));

        latch.assertComplete();
    }

    public void testPostResponseFailure() {
        final AssertionLatch latch = new AssertionLatch(1);
        final DataStore.Observer observer = new DataStore.Observer() {
            @Override
            public void onChange(final String key, final String value) {
                fail();
            }

            @Override
            public void onError(final String key, final DataError error) {
                latch.countDown();
            }
        };
        final ObserverHandler handler = new TestObserverHandler(observer);
        handler.postResponse(DataStore.Response.failure(null, null));

        latch.assertComplete();
    }

    private static class TestObserverHandler extends ObserverHandler {

        private static final Object LOCK = new Object();

        public TestObserverHandler(final DataStore.Observer observer) {
            super(createSet(observer), LOCK);
        }

        private static Set<DataStore.Observer> createSet(final DataStore.Observer observer) {
            final Set<DataStore.Observer> set = new HashSet<DataStore.Observer>(1);
            set.add(observer);
            return set;
        }

        @Override
        public boolean sendMessageAtTime(final Message msg, final long uptimeMillis) {
            handleMessage(msg);
            return true;
        }
    }
}