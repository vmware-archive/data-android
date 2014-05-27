package com.pivotal.cf.mobile.datasdk.client;

import android.content.Context;
import android.os.AsyncTask;

import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.authorization.AbstractAuthorizationClient;
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

    public void get(final URL url, final Map<String, String> headers, DataParameters parameters, final Listener listener) {
        verifyGetArguments(url, parameters, listener);

        // TODO - user a thread pool to process request

        final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                Logger.fd("Making GET call to '%s'.", url);

                // TODO - add headers

                final AuthorizedApiRequest request = apiProvider.getAuthorizedApiRequest(context, authorizationPreferencesProvider);

                request.get(url, headers, authorizationPreferencesProvider, new AuthorizedApiRequest.HttpOperationListener() {
                    @Override
                    public void onSuccess(int httpStatusCode, String contentType, InputStream result) {
                        if (isSuccessfulHttpStatusCode(httpStatusCode)) {
                            // TODO - watch for 401 errors and clear credentials.
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
