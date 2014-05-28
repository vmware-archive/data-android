package com.pivotal.cf.mobile.datasdk.client;

import android.content.Context;
import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.api.FakeApiProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;
import com.pivotal.cf.mobile.datasdk.prefs.FakeAuthorizationPreferences;

import java.net.URL;
import java.util.concurrent.Semaphore;

public abstract class AbstractAuthorizedResourceClientTest<T extends AbstractAuthorizationClient> extends AndroidTestCase {

    protected static final String TEST_CLIENT_SECRET = "TEST_CLIENT_SECRET";
    protected static final String TEST_CLIENT_ID = "TEST_CLIENT_ID";
    protected static URL TEST_REDIRECT_URL;
    protected static URL TEST_AUTHORIZATION_URL;
    protected static URL TEST_TOKEN_URL;

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
        TEST_REDIRECT_URL = new URL("https://test.redirect.url");
        TEST_AUTHORIZATION_URL = new URL("https://test.authorization.url");
        TEST_TOKEN_URL = new URL("https://test.token.url");
        parameters = new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL);
        semaphore = new Semaphore(0);
    }

    protected void savePreferences() {
        preferences.setClientId(TEST_CLIENT_ID);
        preferences.setClientSecret(TEST_CLIENT_SECRET);
        preferences.setAuthorizationUrl(TEST_AUTHORIZATION_URL);
        preferences.setTokenUrl(TEST_TOKEN_URL);
        preferences.setRedirectUrl(TEST_REDIRECT_URL);
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