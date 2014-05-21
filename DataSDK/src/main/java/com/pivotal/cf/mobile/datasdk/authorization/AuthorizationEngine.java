package com.pivotal.cf.mobile.datasdk.authorization;

import android.app.Activity;

import com.pivotal.cf.mobile.datasdk.DataParameters;

public class AuthorizationEngine {

    public AuthorizationEngine() {

    }

    public void obtainAuthorization(Activity activity, DataParameters parameters) {
        verifyAuthorizationParameters(activity, parameters);

    }

    private void verifyAuthorizationParameters(Activity activity, DataParameters parameters) {
        if (activity == null) {
            throw new IllegalArgumentException("activity may not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
    }
}
