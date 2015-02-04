/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.app.Activity;
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
import java.net.MalformedURLException;
import java.net.URL;

public interface RemoteClient<T> {

    public Response<T> get(final Request<T> request) throws Exception;

    public Response<T> put(final Request<T> request) throws Exception;

    public Response<T> delete(final Request<T> request) throws Exception;


    public static class Default<T> implements RemoteClient<T> {

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
        public Response<T> get(final Request<T> request) throws Exception {

            if (request.object instanceof KeyValue) {

                final KeyValue object = (KeyValue) request.object;
                final String url = getUrl(object);

                final HttpGet get = new HttpGet(url);

                addUserAgentHeader(get);
                addAuthHeader(get);

                if (!request.force) {
                    addEtagHeader(get, url, Headers.IF_NONE_MATCH);
                }

                final KeyValue responseObject = new KeyValue(object);
                responseObject.value = execute(get);

                return new Response<T>((T) responseObject);

            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public Response<T> put(final Request<T> request) throws Exception {

            if (request.object instanceof KeyValue) {

                final KeyValue object = (KeyValue) request.object;
                final String url = getUrl(object);

                final HttpPut put = new HttpPut(url);
                put.setEntity(new ByteArrayEntity(object.value.getBytes()));

                addUserAgentHeader(put);
                addAuthHeader(put);

                if (!request.force) {
                    addEtagHeader(put, url, Headers.IF_MATCH);
                }

                final String result = execute(put);

                final KeyValue responseObject = new KeyValue(object);
                responseObject.value = TextUtils.isEmpty(result) ? object.value : result;

                return new Response<T>((T) responseObject);

            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public Response<T> delete(final Request<T> request) throws Exception {

            if (request.object instanceof KeyValue) {

                final KeyValue object = (KeyValue) request.object;
                final String url = getUrl(object);

                final HttpDelete delete = new HttpDelete(url);

                addUserAgentHeader(delete);
                addAuthHeader(delete);

                if (!request.force) {
                    addEtagHeader(delete, url, Headers.IF_MATCH);
                }

                final KeyValue responseObject = new KeyValue(object);
                responseObject.value = execute(delete);

                return new Response<T>((T) responseObject);

            } else {
                throw new UnsupportedOperationException();
            }
        }

        protected String getUrl(final KeyValue keyValue) throws MalformedURLException {
            return new URL(Pivotal.getServiceUrl() + "/" + keyValue.collection + "/" + keyValue.key).toString();
        }


        public String execute(final HttpUriRequest request) throws Exception {
            final String url = request.getURI().toString();

            Logger.v("Request Url: " + url);

            final HttpClient client = getHttpClient();
            final HttpResponse response = client.execute(request);

            return handleResponse(response, url);
        }


        // ========================================================


        protected String getAccessToken() {
            final TokenProvider provider = TokenProviderFactory.obtainTokenProvider();
            if (provider == null) {
                return null;
            } else if (mContext instanceof Activity) {
                final Activity activity = (Activity) mContext;
                return provider.provideAccessTokenWithUserPrompt(activity);
            } else {
                return provider.provideAccessToken(mContext);
            }
        }

        protected void addAuthHeader(final HttpUriRequest request) {
            final String accessToken = getAccessToken();
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
