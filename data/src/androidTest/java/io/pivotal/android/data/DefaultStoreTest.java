/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public class DefaultStoreTest extends AndroidTestCase {

    public static final String TEST_TOKEN = "TOKEN";
    public static final String TEST_BASE_URL = "http://www.test.com";
    public static final String TEST_COLLECTION = "objects";
    public static final String TEST_KEY = "key";
    public static final String TEST_VALUE = "value";

    public void testGetWithSyncStore() {
        final DefaultStore dataStore = new TestDefaultStore();
        final DataObject object = new DataObject(dataStore, TEST_KEY);
        object.addObserver(new DataObject.Observer() {
            @Override
            public void onChange(final String key, final String value) {
                fail();
            }

            @Override
            public void onError(final String key, final Error error) {
                fail();
            }
        });

        assertEquals(TEST_VALUE, object.get(TEST_TOKEN));
    }

    public void testPutWithSyncStore() {
        final AssertionLatch latch = new AssertionLatch(1);
        final DefaultStore dataStore = new TestDefaultStore();
        final DataObject object = new DataObject(dataStore, TEST_KEY);
        object.addObserver(new DataObject.Observer() {
            @Override
            public void onChange(final String key, final String value) {
                latch.countDown();
                assertEquals("value1", value);
            }

            @Override
            public void onError(final String key, final Error error) {
                fail();
            }
        });

        object.put(TEST_TOKEN, "value1");
        latch.assertComplete();
    }

    private final class TestDefaultStore extends DefaultStore {

        public TestDefaultStore() {
            super(new TestLocalStore(), new TestRemoteStore());
        }
    }

    private final class TestLocalStore extends LocalStore {

        public TestLocalStore() {
            super(mContext, TEST_COLLECTION);
        }

        @Override
        /* package */ ObserverHandler createObserverHandler(final Set<Observer> observers, final Object lock) {
            return new FakeObserverHandler(observers, lock);
        }
    }

    private final MockContext mContext = new MockContext() {
        @Override
        public SharedPreferences getSharedPreferences(final String name, final int mode) {
            return new FakePreferences(TEST_KEY, TEST_VALUE);
        }
    };

    private final class TestRemoteStore extends RemoteStore {

        public TestRemoteStore() {
            super(mContext, TEST_COLLECTION);
        }

        @Override
        /* package */ ObserverHandler createObserverHandler(final Set<Observer> observers, final Object lock) {
            return new FakeObserverHandler(observers, lock);
        }

        @Override
        /* package */ RemoteClient createRemoteClient(final Context context) {
            return new FakeRemoteClient(getUrl(), TEST_VALUE);
        }

        @Override
        public void putAsync(final String token, final String key, final String value, final Listener listener) {
            if (listener != null) {
                listener.onResponse(Response.success(key, value));
            }
        }

        private URI getUrl() {
            try {
                return new URI(TEST_BASE_URL + "/" + TEST_COLLECTION + "/" + TEST_KEY);
            } catch (URISyntaxException e) {
                return null;
            }
        }
    }
}
