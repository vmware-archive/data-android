package com.pivotal.cf.mobile.datasdk.api;

import android.app.Activity;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public interface AuthorizedApiRequest {

    public interface AuthorizationListener {
        public void onSuccess();
        public void onFailure(String reason);
    }

    public interface HttpOperationListener {
        public void onSuccess(int httpStatusCode, String contentType, InputStream result);
        public void onFailure(String reason);
    }

    public void obtainAuthorization(Activity activity, DataParameters parameters);

    public void getAccessToken(String authorizationCode, AuthorizationListener listener);

    public void get(URL url,
                    Map<String, String> headers,
                    AuthorizationPreferencesProvider authorizationPreferencesProvider,
                    HttpOperationListener listener);

}
