package com.pivotal.cf.mobile.datasdk.authorization;

import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.activity.FakeAuthorizationActivity;
import com.pivotal.cf.mobile.datasdk.prefs.FakeAuthorizationPreferences;

import java.net.URL;

public class AuthorizationEngineTest extends AndroidTestCase {

    private static final String CLIENT_SECRET = "TEST_CLIENT_SECRET";
    private static final String CLIENT_ID = "TEST_CLIENT_ID";
    private static final String REDIRECT_URL = "https://test.redirect.url";
    private static final String AUTHORIZATION_URL = "https://test.authorization.url";
    private static final String TOKEN_URL = "https://test.token.url";

    private FakeAuthorizationPreferences preferences;
    private FakeAuthorizationActivity activity;
    private DataParameters parameters;

    @Override
    protected void setUp() throws Exception {
        activity = new FakeAuthorizationActivity();
        preferences = new FakeAuthorizationPreferences();
        parameters = new DataParameters(
                CLIENT_ID,
                CLIENT_SECRET,
                new URL(AUTHORIZATION_URL),
                new URL(TOKEN_URL),
                new URL(REDIRECT_URL));
    }

    public void testRequiresContext() {
        try {
            new AuthorizationEngine(null, preferences);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresAuthorizationPreferencesProvider() {
        try {
            new AuthorizationEngine(getContext(), null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresActivity() {
        try {
            final AuthorizationEngine engine = new AuthorizationEngine(getContext(), preferences);
            engine.obtainAuthorization(null, parameters);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresParameters() {
        try {
            final AuthorizationEngine engine = new AuthorizationEngine(getContext(), preferences);
            engine.obtainAuthorization(activity, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresClientId() throws Exception {
        try {
            final AuthorizationEngine engine = new AuthorizationEngine(getContext(), preferences);
            engine.obtainAuthorization(activity, new DataParameters(
                    null,
                    CLIENT_SECRET,
                    new URL(AUTHORIZATION_URL),
                    new URL(TOKEN_URL),
                    new URL(REDIRECT_URL)));
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresClientSecret() throws Exception {
        try {
            final AuthorizationEngine engine = new AuthorizationEngine(getContext(), preferences);
            engine.obtainAuthorization(activity, new DataParameters(
                    CLIENT_ID,
                    null,
                    new URL(AUTHORIZATION_URL),
                    new URL(TOKEN_URL),
                    new URL(REDIRECT_URL)));
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresAuthorizationUrl() throws Exception {
        try {
            final AuthorizationEngine engine = new AuthorizationEngine(getContext(), preferences);
            engine.obtainAuthorization(activity, new DataParameters(
                    CLIENT_ID,
                    CLIENT_SECRET,
                    null,
                    new URL(TOKEN_URL),
                    new URL(REDIRECT_URL)));
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresTokenUrl() throws Exception {
        try {
            final AuthorizationEngine engine = new AuthorizationEngine(getContext(), preferences);
            engine.obtainAuthorization(activity, new DataParameters(
                    CLIENT_ID,
                    CLIENT_SECRET,
                    new URL(AUTHORIZATION_URL),
                    null,
                    new URL(REDIRECT_URL)));
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresRedirectUrl() throws Exception {
        try {
            final AuthorizationEngine engine = new AuthorizationEngine(getContext(), preferences);
            engine.obtainAuthorization(activity, new DataParameters(
                    CLIENT_ID,
                    CLIENT_SECRET,
                    new URL(AUTHORIZATION_URL),
                    new URL(TOKEN_URL),
                    null));
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

}
