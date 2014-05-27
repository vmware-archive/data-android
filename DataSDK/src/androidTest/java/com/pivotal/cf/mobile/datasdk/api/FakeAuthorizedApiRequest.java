package com.pivotal.cf.mobile.datasdk.api;

import android.app.Activity;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class FakeAuthorizedApiRequest implements AuthorizedApiRequest {

    private final boolean shouldAuthorizationListenerBeSuccessful;
    private final boolean shouldAuthorizedApiRequestBeSuccessful;
    private boolean didCallObtainAuthorization;
    private boolean didCallGetAccessToken;
    private final String contentData;
    private final String contentType;
    private final int httpStatusCode;

    public FakeAuthorizedApiRequest(boolean shouldAuthorizationListenerBeSuccessful,
                                    boolean shouldAuthorizedApiRequestBeSuccessful,
                                    int httpStatus,
                                    String contentType,
                                    String contentData) {

        this.shouldAuthorizationListenerBeSuccessful = shouldAuthorizationListenerBeSuccessful;
        this.shouldAuthorizedApiRequestBeSuccessful = shouldAuthorizedApiRequestBeSuccessful;
        this.httpStatusCode = httpStatus;
        this.contentType = contentType;
        this.contentData = contentData;
    }

    public void obtainAuthorization(Activity activity, DataParameters parameters) {
        didCallObtainAuthorization = true;
    }

    @Override
    public void getAccessToken(String authorizationCode, AuthorizationListener listener) {
        didCallGetAccessToken = true;
        if (shouldAuthorizationListenerBeSuccessful) {
            listener.onSuccess();
        } else {
            listener.onFailure("Fake request failed fakely.");
        }
    }

    @Override
    public void get(URL url,
                    Map<String, String> headers,
                    AuthorizationPreferencesProvider authorizationPreferencesProvider,
                    HttpOperationListener listener) {

        if (shouldAuthorizedApiRequestBeSuccessful) {
            listener.onSuccess(httpStatusCode, contentType, getInputStream());
        } else {
            listener.onFailure("Fake request failed fakely.");
        }
    }

    private InputStream getInputStream() {
        return new ByteArrayInputStream(contentData.getBytes());
    }

    public boolean didCallObtainAuthorization() {
        return didCallObtainAuthorization;
    }

    public boolean didCallGetAccessToken() {
        return didCallGetAccessToken;
    }
}
