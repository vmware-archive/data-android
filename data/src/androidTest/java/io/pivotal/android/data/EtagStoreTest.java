/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import java.util.UUID;

public class EtagStoreTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();

    public void testGetInvokesSharedPreferences() {
        final AssertionLatch latch = new AssertionLatch(1);
        final Context context = new FakeContext(new MockSharedPreferences() {

            @Override
            public String getString(final String key, final String defValue) {
                latch.countDown();
                assertEquals(KEY, key);
                assertNull(defValue);
                return VALUE;
            }
        });

        final EtagStore store = new EtagStore.Default(context);

        assertEquals(VALUE, store.get(KEY));

        latch.assertComplete();
    }

    public void testPutInvokesSharedPreferences() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);
        final AssertionLatch latch3 = new AssertionLatch(1);

        final Context context = new FakeContext(new MockSharedPreferences() {

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

        final EtagStore store = new EtagStore.Default(context);

        store.put(KEY, VALUE);

        latch1.assertComplete();
        latch2.assertComplete();
        latch3.assertComplete();
    }


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
}