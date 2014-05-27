package com.pivotal.cf.mobile.datasdk.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.api.ApiProviderImpl;
import com.pivotal.cf.mobile.datasdk.client.AuthorizationEngine;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProviderImpl;

public abstract class BaseAuthorizationActivity extends Activity {

    private AuthorizationPreferencesProvider authorizationPreferencesProvider;
    private ApiProvider apiProvider;
    private AuthorizationEngine authorizationEngine;

    public abstract void onAuthorizationComplete();
    public abstract void onAuthorizationFailed(String reason);

    // TODO - do we need to onCreate and provide a null content view?

    @Override
    protected void onResume() {
        super.onResume();
        setupRequirements();
        if (intentHasCallbackUrl(getIntent())) {
            // TODO - check state field in intent.data URI
            setupAuthorizationEngine();
            try {
                reenterAuthorizationEngine(getIntent());
            } catch (Exception e) {
                Logger.ex("Could not provide access code to Authorization Engine", e);
                onAuthorizationFailed("Could not provide access code to Authorization Engine :" + e.getLocalizedMessage());
            }
        }

        // TODO - finish here?
//        finish();
    }

    private void setupRequirements() {
        if (authorizationPreferencesProvider == null) {
            // TODO - find a way to provide an alternate preferences provider in unit tests
            authorizationPreferencesProvider = new AuthorizationPreferencesProviderImpl(this);
        }
        if (apiProvider == null) {
            apiProvider = new ApiProviderImpl();
        }
    }

    private boolean intentHasCallbackUrl(Intent intent) {
        if (intent == null) {
            return false;
        }
        if (!intent.hasCategory(Intent.CATEGORY_BROWSABLE)) {
            return false;
        }
        if (intent.getData() == null) {
            return false;
        }
        return intent.getData().toString().startsWith(authorizationPreferencesProvider.getRedirectUrl().toString());
    }

    // TODO - find a way to get the state token in here
//    private boolean verifyCallbackState(Uri uri) {
//        return uri.getQueryParameter("state").equals(STATE_TOKEN);
//    }

    private void setupAuthorizationEngine() {
        if (authorizationEngine == null) {
            authorizationEngine = new AuthorizationEngine(this, apiProvider, authorizationPreferencesProvider);
        }
    }

    private void reenterAuthorizationEngine(Intent intent) throws Exception {
        final String authorizationCode = getAuthorizationCode(intent.getData());
        authorizationEngine.authorizationCodeReceived(this, authorizationCode);
    }

    private String getAuthorizationCode(Uri uri) {
        return uri.getQueryParameter("code");
    }
}
