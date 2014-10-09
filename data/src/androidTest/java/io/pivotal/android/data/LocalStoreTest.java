/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import java.util.Set;

public class LocalStoreTest extends AndroidTestCase {

    public static final String TEST_TOKEN = "TOKEN";
    public static final String TEST_COLLECTION = "objects";
    public static final String TEST_KEY = "key";
    public static final String TEST_VALUE = "value";

    public void testGetFromLocalStore() {
        final LocalStore dataStore = new TestLocalStore(TEST_COLLECTION);
        final DataObject object = new DataObject(dataStore, TEST_KEY);
        object.addObserver(new DataObject.Observer() {
            @Override
            public void onChange(final String key, final String value) {
                fail();
            }

            @Override
            public void onError(final String key, final DataError error) {
                fail();
            }
        });

        assertEquals(TEST_VALUE, object.get(TEST_TOKEN));
    }

    public void testPutToLocalStore() {
        final AssertionLatch latch = new AssertionLatch(1);
        final LocalStore dataStore = new TestLocalStore(TEST_COLLECTION);
        final DataObject object = new DataObject(dataStore, TEST_KEY);
        object.addObserver(new DataObject.Observer() {
            @Override
            public void onChange(final String key, final String value) {
                latch.countDown();
                assertEquals("value1", value);
            }

            @Override
            public void onError(final String key, final DataError error) {
                fail();
            }
        });

        object.put(TEST_TOKEN, "value1");
        latch.assertComplete();
    }

    private final class TestLocalStore extends LocalStore {

        public TestLocalStore(final String objects) {
            super(mContext, objects);
        }

        @Override
        protected ObserverHandler createObserverHandler(final Set<Observer> observers, final Object lock) {
            return new FakeObserverHandler(observers, lock);
        }
    }

    private final MockContext mContext = new MockContext() {
        @Override
        public SharedPreferences getSharedPreferences(final String name, final int mode) {
            return new FakeSharedPreferences(TEST_KEY, TEST_VALUE);
        }
    };
}
