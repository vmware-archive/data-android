package com.pivotal.cf.mobile.datasdk.client;

import android.test.AndroidTestCase;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.api.FakeApiProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;
import com.pivotal.cf.mobile.datasdk.prefs.FakeAuthorizationPreferences;

import java.net.URL;
import java.util.concurrent.Semaphore;

public abstract class AbstractAuthorizedClientTest<T extends AbstractAuthorizationClient> extends AndroidTestCase {

    protected static final String TEST_CLIENT_SECRET = "TEST_CLIENT_SECRET";
    protected static final String TEST_CLIENT_SECRET_2 = "TEST_CLIENT_SECRET_2";
    protected static final String TEST_CLIENT_ID = "TEST_CLIENT_ID";
    protected static final String TEST_CLIENT_ID_2 = "TEST_CLIENT_ID_2";
    protected static URL TEST_REDIRECT_URL;
    protected static URL TEST_REDIRECT_URL_2;
    protected static URL TEST_AUTHORIZATION_URL;
    protected static URL TEST_AUTHORIZATION_URL_2;
    protected static URL TEST_TOKEN_URL;
    protected static URL TEST_TOKEN_URL_2;

    protected FakeApiProvider apiProvider;
    protected FakeAuthorizationPreferences preferences;
    protected DataParameters parameters;
    protected Semaphore semaphore;
    protected Credential credential;

    protected abstract T construct(AuthorizationPreferencesProvider preferencesProvider,
                                   ApiProvider apiProvider);

    @Override
    protected void setUp() throws Exception {
        preferences = new FakeAuthorizationPreferences();
        apiProvider = new FakeApiProvider();
        TEST_AUTHORIZATION_URL = new URL("https://test.authorization.url");
        TEST_TOKEN_URL = new URL("https://test.token.url");
        TEST_REDIRECT_URL = new URL("https://test.redirect.url");
        TEST_AUTHORIZATION_URL_2 = new URL("https://test.authorization.url.2");
        TEST_TOKEN_URL_2 = new URL("https://test.token.url.2");
        TEST_REDIRECT_URL_2 = new URL("https://test.redirect.url.2");
        parameters = new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL);
        credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
        semaphore = new Semaphore(0);
    }

    protected void savePreferences() {
        preferences.setClientId(TEST_CLIENT_ID);
        preferences.setClientSecret(TEST_CLIENT_SECRET);
        preferences.setAuthorizationUrl(TEST_AUTHORIZATION_URL);
        preferences.setTokenUrl(TEST_TOKEN_URL);
        preferences.setRedirectUrl(TEST_REDIRECT_URL);
    }

    public void testRequiresAuthorizationPreferencesProvider() {
        try {
            construct(null, apiProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresHttpRequestFactoryProvider() {
        try {
            construct(preferences, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    protected void saveCredential() {
        apiProvider.setCredential(credential);
    }

    protected void assertCredentialEquals(final Credential expectedCredential, FakeApiProvider apiProvider) {
        assertEquals(expectedCredential, apiProvider.getCredential());
    }
}
