package com.pivotal.cf.mobile.datasdk.client;

import android.app.Activity;
import android.content.Context;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.activity.BaseAuthorizationActivity;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

public class AuthorizationEngineTest extends AbstractAuthorizedResourceClientTest<AuthorizationEngine> {

    private static final String TEST_AUTHORIZATION_CODE = "TEST AUTHORIZATION CODE";
    private FakeActivity activity;
    private FakeBaseAuthorizationActivity authorizationActivity;

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

    public void testObtainAuthorization() {
        getEngine().obtainAuthorization(activity, parameters);
        assertEquals(1, apiProvider.getApiRequests().size());
        assertTrue(apiProvider.getApiRequests().get(0).didCallObtainAuthorization());
    }

    public void testAuthorizationCodeReceivedSuccessfully() throws InterruptedException {
        shouldBaseAuthorizationActivityListenerBeSuccessful = true;
        apiProvider.setShouldAuthorizationListenerBeSuccessful(true);
        getEngine().authorizationCodeReceived(authorizationActivity, TEST_AUTHORIZATION_CODE);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertTrue(apiProvider.getApiRequests().get(0).didCallGetAccessToken());
    }

    public void testAuthorizationCodeReceivedFailure() throws InterruptedException {
        shouldBaseAuthorizationActivityListenerBeSuccessful = false;
        apiProvider.setShouldAuthorizationListenerBeSuccessful(false);
        getEngine().authorizationCodeReceived(authorizationActivity, TEST_AUTHORIZATION_CODE);
        semaphore.acquire();
        assertEquals(1, apiProvider.getApiRequests().size());
        assertTrue(apiProvider.getApiRequests().get(0).didCallGetAccessToken());
    }

}
