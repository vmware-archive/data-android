package com.pivotal.cf.mobile.datasdk.client;

import com.pivotal.cf.mobile.datasdk.data.DataException;
import com.pivotal.cf.mobile.datasdk.util.StreamUtil;

import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

public class FakeAuthorizedResourceClient implements AuthorizedResourceClient {

    private boolean isSuccessful;
    private int httpStatusCode;
    private String returnedContentType;
    private String returnedContentEncoding;
    private String returnedContentData;

    public void setupSuccessfulGetResults(String contentType, String contentEncoding, String contentData) {
        this.httpStatusCode = 200;
        this.isSuccessful = true;
        this.returnedContentType = contentType;
        this.returnedContentEncoding = contentEncoding;
        this.returnedContentData = contentData;
    }

    public void setupFailedHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        this.isSuccessful = true;
        this.returnedContentType = "application/json";
        this.returnedContentEncoding = "utf-8";
        this.returnedContentData = "{}";
    }

    @Override
    public void executeDataServicesRequest(final String method,
                                           final String className,
                                           final String objectId,
                                           final Map<String, Object> headers,
                                           String contentType,
                                           String contentEncoding,
                                           final OutputStream contentData,
                                           final Listener listener) throws AuthorizationException, DataException {
        if (isSuccessful) {
            listener.onSuccess(httpStatusCode, returnedContentType, returnedContentEncoding, StreamUtil.getInputStream(returnedContentData));
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

        if (isSuccessful) {
            listener.onSuccess(httpStatusCode, returnedContentType, returnedContentEncoding, StreamUtil.getInputStream(returnedContentData));
        }
    }

}
