package com.pivotal.cf.mobile.datasdk.authorization;

import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.activity.FakeActivity;

import java.net.URL;

public class AuthorizationEngineTest extends AndroidTestCase {

    private static final String CLIENT_SECRET = "TEST_CLIENT_SECRET";
    private static final String CLIENT_ID = "TEST_CLIENT_ID";
    private static final String REDIRECT_URL = "https://test.redirect.url";
    private static final String USER_INFO_URL = "https://test.user.info.url";
    private static final String AUTHORIZATION_URL = "https://test.authorization.url";
    private static final String TOKEN_URL = "https://test.token.url";
    private FakeActivity activity;
    private DataParameters parameters;

    @Override
    protected void setUp() throws Exception {
        activity = new FakeActivity();
        parameters = new DataParameters(
                CLIENT_ID,
                CLIENT_SECRET,
                new URL(AUTHORIZATION_URL),
                new URL(TOKEN_URL),
                new URL(USER_INFO_URL),
                new URL(REDIRECT_URL));
    }

    public void testRequiresActivity() {
        try {
            final AuthorizationEngine engine = new AuthorizationEngine();
            engine.obtainAuthorization(null, parameters);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresParameters() {
        try {
            final AuthorizationEngine engine = new AuthorizationEngine();
            engine.obtainAuthorization(activity, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }
}
