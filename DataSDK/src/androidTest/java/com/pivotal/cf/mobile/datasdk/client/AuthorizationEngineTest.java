package com.pivotal.cf.mobile.datasdk.client;

import android.app.Activity;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.activity.BaseAuthorizationActivity;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.api.AuthorizedApiRequest;
import com.pivotal.cf.mobile.datasdk.api.FakeAuthorizedApiRequest;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.util.concurrent.Semaphore;

public class AuthorizationEngineTest extends AbstractAuthorizedClientTest<AuthorizationEngine> {

    private static final String TEST_AUTHORIZATION_CODE = "TEST AUTHORIZATION CODE";
    private static final String TEST_EXPECTED_ACCESS_TOKEN = "TEST EXPECTED ACCESS TOKEN";
    private static final String TEST_SAVED_ACCESS_TOKEN = "TEST SAVED ACCESS TOKEN";
    private FakeActivity activity;
    private FakeBaseAuthorizationActivity authorizationActivity;
    private TokenResponse expectedTokenResponse;
    private TokenResponse savedTokenResponse;

    private boolean shouldBaseAuthorizationActivityListenerBeSuccessful;
    private boolean shouldBaseAuthorizationActivityListenerAuthorizationDenied;

    private class FakeActivity extends Activity {
        // Empty
    }

    private class FakeBaseAuthorizationActivity extends BaseAuthorizationActivity {

        @Override
        public void onAuthorizationComplete() {
            assertTrue(shouldBaseAuthorizationActivityListenerBeSuccessful);
            assertFalse(shouldBaseAuthorizationActivityListenerAuthorizationDenied);
            semaphore.release();
        }

        @Override
        public void onAuthorizationDenied() {
            assertFalse(shouldBaseAuthorizationActivityListenerBeSuccessful);
            assertTrue(shouldBaseAuthorizationActivityListenerAuthorizationDenied);
            semaphore.release();
        }

        @Override
        public void onAuthorizationFailed(String reason) {
            assertFalse(shouldBaseAuthorizationActivityListenerBeSuccessful);
            assertFalse(shouldBaseAuthorizationActivityListenerAuthorizationDenied);
            semaphore.release();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = new FakeActivity();
        authorizationActivity = new FakeBaseAuthorizationActivity();
        expectedTokenResponse = new TokenResponse();
        expectedTokenResponse.setAccessToken(TEST_EXPECTED_ACCESS_TOKEN);
        savedTokenResponse = new TokenResponse();
        savedTokenResponse.setAccessToken(TEST_SAVED_ACCESS_TOKEN);
    }

    @Override
    protected AuthorizationEngine construct(AuthorizationPreferencesProvider preferencesProvider,
                                            ApiProvider apiProvider) {

        return new AuthorizationEngine(apiProvider, preferencesProvider);
    }

    private AuthorizationEngine getEngine() {
        return new AuthorizationEngine(apiProvider, preferences);
    }

    public void testObtainAuthorizationRequiresActivity() throws Exception {
        baseTestObtainAuthorizationRequires(null, parameters);
    }

    public void testObtainAuthorizationRequiresParameters() throws Exception {
        baseTestObtainAuthorizationRequires(activity, null);
    }

    public void testObtainAuthorizationRequiresNotNullClientId() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(null, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testObtainAuthorizationRequiresNotEmptyClientId() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters("", TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testObtainAuthorizationRequiresNotNullClientSecret() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, null, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testObtainAuthorizationRequiresNotEmptyClientSecret() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, "", TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testObtainAuthorizationRequiresAuthorizationUrl() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, null, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testObtainAuthorizationRequiresTokenUrl() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, null, TEST_REDIRECT_URL));
    }

    public void testObtainAuthorizationRequiresRedirectUrl() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, null));
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

    public void testAuthorizationCodeReceivedRequiresActivity() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(null, TEST_AUTHORIZATION_CODE);
    }

    public void testAuthorizationCodeReceivedRequiresNonNullAuthorizationCode() throws Exception {
        baseTestAuthorizationCodeReceivedWithInvalidAuthorizationCode(null);
    }

    public void testAuthorizationCodeReceivedRequiresNonEmptyAuthorizationCode() throws Exception {
        baseTestAuthorizationCodeReceivedWithInvalidAuthorizationCode("");
    }

    private void baseTestAuthorizationCodeReceivedWithInvalidAuthorizationCode(String authorizationCode) throws Exception {
        savePreferences();
        saveCredential();
        shouldBaseAuthorizationActivityListenerBeSuccessful = false;
        getEngine().authorizationCodeReceived(authorizationActivity, authorizationCode);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertCredential(null, apiProvider.getApiRequests().get(0));
    }

    public void testAuthorizationCodeReceivedSuccessfully() throws Exception {
        savePreferences();
        shouldBaseAuthorizationActivityListenerBeSuccessful = true;
        apiProvider.setShouldGetAccessTokenBeSuccessful(true);
        apiProvider.setTokenResponseToReturn(expectedTokenResponse);
        getEngine().authorizationCodeReceived(authorizationActivity, TEST_AUTHORIZATION_CODE);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertTrue(apiProvider.getApiRequests().get(0).didCallGetAccessToken());
        assertEquals(expectedTokenResponse, apiProvider.getApiRequests().get(0).getSavedTokenResponse());
    }

    public void testAuthorizationCodeReceivedUnauthorized() throws Exception {
        saveCredential();
        savePreferences();
        shouldBaseAuthorizationActivityListenerBeSuccessful = false;
        shouldBaseAuthorizationActivityListenerAuthorizationDenied = true;
        apiProvider.setShouldGetAccessTokenBeUnauthorized(true);
        getEngine().authorizationCodeReceived(authorizationActivity, TEST_AUTHORIZATION_CODE);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        final FakeAuthorizedApiRequest request = apiProvider.getApiRequests().get(0);
        assertTrue(request.didCallGetAccessToken());
        assertNull(request.getSavedTokenResponse());
        assertCredential(null, request);
    }

    public void testAuthorizationCodeReceivedFailure() throws Exception {
        savePreferences();
        shouldBaseAuthorizationActivityListenerBeSuccessful = false;
        apiProvider.setShouldGetAccessTokenBeSuccessful(false);
        getEngine().authorizationCodeReceived(authorizationActivity, TEST_AUTHORIZATION_CODE);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertTrue(apiProvider.getApiRequests().get(0).didCallGetAccessToken());
        assertNull(apiProvider.getApiRequests().get(0).getSavedTokenResponse());
    }

    public void testClearCredentialRequiresParameters() throws Exception {
        baseTestClearCredentialRequires(null);
    }
    
    public void testClearCredentialRequiresNotNullClientId() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(null, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testClearCredentialRequiresNotEmptyClientId() throws Exception {
        baseTestClearCredentialRequires(new DataParameters("", TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testClearCredentialRequiresNotNullClientSecret() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, null, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testClearCredentialRequiresNotEmptyClientSecret() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, "", TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testClearCredentialRequiresAuthorizationUrl() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, null, TEST_TOKEN_URL, TEST_REDIRECT_URL));
    }

    public void testClearCredentialRequiresTokenUrl() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, null, TEST_REDIRECT_URL));
    }

    public void testClearCredentialRequiresRedirectUrl() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, null));
    }

    public void testClearCredential() throws Exception {
        saveSavedTokenResponse();
        getEngine().clearAuthorization(parameters);
        assertEquals(1, apiProvider.getApiRequests().size());
        final FakeAuthorizedApiRequest request = apiProvider.getApiRequests().get(0);
        assertNull(request.getSavedTokenResponse());
        final Semaphore credentialSemaphore = new Semaphore(0);
        request.loadCredential(new AuthorizedApiRequest.LoadCredentialListener() {
            @Override
            public void onCredentialLoaded(Credential credential) {
                assertNull(credential);
                credentialSemaphore.release();
            }
        });
        credentialSemaphore.acquire();
    }
    
    private void saveSavedTokenResponse() {
        apiProvider.setSavedTokenResponse(savedTokenResponse);
    }


    private void baseTestObtainAuthorizationRequires(Activity activity, DataParameters parameters) throws Exception {
        try {
            getEngine().obtainAuthorization(activity, parameters);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    private void baseTestAuthorizationCodeReceivedRequires(BaseAuthorizationActivity activity, String authorizationCode) throws Exception {
        try {
            getEngine().authorizationCodeReceived(activity, authorizationCode);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    private void baseTestClearCredentialRequires(DataParameters parameters) throws Exception {
        try {
            getEngine().clearAuthorization(parameters);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }
}
