package com.pivotal.cf.mobile.datasdk.authorization;

import android.content.Context;
import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.activity.FakeAuthorizationActivity;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;
import com.pivotal.cf.mobile.datasdk.prefs.FakeAuthorizationPreferences;

import java.net.URL;

public abstract class AbstractAuthorizedResourceClientTest<T extends AbstractAuthorizationClient> extends AndroidTestCase {

    protected static final String CLIENT_SECRET = "TEST_CLIENT_SECRET";
    protected static final String CLIENT_ID = "TEST_CLIENT_ID";
    protected static URL REDIRECT_URL;
    protected static URL AUTHORIZATION_URL;
    protected static URL TOKEN_URL;

    protected FakeAuthorizationPreferences preferences;
    protected FakeAuthorizationActivity activity;
    protected DataParameters parameters;

    protected abstract T construct(Context context, AuthorizationPreferencesProvider preferencesProvider);

    @Override
    protected void setUp() throws Exception {
        activity = new FakeAuthorizationActivity();
        preferences = new FakeAuthorizationPreferences();
        REDIRECT_URL = new URL("https://test.redirect.url");
        AUTHORIZATION_URL = new URL("https://test.authorization.url");
        TOKEN_URL = new URL("https://test.token.url");
        parameters = new DataParameters(CLIENT_ID, CLIENT_SECRET, AUTHORIZATION_URL, TOKEN_URL, REDIRECT_URL);
    }

    public void testRequiresContext() {
        try {
            construct(null, preferences);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresAuthorizationPreferencesProvider() {
        try {
            construct(getContext(), null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }
}
