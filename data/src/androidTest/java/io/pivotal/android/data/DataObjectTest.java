/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

public class DataObjectTest extends AndroidTestCase {

    private static final String TOKEN = "TOKEN";

    public void testInstantiation() {
        final DataStore dataStore = new FakeDataStore(null, null);
        final DataObject object = new DataObject(dataStore, "key");

        assertNotNull(object);
    }

    public void testGetReturnsValueFromDataStore() {
        final DataStore dataStore = new FakeDataStore("key", "value");
        final DataObject object = new DataObject(dataStore, "key");

        assertEquals("value", object.get(null));
    }

    public void testGetInvokesDataStore() {
        final AssertionLatch latch = new AssertionLatch(1);
        final DataStore dataStore = new MockDataStore() {

            @Override
            public Response get(final String token, final String key) {
                latch.countDown();
                return Response.success(null, null);
            }
        };
        final DataObject object = new DataObject(dataStore, null);

        object.get(null);

        latch.assertComplete();
    }

    public void testPutInvokesDataStore() {
        final AssertionLatch latch = new AssertionLatch(1);
        final DataStore dataStore = new MockDataStore() {

            @Override
            public Response put(final String token, final String key, final String value) {
                latch.countDown();
                return Response.success(null, null);
            }
        };
        final DataObject object = new DataObject(dataStore, null);

        object.put(null, null);

        latch.assertComplete();
    }

    public void testDeleteInvokesDataStore() {
        final AssertionLatch latch = new AssertionLatch(1);
        final DataStore dataStore = new MockDataStore() {

            @Override
            public Response delete(final String token, final String key) {
                latch.countDown();
                return Response.success(null, null);
            }
        };
        final DataObject object = new DataObject(dataStore, null);

        object.delete(null);

        latch.assertComplete();
    }

    public void testAddObservers() {
        final TestObserver observer1 = new TestObserver();
        final TestObserver observer2 = new TestObserver();

        final FakeDataStore dataStore = new FakeDataStore();
        final DataObject object = new DataObject(dataStore, null);

        assertTrue(object.addObserver(observer1));
        assertTrue(object.addObserver(observer2));

        assertFalse(object.addObserver(observer1));
        assertFalse(object.addObserver(observer2));
    }

    public void testAddObserversInvokesDataStoreIfObserverNotRegistered() {
        final AssertionLatch latch = new AssertionLatch(1);
        final TestObserver observer = new TestObserver();

        final DataStore dataStore = new MockDataStore() {

            @Override
            public boolean addObserver(final Observer observer) {
                latch.countDown();
                return true;
            }
        };

        final DataObject object = new DataObject(dataStore, null);

        // This should countdown the latch a single time
        object.addObserver(observer);

        latch.assertComplete();
    }

    public void testAddObserversDoesNotInvokesDataStoreIfObserverRegistered() {
        final AssertionLatch latch = new AssertionLatch(1);
        final TestObserver observer = new TestObserver();

        final DataStore dataStore = new MockDataStore() {

            @Override
            public boolean addObserver(final Observer observer) {
                latch.countDown();
                return true;
            }
        };

        final DataObject object = new DataObject(dataStore, null);

        // This should countdown the latch a single time
        object.addObserver(observer);
        object.addObserver(observer);

        latch.assertComplete();
    }


    public void testRemoveObservers() {
        final TestObserver observer1 = new TestObserver();
        final TestObserver observer2 = new TestObserver();

        final FakeDataStore dataStore = new FakeDataStore();
        final DataObject object = new DataObject(dataStore, null);

        assertTrue(object.addObserver(observer1));
        assertTrue(object.addObserver(observer2));

        assertTrue(object.removeObserver(observer1));
        assertTrue(object.removeObserver(observer2));

        assertFalse(object.removeObserver(observer1));
        assertFalse(object.removeObserver(observer2));
    }

    public void testRemoveObserversInvokesDataStoreIfObserverRegistered() {
        final AssertionLatch latch = new AssertionLatch(1);
        final TestObserver observer = new TestObserver();

        final DataStore dataStore = new MockDataStore() {

            @Override
            public boolean addObserver(Observer observer) {
                return true;
            }

            @Override
            public boolean removeObserver(final Observer observer) {
                latch.countDown();
                return true;
            }
        };

        final DataObject object = new DataObject(dataStore, null);

        // This should countdown the latch a single time
        object.addObserver(observer);
        object.removeObserver(observer);

        latch.assertComplete();
    }

    public void testRemoveObserversDoesNotInvokesDataStoreIfObserverNotRegistered() {
        final AssertionLatch latch = new AssertionLatch(0);
        final TestObserver observer = new TestObserver();

        final DataStore dataStore = new MockDataStore() {

            @Override
            public boolean removeObserver(final Observer observer) {
                latch.countDown();
                return true;
            }
        };

        final DataObject object = new DataObject(dataStore, null);

        // This should not countdown the latch
        object.removeObserver(observer);

        latch.assertComplete();
    }


    // ==================================================


    private static class TestObserver implements DataObject.Observer {

        @Override
        public void onChange(final String key, final String value) {

        }

        @Override
        public void onError(final String key, final DataError error) {

        }
    }
}
