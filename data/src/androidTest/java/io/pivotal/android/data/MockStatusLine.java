/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

public class MockStatusLine implements StatusLine {

    @Override
    public ProtocolVersion getProtocolVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatusCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getReasonPhrase() {
        throw new UnsupportedOperationException();
    }
}
