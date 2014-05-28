package com.pivotal.cf.mobile.datasdk.client;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.auth.oauth2.Credential;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.api.AuthorizedApiRequest;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class AuthorizedResourceClient extends AbstractAuthorizationClient {

    public interface Listener {
        public void onSuccess(int httpStatusCode, String contentType, InputStream result);
        public void onFailure(String reason);
    }

    public AuthorizedResourceClient(Context context,
                                    ApiProvider apiProvider,
                                    AuthorizationPreferencesProvider authorizationPreferencesProvider) {

        super(context, apiProvider, authorizationPreferencesProvider);
    }

    // TODO provide documents - including which exceptions can get thrown

    public void get(final URL url,
                    final Map<String, Object> headers,
                    DataParameters parameters,
                    final Listener listener) throws Exception {

        verifyGetArguments(url, parameters, listener);

        if (!areAuthorizationPreferencesAvailable()) {
            throw new AuthorizationException("Authorization parameters have not been set. You must authorize with DataSDK.obtainAuthorization first.");
        }

        final AuthorizedApiRequest request = apiProvider.getAuthorizedApiRequest(context, authorizationPreferencesProvider);
        final Credential credential = request.loadCredential();
        if (credential == null) {
            throw new AuthorizationException("Authorization credentials are not available. You must authorize with DataSDK.obtainAuthorization first.");
        }
        // TODO - user a thread pool to process request

        final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                Logger.fd("Making GET call to '%s'.", url);

                // TODO - add headers

                try {
                    request.get(url, headers, credential, authorizationPreferencesProvider, new AuthorizedApiRequest.HttpOperationListener() {
                        @Override
                        public void onSuccess(int httpStatusCode, String contentType, InputStream result) {
                            // TODO - watch for 401 errors and clear credentials.
                            if (isSuccessfulHttpStatusCode(httpStatusCode)) {
                                listener.onSuccess(httpStatusCode, contentType, result);
                            } else {
                                listener.onFailure("Received failure HTTP status code: '" + httpStatusCode + "'");
                            }
                        }

                        @Override
                        public void onFailure(String reason) {
                            listener.onFailure(reason);
                        }
                    });

                } catch (Exception e) {
                    Logger.ex("Could not perform authorized GET request", e);
                    listener.onFailure("Could not perform authorized GET request: " + e.getLocalizedMessage());
                }

                return null;
            }
        };
        task.execute();
    }

    private boolean isSuccessfulHttpStatusCode(int httpStatusCode) {
        return httpStatusCode >= 200 && httpStatusCode < 300;
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
