/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

public class MockRemoteClient implements RemoteClient {

    @Override
    public String get(final String accessToken, final String url) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String delete(final String accessToken, final String url) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String put(final String accessToken, final String url, final String value) throws Exception {
        throw new UnsupportedOperationException();
    }
}
