package com.pivotal.cf.mobile.datasdk.client;

import com.pivotal.cf.mobile.datasdk.data.DataException;
import com.pivotal.cf.mobile.datasdk.util.StreamUtil;

import java.net.URL;
import java.util.Map;

public class FakeAuthorizedResourceClient implements AuthorizedResourceClient {

    private boolean isSuccessful;
    private int httpStatusCode;
    private String returnedContentType;
    private String returnedContentEncoding;
    private String returnedContentData;
    private byte[] requestContentData;

    public void setupSuccessfulRequestResults(String contentType, String contentEncoding, String contentData) {
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
    public void executeDataServicesRequest(String method,
                                           String className,
                                           String objectId,
                                           Map<String, Object> headers,
                                           String contentType,
                                           String contentEncoding,
                                           byte[] contentData,
                                           Listener listener) throws AuthorizationException, DataException {

        this.requestContentData = contentData;

        if (isSuccessful) {
            listener.onSuccess(httpStatusCode, returnedContentType, returnedContentEncoding, StreamUtil.getInputStream(returnedContentData));
        }
    }

    @Override
    public void executeHttpRequest(String method,
                                   URL url,
                                   Map<String, Object> headers,
                                   String contentType,
                                   String contentEncoding,
                                   byte[] contentData,
                                   Listener listener) throws AuthorizationException {

        this.requestContentData = contentData;

        if (isSuccessful) {
            listener.onSuccess(httpStatusCode, returnedContentType, returnedContentEncoding, StreamUtil.getInputStream(returnedContentData));
        }
    }

    public byte[] getRequestContentData() {
        return requestContentData;
    }

}
