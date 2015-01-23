/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import java.util.Properties;
import java.util.UUID;

public class PivotalTest extends AndroidTestCase {

    private static final String KEY = UUID.randomUUID().toString();
    private static final String VALUE = UUID.randomUUID().toString();
    private static final String RESULT = UUID.randomUUID().toString();

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        Pivotal.setProperties(null);
    }

    public void testGetSucceeds() {
        final Properties properties = new Properties();
        properties.setProperty(KEY, VALUE);

        Pivotal.setProperties(properties);

        assertEquals(VALUE, Pivotal.get(KEY));
    }

    public void testGetFails() {
        final Properties properties = new Properties();

        Pivotal.setProperties(properties);

        try {
            assertEquals(VALUE, Pivotal.get(KEY));
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e);
        }
    }

    public void testGetServiceUrl() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.serviceUrl", RESULT);

        Pivotal.setProperties(properties);

        assertEquals(RESULT, Pivotal.getServiceUrl());
    }

    public void testAreEtagsEnabledWithCollisionStrategyOptimisticLocking() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.collisionStrategy", "OptimisticLocking");

        Pivotal.setProperties(properties);

        assertTrue(Pivotal.areEtagsEnabled());
    }

    public void testAreEtagsEnabledWithCollisionStrategyLastWriteWins() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.collisionStrategy", "LastWriteWins");

        Pivotal.setProperties(properties);

        assertFalse(Pivotal.areEtagsEnabled());
    }

    public void testAreEtagsEnabledWithCollisionStrategyRandom() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.collisionStrategy", RESULT);

        Pivotal.setProperties(properties);

        assertFalse(Pivotal.areEtagsEnabled());
    }

    public void testEtagsValueUnspecified() {
        assertFalse(Pivotal.areEtagsEnabled());
    }
}