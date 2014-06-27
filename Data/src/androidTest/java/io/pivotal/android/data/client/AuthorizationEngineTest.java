package io.pivotal.android.data.client;

import android.app.Activity;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;

import java.util.concurrent.Semaphore;

import io.pivotal.android.data.DataParameters;
import io.pivotal.android.data.activity.BaseAuthorizationActivity;
import io.pivotal.android.data.api.ApiProvider;
import io.pivotal.android.data.api.AuthorizedApiRequest;
import io.pivotal.android.data.api.FakeAuthorizedApiRequest;
import io.pivotal.android.data.prefs.AuthorizationPreferencesProvider;

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

    public void testSetParametersRequiresParameters() throws Exception {
        baseTestSetParametersRequires(null);
    }

    public void testSetParametersRequiresNotNullClientId() throws Exception {
        baseTestSetParametersRequires(new DataParameters(null, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testSetParametersRequiresNotEmptyClientId() throws Exception {
        baseTestSetParametersRequires(new DataParameters("", TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testSetParametersRequiresNotNullClientSecret() throws Exception {
        baseTestSetParametersRequires(new DataParameters(TEST_CLIENT_ID, null, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testSetParametersRequiresNotEmptyClientSecret() throws Exception {
        baseTestSetParametersRequires(new DataParameters(TEST_CLIENT_ID, "", TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testSetParametersRequiresAuthorizationUrl() throws Exception {
        baseTestSetParametersRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, null, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testSetParametersRequiresTokenUrl() throws Exception {
        baseTestSetParametersRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, null, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testSetParametersRequiresNotNullRedirectUrl() throws Exception {
        baseTestSetParametersRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, null, TEST_DATA_SERVICES_URL));
    }

    public void testSetParametersRequiresNotEmptyRedirectUrl() throws Exception {
        baseTestSetParametersRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, "", TEST_DATA_SERVICES_URL));
    }

    public void testSetParametersRequiresDataServices() throws Exception {
        baseTestSetParametersRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, null));
    }

    public void testCallingSetParametersWithSameParametersWillNotClearCredentials() throws Exception {
        getEngine().setParameters(parameters);
        saveCredential();
        getEngine().setParameters(parameters);
        assertCredentialEquals(credential, apiProvider);
    }

    public void testChangingClientIdWillClearCredentials() throws Exception {
        getEngine().setParameters(parameters);
        saveCredential();
        final DataParameters modifiedParameters = new DataParameters(TEST_CLIENT_ID_2, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL);
        getEngine().setParameters(modifiedParameters);
        assertCredentialEquals(null, apiProvider);
    }

    public void testChangingClientSecretWillClearCredentials() throws Exception {
        getEngine().setParameters(parameters);
        saveCredential();
        final DataParameters modifiedParameters = new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET_2, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL);
        getEngine().setParameters(modifiedParameters);
        assertCredentialEquals(null, apiProvider);
    }

    public void testChangingAuthorizationUrlWillClearCredentials() throws Exception {
        getEngine().setParameters(parameters);
        saveCredential();
        final DataParameters modifiedParameters = new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL_2, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL);
        getEngine().setParameters(modifiedParameters);
        assertCredentialEquals(null, apiProvider);
    }

    public void testChangingTokenUrlWillClearCredentials() throws Exception {
        getEngine().setParameters(parameters);
        saveCredential();
        final DataParameters modifiedParameters = new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL_2, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL);
        getEngine().setParameters(modifiedParameters);
        assertCredentialEquals(null, apiProvider);
    }

    public void testChangingRedirectUrlWillClearCredentials() throws Exception {
        getEngine().setParameters(parameters);
        saveCredential();
        final DataParameters modifiedParameters = new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL_2, TEST_DATA_SERVICES_URL);
        getEngine().setParameters(modifiedParameters);
        assertCredentialEquals(null, apiProvider);
    }

    public void testChangingDataServicesUrlDoesNotClearCredentials() throws Exception {
        getEngine().setParameters(parameters);
        saveCredential();
        final DataParameters modifiedParameters = new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL_2);
        getEngine().setParameters(modifiedParameters);
        assertCredentialEquals(credential, apiProvider);
    }

    public void testObtainAuthorizationRequiresActivity() throws Exception {
        baseTestObtainAuthorizationRequires(null, parameters);
    }

    public void testObtainAuthorizationRequiresParameters() throws Exception {
        baseTestObtainAuthorizationRequires(activity, null);
    }

    public void testObtainAuthorizationRequiresNotNullClientId() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(null, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testObtainAuthorizationRequiresNotEmptyClientId() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters("", TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testObtainAuthorizationRequiresNotNullClientSecret() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, null, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testObtainAuthorizationRequiresNotEmptyClientSecret() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, "", TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testObtainAuthorizationRequiresAuthorizationUrl() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, null, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testObtainAuthorizationRequiresTokenUrl() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, null, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testObtainAuthorizationRequiresNotNullRedirectUrl() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, null, TEST_DATA_SERVICES_URL));
    }

    public void testObtainAuthorizationRequiresNotEmptyRedirectUrl() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, "", TEST_DATA_SERVICES_URL));
    }

    public void testObtainAuthorizationRequiresDataServicesUrl() throws Exception {
        baseTestObtainAuthorizationRequires(activity, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, null));
    }

    public void testObtainAuthorization() throws Exception {
        getEngine().setParameters(parameters);
        getEngine().obtainAuthorization(activity);
        assertEquals(1, apiProvider.getApiRequests().size());
        assertTrue(apiProvider.getApiRequests().get(0).didCallObtainAuthorization());
        assertEquals(TEST_CLIENT_ID, preferences.getClientId());
        assertEquals(TEST_CLIENT_SECRET, preferences.getClientSecret());
        assertEquals(TEST_AUTHORIZATION_URL, preferences.getAuthorizationUrl());
        assertEquals(TEST_TOKEN_URL, preferences.getTokenUrl());
        assertEquals(TEST_REDIRECT_URL, preferences.getRedirectUrl());
    }

    public void testAuthorizationCodeReceivedRequiresActivity() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(null, TEST_AUTHORIZATION_CODE, parameters);
    }

    public void testAuthorizationCodeReceivedRequiresNonNullAuthorizationCode() throws Exception {
        baseTestAuthorizationCodeReceivedWithInvalidAuthorizationCode(null);
    }

    public void testAuthorizationCodeReceivedRequiresNonEmptyAuthorizationCode() throws Exception {
        baseTestAuthorizationCodeReceivedWithInvalidAuthorizationCode("");
    }

    public void testAuthorizationCodeReceivedRequiresParameters() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(authorizationActivity, TEST_AUTHORIZATION_CODE, null);
    }

    public void testAuthorizationCodeReceivedRequiresNotNullClientId() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(authorizationActivity, TEST_AUTHORIZATION_CODE, new DataParameters(null, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testAuthorizationCodeReceivedRequiresNotEmptyClientId() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(authorizationActivity, TEST_AUTHORIZATION_CODE, new DataParameters("", TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testAuthorizationCodeReceivedRequiresNotNullClientSecret() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(authorizationActivity, TEST_AUTHORIZATION_CODE, new DataParameters(TEST_CLIENT_ID, null, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testAuthorizationCodeReceivedRequiresNotEmptyClientSecret() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(authorizationActivity, TEST_AUTHORIZATION_CODE, new DataParameters(TEST_CLIENT_ID, "", TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testAuthorizationCodeReceivedRequiresAuthorizationUrl() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(authorizationActivity, TEST_AUTHORIZATION_CODE, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, null, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testAuthorizationCodeReceivedRequiresTokenUrl() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(authorizationActivity, TEST_AUTHORIZATION_CODE, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, null, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testAuthorizationCodeReceivedRequiresNotNullRedirectUrl() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(authorizationActivity, TEST_AUTHORIZATION_CODE, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, null, TEST_DATA_SERVICES_URL));
    }

    public void testAuthorizationCodeReceivedRequiresNotEmptyRedirectUrl() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(authorizationActivity, TEST_AUTHORIZATION_CODE, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, "", TEST_DATA_SERVICES_URL));
    }

    public void testAuthorizationCodeReceivedRequiresDataServicesUrl() throws Exception {
        baseTestAuthorizationCodeReceivedRequires(authorizationActivity, TEST_AUTHORIZATION_CODE, new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, null));
    }

    private void baseTestAuthorizationCodeReceivedWithInvalidAuthorizationCode(String authorizationCode) throws Exception {
        savePreferences();
        saveCredential();
        shouldBaseAuthorizationActivityListenerBeSuccessful = false;
        getEngine().authorizationCodeReceived(authorizationActivity, authorizationCode);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertCredentialEquals(null, apiProvider);
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
        assertCredentialEquals(null, apiProvider);
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
        baseTestClearCredentialRequires(new DataParameters(null, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testClearCredentialRequiresNotEmptyClientId() throws Exception {
        baseTestClearCredentialRequires(new DataParameters("", TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testClearCredentialRequiresNotNullClientSecret() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, null, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testClearCredentialRequiresNotEmptyClientSecret() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, "", TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testClearCredentialRequiresAuthorizationUrl() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, null, TEST_TOKEN_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testClearCredentialRequiresTokenUrl() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, null, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL));
    }

    public void testClearCredentialRequiresNotNullRedirectUrl() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, null, TEST_DATA_SERVICES_URL));
    }

    public void testClearCredentialRequiresNotEmptyRedirectUrl() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, "", TEST_DATA_SERVICES_URL));
    }

    public void testClearCredentialRequiresDataServicesUrl() throws Exception {
        baseTestClearCredentialRequires(new DataParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_TOKEN_URL, TEST_REDIRECT_URL, null));
    }

    public void testClearCredential() throws Exception {
        saveSavedTokenResponse();
        getEngine().setParameters(parameters);
        getEngine().clearAuthorization();
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

    private AuthorizationEngine getEngine() {
        return new AuthorizationEngine(apiProvider, preferences);
    }

    private void saveSavedTokenResponse() {
        apiProvider.setSavedTokenResponse(savedTokenResponse);
    }

    private void baseTestSetParametersRequires(DataParameters parameters) throws Exception {
        try {
            getEngine().setParameters(parameters);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }
    
    private void baseTestObtainAuthorizationRequires(Activity activity, DataParameters parameters) throws Exception {
        try {
            if (parameters != null) {
                getEngine().setParameters(parameters);
            }
            getEngine().obtainAuthorization(activity);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        } catch (AuthorizationException e) {
            // success
        }
    }

    private void baseTestAuthorizationCodeReceivedRequires(BaseAuthorizationActivity activity, String authorizationCode, DataParameters parameters) throws Exception {
        try {
            if (parameters != null) {
                getEngine().setParameters(parameters);
            }
            getEngine().authorizationCodeReceived(activity, authorizationCode);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        } catch (AuthorizationException e) {
            // success
        }
    }

    private void baseTestClearCredentialRequires(DataParameters parameters) throws Exception {
        try {
            if (parameters != null) {
                getEngine().setParameters(parameters);
            }
            getEngine().clearAuthorization();
            fail();
        } catch (IllegalArgumentException e) {
            // success
        } catch (AuthorizationException e) {
            // success
        }
    }
}
