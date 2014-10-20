/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.test.AndroidTestCase;

import java.util.Properties;

public class PivotalTest extends AndroidTestCase {


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        Pivotal.setProperties(null);
    }

    public void testGetSucceeds() {
        final String key = "key";
        final String value = "value";

        final Properties properties = new Properties();
        properties.setProperty(key, value);

        Pivotal.setProperties(properties);
        assertEquals(value, Pivotal.get(key));
    }

    public void testGetFails() {
        final String key = "key";
        final String value = "value";

        final Properties properties = new Properties();

        Pivotal.setProperties(properties);

        try {
            assertEquals(value, Pivotal.get(key));
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e);
        }
    }

    public void testGetClientId() {
        assertEquals("http://example.com", Pivotal.getServiceUrl());
    }

    public void testEtagsValueEnabled() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.etagSupport", "enabled");

        Pivotal.setProperties(properties);
        assertTrue(Pivotal.areEtagsEnabled());
    }

    public void testEtagsValueDisabled() {
        final Properties properties = new Properties();
        properties.setProperty("pivotal.data.etagSupport", "disabled");

        Pivotal.setProperties(properties);
        assertFalse(Pivotal.areEtagsEnabled());
    }

    public void testEtagsValueUnspecified() {
        assertFalse(Pivotal.areEtagsEnabled());
    }
}