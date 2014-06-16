package com.pivotal.cf.mobile.datasdk.client;

import com.pivotal.cf.mobile.datasdk.data.DataException;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public interface AuthorizedResourceClient {

    public interface Listener {
        public void onSuccess(int httpStatusCode, String contentType, String contentEncoding, InputStream result);
        public void onUnauthorized();
        public void onFailure(String reason);
    }

    public void get(final URL url,
                    final Map<String, Object> headers,
                    final Listener listener) throws AuthorizationException;
}
