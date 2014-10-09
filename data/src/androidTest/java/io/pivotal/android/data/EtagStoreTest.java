/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

public class EtagStoreTest extends AndroidTestCase {

    public void testGetInvokesSharedPreferences() {
        final AssertionLatch latch = new AssertionLatch(1);
        final SharedPreferences prefs = new MockSharedPreferences() {

            @Override
            public String getString(final String key, final String defValue) {
                latch.countDown();
                return null;
            }
        };
        final Context context = new TestContext(prefs);
        final EtagStore store = new EtagStore.Default(context);

        store.get(null);

        latch.assertComplete();
    }

    public void testPutInvokesSharedPreferences() {
        final AssertionLatch latch1 = new AssertionLatch(1);
        final AssertionLatch latch2 = new AssertionLatch(1);
        final AssertionLatch latch3 = new AssertionLatch(1);

        final SharedPreferences prefs = new MockSharedPreferences() {

            @Override
            public Editor edit() {
                latch1.countDown();
                return this;
            }

            @Override
            public Editor putString(final String key, final String value) {
                latch2.countDown();
                return this;
            }

            @Override
            public boolean commit() {
                latch3.countDown();
                return true;
            }
        };

        final Context context = new TestContext(prefs);
        final EtagStore store = new EtagStore.Default(context);

        store.put(null, null);

        latch1.assertComplete();
        latch2.assertComplete();
        latch3.assertComplete();
    }


    private static class TestContext extends MockContext {

        private final SharedPreferences mSharedPreferences;

        public TestContext(final SharedPreferences prefs) {
            mSharedPreferences = prefs;
        }

        @Override
        public SharedPreferences getSharedPreferences(final String name, final int mode) {
            return mSharedPreferences;
        }
    }
}