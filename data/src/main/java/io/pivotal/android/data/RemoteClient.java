/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.text.TextUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;

public interface RemoteClient {

    public String get(final String accessToken, final String url) throws Exception;

    public String delete(final String accessToken, final String url) throws Exception;

    public String put(final String accessToken, final String url, final String value) throws Exception;


    public static class Default implements RemoteClient {

        public static final class Timeouts {
            public static final int CONNECTION = 4000;
            public static final int SOCKET = 10000;
        }

        public static final class Headers {
            public static final String AUTHORIZATION = "Authorization";
            public static final String IF_MATCH = "If-Match";
            public static final String IF_NONE_MATCH = "If-None-Match";
            public static final String ETAG = "Etag";
        }

        private final EtagStore mEtagStore;

        public Default(final EtagStore store) {
            mEtagStore = store;
        }

        @Override
        public String get(final String accessToken, final String url) throws Exception {
            final HttpGet request = new HttpGet(url);
            addAuthHeader(request, accessToken);
            addEtagHeader(request, url, Headers.IF_NONE_MATCH);

            return execute(request);
        }

        @Override
        public String delete(final String accessToken, final String url) throws Exception {
            final HttpDelete request = new HttpDelete(url);
            addAuthHeader(request, accessToken);
            addEtagHeader(request, url, Headers.IF_MATCH);

            return execute(request);
        }

        @Override
        public String put(final String accessToken, final String url, final String value) throws Exception {
            final HttpPut request = new HttpPut(url);
            request.setEntity(new ByteArrayEntity(value.getBytes()));
            addAuthHeader(request, accessToken);
            addEtagHeader(request, url, Headers.IF_MATCH);

            final String result = execute(request);
            return TextUtils.isEmpty(result) ? value : result;
        }


        public String execute(final HttpUriRequest request) throws Exception {
            final String url = request.getURI().toString();

            Logger.v("Request Url: " + url);

            final HttpClient client = getHttpClient();
            final HttpResponse response = client.execute(request);

            return handleResponse(response, url);
        }


        // ========================================================



        protected void addAuthHeader(final HttpUriRequest request, final String accessToken) {
            if (accessToken != null) {
                Logger.v("Request Header - " + Headers.AUTHORIZATION + ": Bearer " + accessToken);
                request.addHeader(Headers.AUTHORIZATION, "Bearer " + accessToken);
            } else {
                Logger.e("Request Header - No access token found.");
            }
        }

        protected void addEtagHeader(final HttpUriRequest request, final String url, final String header) {
            if (Pivotal.areEtagsEnabled()) {
                final String etag = mEtagStore.get(url);
                if (etag != null) {
                    Logger.v("Request Header - " + header + ": " + etag);
                    request.addHeader(header, etag);
                } else {
                    Logger.e("Request Header - No etag found.");
                }
            }
        }


        // ========================================================



        protected HttpClient getHttpClient() {
            final HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, Timeouts.CONNECTION);
            HttpConnectionParams.setSoTimeout(params, Timeouts.SOCKET);

            return new DefaultHttpClient(params);
        }

        protected String handleResponse(final HttpResponse response, final String url) throws Exception {
            final StatusLine statusLine = response.getStatusLine();

            Logger.v("Response Status: " + statusLine);

            checkStatusLine(statusLine);
            checkEtagHeader(response, url);

            return getResponseBody(response);
        }

        protected void checkStatusLine(final StatusLine statusLine) throws Exception {
            final int statusCode = statusLine.getStatusCode();
            final String reasonPhrase = statusLine.getReasonPhrase();

            if (statusCode < 200 || statusCode > 299) {
                throw new DataHttpException(statusCode, reasonPhrase);
            }
        }

        protected void checkEtagHeader(final HttpResponse response, final String url) {
            if (Pivotal.areEtagsEnabled()) {
                final Header header = response.getFirstHeader(Headers.ETAG);
                final String etag = header != null ? header.getValue() : "";

                Logger.v("Response Header - " + Headers.ETAG + ": " + etag + ", url: " + url);

                mEtagStore.put(url, etag);
            }
        }

        protected String getResponseBody(final HttpResponse response) throws IOException {
            final InputStream inputStream = response.getEntity().getContent();
            final String result = StreamUtils.consumeAndClose(inputStream);

            Logger.v("Response Body: " + result);

            return result;
        }
    }
}
