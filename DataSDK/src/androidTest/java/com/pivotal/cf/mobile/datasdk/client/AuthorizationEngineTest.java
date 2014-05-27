package com.pivotal.cf.mobile.datasdk.client;

import android.app.Activity;
import android.content.Context;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.activity.BaseAuthorizationActivity;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

public class AuthorizationEngineTest extends AbstractAuthorizedResourceClientTest<AuthorizationEngine> {

    private static final String TEST_AUTHORIZATION_CODE = "TEST AUTHORIZATION CODE";
    private static final String TEST_ACCESS_TOKEN = "TEST ACCESS TOKEN";
    private FakeActivity activity;
    private FakeBaseAuthorizationActivity authorizationActivity;
    private TokenResponse tokenResponse;

    private boolean shouldBaseAuthorizationActivityListenerBeSuccessful;

    private class FakeActivity extends Activity {
        // Empty
    }

    private class FakeBaseAuthorizationActivity extends BaseAuthorizationActivity {

        @Override
        public void onAuthorizationComplete() {
            assertTrue(shouldBaseAuthorizationActivityListenerBeSuccessful);
            semaphore.release();
        }

        @Override
        public void onAuthorizationFailed(String reason) {
            assertFalse(shouldBaseAuthorizationActivityListenerBeSuccessful);
            semaphore.release();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = new FakeActivity();
        authorizationActivity = new FakeBaseAuthorizationActivity();
        tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(TEST_ACCESS_TOKEN);
    }

    @Override
    protected AuthorizationEngine construct(Context context,
                                            AuthorizationPreferencesProvider preferencesProvider,
                                            ApiProvider apiProvider) {

        return new AuthorizationEngine(context, apiProvider, preferencesProvider);
    }

    private AuthorizationEngine getEngine() {
        return new AuthorizationEngine(getContext(), apiProvider, preferences);
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
        baseTestRequires(activity, new DataParameters(null, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testRequiresClientSecret() throws Exception {
        baseTestRequires(activity, new DataParameters(TEST_CLIENT_ID, null, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testRequiresAuthorizationUrl() throws Exception {
        baseTestRequires(activity, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, null, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testRequiresTokenUrl() throws Exception {
        baseTestRequires(activity, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, null, TEST_REDIRECT_URL));
    }

    public void testRequiresRedirectUrl() throws Exception {
        baseTestRequires(activity, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, null));
    }

    public void testObtainAuthorization() throws Exception {
        getEngine().obtainAuthorization(activity, parameters);
        assertEquals(1, apiProvider.getApiRequests().size());
        assertTrue(apiProvider.getApiRequests().get(0).didCallObtainAuthorization());
        assertEquals(TEST_CLIENT_ID, preferences.getClientId());
        assertEquals(TEST_CLIENT_SECRET, preferences.getClientSecret());
        assertEquals(TEST_AUTHORIZATION_URL, preferences.getAuthorizationUrl());
        assertEquals(TEST_TOKEN_URL, preferences.getTokenUrl());
        assertEquals(TEST_REDIRECT_URL, preferences.getRedirectUrl());
    }

    public void testAuthorizationCodeReceivedRequiresParametersToBeSavedFirst() throws Exception {
        try {
            getEngine().authorizationCodeReceived(authorizationActivity, TEST_AUTHORIZATION_CODE);
            fail();
        } catch (AuthorizationException e) {
            // success
        }
        assertEquals(0, apiProvider.getApiRequests().size());
    }

    public void testAuthorizationCodeReceivedSuccessfully() throws Exception {
        savePreferences();
        shouldBaseAuthorizationActivityListenerBeSuccessful = true;
        apiProvider.setShouldAuthorizationListenerBeSuccessful(true);
        apiProvider.setTokenResponse(tokenResponse);
        getEngine().authorizationCodeReceived(authorizationActivity, TEST_AUTHORIZATION_CODE);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertTrue(apiProvider.getApiRequests().get(0).didCallGetAccessToken());
        assertEquals(tokenResponse, apiProvider.getApiRequests().get(0).getSavedTokenReponse());
    }

    public void testAuthorizationCodeReceivedFailure() throws Exception {
        savePreferences();
        shouldBaseAuthorizationActivityListenerBeSuccessful = false;
        apiProvider.setShouldAuthorizationListenerBeSuccessful(false);
        getEngine().authorizationCodeReceived(authorizationActivity, TEST_AUTHORIZATION_CODE);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertTrue(apiProvider.getApiRequests().get(0).didCallGetAccessToken());
        assertNull(apiProvider.getApiRequests().get(0).getSavedTokenReponse());
    }

}
