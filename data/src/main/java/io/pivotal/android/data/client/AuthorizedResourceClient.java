/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data.client;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import io.pivotal.android.data.data.DataException;

public interface AuthorizedResourceClient {

    public interface Listener {
        public void onSuccess(int httpStatusCode, String contentType, String contentEncoding, InputStream result);
        public void onUnauthorized();
        public void onFailure(String reason);
    }

    public void executeDataServicesRequest(String method,
                                           String className,
                                           String objectId,
                                           Map<String, Object> headers,
                                           String contentType,
                                           String contentEncoding,
                                           byte[] contentData,
                                           Listener listener) throws AuthorizationException, DataException;

    public void executeHttpRequest(String method,
                                   URL url,
                                   Map<String, Object> headers,
                                   String contentType,
                                   String contentEncoding,
                                   byte[] contentData,
                                   Listener listener) throws AuthorizationException;
}
