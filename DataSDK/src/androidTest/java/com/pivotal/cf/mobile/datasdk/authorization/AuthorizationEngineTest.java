package com.pivotal.cf.mobile.datasdk.authorization;

import android.app.Activity;
import android.content.Context;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

public class AuthorizationEngineTest extends AbstractAuthorizedResourceClientTest<AuthorizationEngine> {

    @Override
    protected AuthorizationEngine construct(Context context, AuthorizationPreferencesProvider preferencesProvider) {
        return new AuthorizationEngine(context, preferencesProvider);
    }

    private AuthorizationEngine getEngine() {
        return new AuthorizationEngine(getContext(), preferences);
    }

    private void baseTestRequires(Activity activity, DataParameters parameters) throws Exception {
        try {
            getEngine().obtainAuthorization(activity, parameters);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresActivity() throws Exception {
        baseTestRequires(null, parameters);
    }

    public void testRequiresParameters() throws Exception {
        baseTestRequires(activity, null);
    }

    public void testRequiresClientId() throws Exception {
        baseTestRequires(activity, new DataParameters(null, CLIENT_SECRET, AUTHORIZATION_URL, TOKEN_URL, REDIRECT_URL));
    }

    public void testRequiresClientSecret() throws Exception {
        baseTestRequires(activity, new DataParameters(CLIENT_ID, null, AUTHORIZATION_URL, TOKEN_URL, REDIRECT_URL));
    }

    public void testRequiresAuthorizationUrl() throws Exception {
        baseTestRequires(activity, new DataParameters(CLIENT_ID, CLIENT_SECRET, null, TOKEN_URL, REDIRECT_URL));
    }

    public void testRequiresTokenUrl() throws Exception {
        baseTestRequires(activity, new DataParameters(CLIENT_ID, CLIENT_SECRET, AUTHORIZATION_URL, null, REDIRECT_URL));
    }

    public void testRequiresRedirectUrl() throws Exception {
        baseTestRequires(activity, new DataParameters(CLIENT_ID, CLIENT_SECRET, AUTHORIZATION_URL, TOKEN_URL, null));
    }

}
