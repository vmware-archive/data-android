/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;
import android.os.Build;
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


    public String get(String url, boolean force) throws Exception;

    public String put(String url, byte[] entity, boolean force) throws Exception;

    public String delete(String url, boolean force) throws Exception;


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
            public static final String USER_AGENT = "User-Agent";
        }

        private final EtagStore mEtagStore;
        private final Context mContext;

        public Default(final Context context) {
            mEtagStore = new EtagStore(context);
            mContext = context;
        }

        public Default(final Context context, final EtagStore store) {
            mEtagStore = store;
            mContext = context;
        }

        @Override
        public String get(final String url, final boolean force) throws Exception {
            final HttpGet request = new HttpGet(url);
            return execute(request, force);
        }

        @Override
        public String put(final String url, final byte[] entity, final boolean force) throws Exception {
            final HttpPut request = new HttpPut(url);
            request.setEntity(new ByteArrayEntity(entity));

            final String result = execute(request, force);
            return TextUtils.isEmpty(result) ? new String(entity) : result;
        }

        @Override
        public String delete(final String url, final boolean force) throws Exception {
            final HttpDelete request = new HttpDelete(url);
            return execute(request, force);
        }

        protected String execute(final HttpUriRequest request, final boolean force) throws Exception {
            final String url = request.getURI().toString();
            final HttpClient httpClient = getHttpClient();

            addHeaders(request, force);

            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == 401) {
                Logger.v("Response 401 (invalidating token).");
                invalidateAccessToken();

                Logger.v("Response 401 (requesting new token).");
                addAuthHeader(request);

                Logger.v("Response 401 (retrying).");
                response = httpClient.execute(request);
            }

            return handleResponse(response, url);
        }

        protected void addHeaders(final HttpUriRequest request, final boolean force) throws Exception {
            final String url = request.getURI().toString();

            Logger.v("Request Url: " + url);

            addAuthHeader(request);

            addUserAgentHeader(request);

            if (!force) {
                addEtagHeader(request, url);
            } else {
                Logger.e("Request Header - No Etag. Request Forced.");
            }
        }


        // ========================================================


        protected String provideAccessToken() {
            final TokenProvider provider = TokenProviderFactory.obtainTokenProvider();
            if (provider != null) {
                return provider.provideAccessToken(mContext);
            } else {
                return null;
            }
        }

        protected void invalidateAccessToken() {
            final TokenProvider provider = TokenProviderFactory.obtainTokenProvider();
            if (provider != null) {
                provider.invalidateAccessToken(mContext);
            }
        }

        protected void addAuthHeader(final HttpUriRequest request) {
            final String accessToken = provideAccessToken();
            if (accessToken != null) {
                Logger.v("Request Header - " + Headers.AUTHORIZATION + ": Bearer " + accessToken);
                request.addHeader(Headers.AUTHORIZATION, "Bearer " + accessToken);
            } else {
                Logger.e("Request Header - No access token found.");
                throw new IllegalStateException("Could not retrieve access token.");
            }
        }

        protected void addEtagHeader(final HttpUriRequest request, final String url) {
            if (Pivotal.areEtagsEnabled()) {
                final String etag = mEtagStore.get(url);
                if (etag != null) {
                    if (request instanceof HttpGet) {
                        request.addHeader(Headers.IF_NONE_MATCH, etag);
                    } else {
                        request.addHeader(Headers.IF_MATCH, etag);
                    }
                    Logger.v("Request Header - Etag: " + etag);
                } else {
                    Logger.e("Request Header - No etag found.");
                }
            } else {
                Logger.e("Request Header - Etags Disabled.");
            }
        }

        protected void addUserAgentHeader(final HttpUriRequest request) {
            final String sdkVersion = String.format("PCFData/%s;", BuildConfig.SDK_VERSION);
            final String androidVersion = String.format("Android Version %s (Build %s)", Build.VERSION.RELEASE, Build.ID);
            request.addHeader(Headers.USER_AGENT, sdkVersion + " " + androidVersion);
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

            final int statusCode = statusLine.getStatusCode();
            final String reasonPhrase = statusLine.getReasonPhrase();

            if (statusCode < 200 || statusCode > 299) {
                if (statusCode == 404 && Pivotal.areEtagsEnabled()) {
                    mEtagStore.put(url, "");
                }

                throw new DataHttpException(statusCode, reasonPhrase);
            }

            if (Pivotal.areEtagsEnabled()) {
                final Header header = response.getFirstHeader(Headers.ETAG);
                final String etag = header != null ? header.getValue() : "";

                Logger.v("Response Header - " + Headers.ETAG + ": " + etag + ", url: " + url);

                mEtagStore.put(url, etag);
            }

            return getResponseBody(response);
        }

        protected String getResponseBody(final HttpResponse response) throws IOException {
            final InputStream inputStream = response.getEntity().getContent();
            final String result = StreamUtils.consumeAndClose(inputStream);

            Logger.v("Response Body: " + result);

            return result;
        }
    }
}
