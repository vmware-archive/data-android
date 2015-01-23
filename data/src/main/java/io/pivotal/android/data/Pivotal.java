/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/* package */ class Pivotal {

    private static final class Strategies {
        private static final String OPTIMISTIC_LOCKING = "OptimisticLocking";
        private static final String LAST_WRITE_WINS = "LastWriteWins";
    }

    private static final class Keys {
        private static final String SERVICE_URL = "pivotal.data.serviceUrl";
        private static final String COLLISION_STRATEGY = "pivotal.data.collisionStrategy";
    }

    private static final String[] LOCATIONS = {
            "assets/pivotal.properties", "res/raw/pivotal.properties"
    };

    private static Properties sProperties;

    /* package */ static Properties getProperties() {
        if (sProperties == null) {
            sProperties = loadProperties();
        }
        return sProperties;
    }

    /* package */ static void setProperties(final Properties properties) {
        sProperties = properties;
    }

    private static Properties loadProperties() {
        for (final String path : LOCATIONS) {
            try {
                return loadProperties(path);
            } catch (final Exception e) {
                Logger.ex(e);
            }
        }
        throw new IllegalStateException("Could not find pivotal.properties file.");
    }

    private static Properties loadProperties(final String path) throws IOException {
        final Properties properties = new Properties();
        properties.load(getInputStream(path));
        return properties;
    }

    private static InputStream getInputStream(final String path) {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader loader = currentThread.getContextClassLoader();
        return loader.getResourceAsStream(path);
    }

    public static String get(final String key) {
        final String value = getProperties().getProperty(key);
        if (TextUtils.isEmpty(value)) {
            throw new IllegalStateException("'" + key + "' not found in pivotal.properties");
        }
        return value;
    }

    public static String getServiceUrl() {
        return get(Keys.SERVICE_URL);
    }

    public static boolean areEtagsEnabled() {
        try {
            final String collisionStrategy = get(Keys.COLLISION_STRATEGY);
            return collisionStrategy != null && collisionStrategy.equals(Strategies.OPTIMISTIC_LOCKING);
        } catch (final IllegalStateException e) {
            return false;
        }
    }
}
