/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.InputStream;

/* package */ class RemoteClient {

    private final AuthStore mAuthStore;

    public RemoteClient(final AuthStore authStore) {
        mAuthStore = authStore;
    }

    public String get(final String accessToken, final String url) throws Exception {
        final HttpGet request = new HttpGet(url);
        return execute(accessToken, request);
    }

    public String put(final String accessToken, final String url, final String value) throws Exception {
        final byte[] bytes = value.getBytes();
        final HttpPut request = new HttpPut(url);
        request.setEntity(new ByteArrayEntity(bytes));
        final String result = execute(accessToken, request);
        return result == null || result.isEmpty() ? value : result;
    }

    public String delete(final String accessToken, final String url) throws Exception {
        final HttpDelete request = new HttpDelete(url);
        return execute(accessToken, request);
    }

    protected String execute(final String accessToken, final HttpUriRequest request) throws Exception {
        Logger.d("Request Url: " + request.getURI());

        if (accessToken != null) {
            Logger.d("Request Header - Authorization: Bearer " + accessToken);
            request.addHeader("Authorization", "Bearer " + accessToken);
        } else {
            Logger.e("Request Header - No access token found.");
        }

        final HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 3000);
        HttpConnectionParams.setSoTimeout(params, 5000);

        final DefaultHttpClient client = new DefaultHttpClient(params);
        final HttpResponse response = client.execute(request);
        final StatusLine statusLine = response.getStatusLine();
        final int statusCode = statusLine.getStatusCode();

        Logger.d("Response Status: " + statusLine);

        if (statusCode < 200 || statusCode > 299) {
            final String message = statusLine.getReasonPhrase();
            throw new DataException(statusCode, message);
        }

        final InputStream inputStream = response.getEntity().getContent();
        final String result = StreamUtils.getStringAndClose(inputStream);

        Logger.d("Result: " + result);
        return result;
    }
}
