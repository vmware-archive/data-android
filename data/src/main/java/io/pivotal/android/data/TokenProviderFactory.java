/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

/* package */ class TokenProviderFactory {

    private static TokenProvider sProvider;

    public static void registerTokenProvider(final TokenProvider provider) {
        sProvider = provider;
    }

    public static TokenProvider obtainTokenProvider() {
        return sProvider;
    }
}
