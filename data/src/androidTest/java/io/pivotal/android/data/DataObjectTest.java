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

    public void testGetWithBackingDataStore() {
        final DataStore dataStore = new FakeDataStore("key", "value");
        final DataObject object = new DataObject(dataStore, "key");

        assertEquals("value", object.get(TOKEN));
    }

    public void testPutWithBackingDataStore() {
        final AssertionLatch latch = new AssertionLatch(1);
        final DataStore dataStore = new FakeDataStore("key", "value");
        final DataObject object = new DataObject(dataStore, "key");
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

        object.put(TOKEN, "value1");
        latch.assertComplete();
    }
}
