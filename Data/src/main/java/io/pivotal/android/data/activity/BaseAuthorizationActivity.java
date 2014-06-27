package io.pivotal.android.data.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import io.pivotal.android.common.util.Logger;
import io.pivotal.android.data.api.ApiProvider;
import io.pivotal.android.data.api.ApiProviderImpl;
import io.pivotal.android.data.client.AuthorizationEngine;
import io.pivotal.android.data.prefs.AuthorizationPreferencesProvider;
import io.pivotal.android.data.prefs.AuthorizationPreferencesProviderImpl;

public abstract class BaseAuthorizationActivity extends Activity {

    private AuthorizationPreferencesProvider authorizationPreferencesProvider;
    private ApiProvider apiProvider;
    private AuthorizationEngine authorizationEngine;

    // NOTE: These callbacks might be called on background threads.
    public abstract void onAuthorizationComplete();
    public abstract void onAuthorizationDenied();
    public abstract void onAuthorizationFailed(String reason);

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
    }

    private void setupRequirements() {
        if (authorizationPreferencesProvider == null) {
            // TODO - find a way to provide an alternate preferences provider in unit tests
            authorizationPreferencesProvider = new AuthorizationPreferencesProviderImpl(this);
        }
        if (apiProvider == null) {
            apiProvider = new ApiProviderImpl(this);
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
        return intent.getData().toString().toLowerCase().startsWith(authorizationPreferencesProvider.getRedirectUrl().toString().toLowerCase());
    }

    // TODO - find a way to get the state token in here
//    private boolean verifyCallbackState(Uri uri) {
//        return uri.getQueryParameter("state").equals(STATE_TOKEN);
//    }

    private void setupAuthorizationEngine() {
        if (authorizationEngine == null) {
            authorizationEngine = new AuthorizationEngine(apiProvider, authorizationPreferencesProvider);
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
