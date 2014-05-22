package com.pivotal.cf.mobile.datasdk.client;

import com.pivotal.cf.mobile.datasdk.DataParameters;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public interface OAuth2Client {

    public interface Listener {
        public void onSuccess(int httpStatusCode, String contentType, InputStream result);
        public void onFailure(String reason);
    }

    public void get(URL url, Map<String, String> headers, DataParameters parameters, Listener listener);
}
