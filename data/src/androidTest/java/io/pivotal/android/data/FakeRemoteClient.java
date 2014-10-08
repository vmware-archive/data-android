/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class FakeRemoteClient extends RemoteClient.Default {

    private final Map<URI, String> mValues = new HashMap<URI, String>();

    public FakeRemoteClient(final URI uri, final String value) {
        super(null);
        mValues.put(uri, value);
    }

    @Override
    public String execute(final HttpUriRequest request) throws Exception {
        if (request instanceof HttpPut) {
            mValues.put(request.getURI(), ((HttpPut) request).getEntity().toString());
            return "";
        } else {
            return mValues.get(request.getURI());
        }
    }
}
