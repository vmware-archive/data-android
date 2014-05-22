package com.pivotal.cf.mobile.datasdk.client;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.authorization.AbstractAuthorizationClient;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class AuthorizedResourceClient extends AbstractAuthorizationClient {

    public interface Listener {
        public void onSuccess(int httpStatusCode, String contentType, InputStream result);
        public void onFailure(String reason);
    }

    public AuthorizedResourceClient(Context context, AuthorizationPreferencesProvider authorizationPreferencesProvider) {
        super(context, authorizationPreferencesProvider);
    }

    public void get(final URL url, final Map<String, String> headers, DataParameters parameters, final Listener listener) {
        verifyGetArguments(url, parameters, listener);

        // TODO - user a thread pool to process request

        final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                Logger.fd("Making GET call to '%s'.", url);

                // TODO - add headers

                final AuthorizationCodeFlow flow = getFlow(); // TODO - handle null flow
                final Credential credentials = loadCredentials(flow);
                final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credentials);
                final GenericUrl requestUrl = new GenericUrl(url);

                try {
                    final HttpRequest request = requestFactory.buildGetRequest(requestUrl);
                    final HttpResponse response = request.execute();
                    if (listener != null) {
                        final int statusCode = response.getStatusCode();
                        final String contentType = response.getContentType();
                        final InputStream inputStream = response.getContent();
                        listener.onSuccess(statusCode, contentType, inputStream);
                    }
                } catch (com.google.api.client.http.HttpResponseException e) {

                    // TODO - Check for an HttpResponseException indicating that the token has expired.
                    Logger.ex("Could not get user info", e);
                    if (listener != null) {
                        listener.onFailure(e.getLocalizedMessage());
                    }

                } catch (IOException e) {

                    // Some other error occurred?
                    Logger.ex("Could not get user info", e);

                    if (listener != null) {
                        listener.onFailure(e.getLocalizedMessage());
                    }
                }

                return null;
            }
        };
        task.execute();
    }

    private void verifyGetArguments(URL url, DataParameters parameters, Listener listener) {
        if (url == null) {
            throw new IllegalArgumentException("url may not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }


}
