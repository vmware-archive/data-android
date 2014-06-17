package com.pivotal.cf.mobile.datasdk.client;

import com.pivotal.cf.mobile.datasdk.util.StreamUtil;

import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

public class FakeAuthorizedResourceClient implements AuthorizedResourceClient {

    private boolean isSuccessful;
    private int httpStatusCode;
    private String contentType;
    private String contentEncoding;
    private String contentData;

    public void setupSuccessfulGetResults(String contentType, String contentEncoding, String contentData) {
        this.httpStatusCode = 200;
        this.isSuccessful = true;
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
        this.contentData = contentData;
    }

    public void setupFailedHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        this.isSuccessful = true;
        this.contentType = "application/json";
        this.contentEncoding = "utf-8";
        this.contentData = "{}";
    }

    public void get(URL url, Map<String, Object> headers, Listener listener) throws AuthorizationException {
        if (isSuccessful) {
            listener.onSuccess(httpStatusCode, contentType, contentEncoding, StreamUtil.getInputStream(contentData));
        }
    }

    @Override
    public void executeHttpRequest(final String method,
                                   final URL url,
                                   final Map<String, Object> headers,
                                   String contentType,
                                   String contentEncoding,
                                   final OutputStream contentData,
                                   final Listener listener) throws AuthorizationException {


    }

}
