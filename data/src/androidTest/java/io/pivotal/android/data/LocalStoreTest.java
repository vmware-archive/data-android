/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import java.util.UUID;

public class LocalStoreTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();

    private static final DataStore.Observer OBSERVER = new DataStore.Observer() {
        @Override
        public void onChange(final String key, final String value) {

        }

        @Override
        public void onError(final String key, final DataError error) {

        }
    };

    public void testContainsInvokesSharedPreferences() {
        final AssertionLatch latch = new AssertionLatch(1);
        final Context context = new FakeContext(new TestSharedPreferences() {

            @Override
            public boolean contains(final String key) {
                latch.countDown();
                assertEquals(KEY, key);
                return true;
            }
        });

        final LocalStore store = new LocalStore(context, null);

        assertTrue(store.contains(null, KEY));

        latch.assertComplete();
    }

    public void testGetInvokesSharedPreferences() {
        final AssertionLatch latch = new AssertionLatch(1);
        final Context context = new FakeContext(new TestSharedPreferences() {

            @Override
            public String getString(final String key, final String defValue) {
                latch.countDown();
                assertEquals(KEY, key);
                assertNull(defValue);
                return VALUE;
            }
        });

        final LocalStore store = new LocalStore(context, null);
        final DataStore.Response response = store.get(null, KEY);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        latch.assertComplete();
    }

    public void testPutInvokesSharedPreferences() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);
        final AssertionLatch latch3 = new AssertionLatch(1);

        final Context context = new FakeContext(new TestSharedPreferences() {

            @Override
            public Editor edit() {
                latch1.countDown();
                return this;
            }

            @Override
            public Editor putString(final String key, final String value) {
                latch2.countDown();
                assertEquals(KEY, key);
                assertEquals(VALUE, value);
                return this;
            }

            @Override
            public boolean commit() {
                latch3.countDown();
                return true;
            }
        });

        final LocalStore store = new LocalStore(context, null);
        final DataStore.Response response = store.put(null, KEY, VALUE);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals(KEY, response.key);
        assertEquals(VALUE, response.value);

        latch1.assertComplete();
        latch2.assertComplete();
        latch3.assertComplete();
    }

    public void testDeleteInvokesSharedPreferences() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);
        final AssertionLatch latch3 = new AssertionLatch(1);

        final Context context = new FakeContext(new TestSharedPreferences() {

            @Override
            public Editor edit() {
                latch1.countDown();
                return this;
            }

            @Override
            public Editor remove(final String key) {
                latch2.countDown();
                assertEquals(KEY, key);
                return this;
            }

            @Override
            public boolean commit() {
                latch3.countDown();
                return true;
            }
        });

        final LocalStore store = new LocalStore(context, null);
        final DataStore.Response response = store.delete(null, KEY);

        assertEquals(DataStore.Response.Status.SUCCESS, response.status);
        assertEquals(KEY, response.key);

        latch1.assertComplete();
        latch2.assertComplete();
        latch3.assertComplete();
    }

    public void testAddObserver() {
        final Context context = new FakeContext(new TestSharedPreferences());
        final LocalStore store = new LocalStore(context, null);

        assertTrue(store.addObserver(OBSERVER));
        assertFalse(store.addObserver(OBSERVER));
    }

    public void testRemoveObserver() {
        final Context context = new FakeContext(new TestSharedPreferences());
        final LocalStore store = new LocalStore(context, null);

        assertFalse(store.removeObserver(OBSERVER));
        assertTrue(store.addObserver(OBSERVER));
        assertTrue(store.removeObserver(OBSERVER));
        assertFalse(store.removeObserver(OBSERVER));
    }

    // ============================================


    private static class FakeContext extends MockContext {

        private final SharedPreferences mSharedPreferences;

        public FakeContext(final SharedPreferences prefs) {
            mSharedPreferences = prefs;
        }

        @Override
        public SharedPreferences getSharedPreferences(final String name, final int mode) {
            return mSharedPreferences;
        }
    }

    private static class TestSharedPreferences extends MockSharedPreferences {

        @Override
        public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
            // do nothing
        }
    }
}
