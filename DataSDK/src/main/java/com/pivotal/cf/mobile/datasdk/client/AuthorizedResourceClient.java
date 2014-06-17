package com.pivotal.cf.mobile.datasdk.client;

import com.pivotal.cf.mobile.datasdk.data.DataException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

public interface AuthorizedResourceClient {

    public interface Listener {
        public void onSuccess(int httpStatusCode, String contentType, String contentEncoding, InputStream result);
        public void onUnauthorized();
        public void onFailure(String reason);
    }

    public void executeHttpRequest(final String method,
                                   final URL url,
                                   final Map<String, Object> headers,
                                   String contentType,
                                   String contentEncoding,
                                   final OutputStream contentData,
                                   final Listener listener) throws AuthorizationException;
}
