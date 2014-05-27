package com.pivotal.cf.mobile.datasdk.client;

import android.content.Context;
import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.api.FakeApiProvider;
import com.pivotal.cf.mobile.datasdk.client.AbstractAuthorizationClient;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;
import com.pivotal.cf.mobile.datasdk.prefs.FakeAuthorizationPreferences;

import java.net.URL;
import java.util.concurrent.Semaphore;

public abstract class AbstractAuthorizedResourceClientTest<T extends AbstractAuthorizationClient> extends AndroidTestCase {

    protected static final String CLIENT_SECRET = "TEST_CLIENT_SECRET";
    protected static final String CLIENT_ID = "TEST_CLIENT_ID";
    protected static URL REDIRECT_URL;
    protected static URL AUTHORIZATION_URL;
    protected static URL TOKEN_URL;

    protected FakeApiProvider apiProvider;
    protected FakeAuthorizationPreferences preferences;
    protected DataParameters parameters;
    protected Semaphore semaphore;

    protected abstract T construct(Context context,
                                   AuthorizationPreferencesProvider preferencesProvider,
                                   ApiProvider apiProvider);

    @Override
    protected void setUp() throws Exception {
        preferences = new FakeAuthorizationPreferences();
        apiProvider = new FakeApiProvider();
        REDIRECT_URL = new URL("https://test.redirect.url");
        AUTHORIZATION_URL = new URL("https://test.authorization.url");
        TOKEN_URL = new URL("https://test.token.url");
        parameters = new DataParameters(CLIENT_ID, CLIENT_SECRET, AUTHORIZATION_URL, TOKEN_URL, REDIRECT_URL);
        preferences.setClientId(CLIENT_ID);
        preferences.setClientSecret(CLIENT_SECRET);
        preferences.setAuthorizationUrl(AUTHORIZATION_URL);
        preferences.setTokenUrl(TOKEN_URL);
        preferences.setRedirectUrl(REDIRECT_URL);
        semaphore = new Semaphore(0);
    }

    public void testRequiresContext() {
        try {
            construct(null, preferences, apiProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresAuthorizationPreferencesProvider() {
        try {
            construct(getContext(), null, apiProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresHttpRequestFactoryProvider() {
        try {
            construct(getContext(), preferences, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

}
