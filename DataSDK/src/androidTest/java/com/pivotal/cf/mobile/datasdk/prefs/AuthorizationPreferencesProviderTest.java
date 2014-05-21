package com.pivotal.cf.mobile.datasdk.prefs;

import android.test.AndroidTestCase;

import java.net.URL;

public class AuthorizationPreferencesProviderTest extends AndroidTestCase {

    private static final String CLIENT_SECRET = "TEST_CLIENT_SECRET";
    private static final String CLIENT_ID = "TEST_CLIENT_ID";
    private static final String REDIRECT_URL = "https://test.redirect.url";
    private static final String USER_INFO_URL = "https://test.user.info.url";
    private static final String AUTHORIZATION_URL = "https://test.authorization.url";
    private static final String TOKEN_URL = "https://test.token.url";

    @Override
    protected void setUp() throws Exception {
        final AuthorizationPreferencesProviderImpl prefs = getPrefs();
        prefs.clear();
    }

    public void testReset() {
        final AuthorizationPreferencesProviderImpl prefs = getPrefs();
        prefs.clear();
        assertNull(prefs.getClientId());
        assertNull(prefs.getClientSecret());
        assertNull(prefs.getAuthorizationUrl());
        assertNull(prefs.getTokenUrl());
        assertNull(prefs.getUserInfoUrl());
        assertNull(prefs.getRedirectUrl());
    }

    public void testSetClientId() {
        final AuthorizationPreferencesProvider prefs1 = getPrefs();
        prefs1.setClientId(CLIENT_ID);
        assertEquals(CLIENT_ID, prefs1.getClientId());
        final AuthorizationPreferencesProvider prefs2 = getPrefs();
        assertEquals(CLIENT_ID, prefs2.getClientId());
    }

    public void testSetClientSecret() {
        final AuthorizationPreferencesProvider prefs1 = getPrefs();
        prefs1.setClientSecret(CLIENT_SECRET);
        assertEquals(CLIENT_SECRET, prefs1.getClientSecret());
        final AuthorizationPreferencesProvider prefs2 = getPrefs();
        assertEquals(CLIENT_SECRET, prefs2.getClientSecret());
    }

    public void testSetAuthorizationUrl() throws Exception {
        final AuthorizationPreferencesProvider prefs1 = getPrefs();
        prefs1.setAuthorizationUrl(new URL(AUTHORIZATION_URL));
        assertEquals(new URL(AUTHORIZATION_URL), prefs1.getAuthorizationUrl());
        final AuthorizationPreferencesProvider prefs2 = getPrefs();
        assertEquals(new URL(AUTHORIZATION_URL), prefs2.getAuthorizationUrl());
    }

    public void testSetTokenUrl() throws Exception {
        final AuthorizationPreferencesProvider prefs1 = getPrefs();
        prefs1.setTokenUrl(new URL(TOKEN_URL));
        assertEquals(new URL(TOKEN_URL), prefs1.getTokenUrl());
        final AuthorizationPreferencesProvider prefs2 = getPrefs();
        assertEquals(new URL(TOKEN_URL), prefs2.getTokenUrl());
    }

    public void testSetUserInfoUrl() throws Exception {
        final AuthorizationPreferencesProvider prefs1 = getPrefs();
        prefs1.setUserInfoUrl(new URL(USER_INFO_URL));
        assertEquals(new URL(USER_INFO_URL), prefs1.getUserInfoUrl());
        final AuthorizationPreferencesProvider prefs2 = getPrefs();
        assertEquals(new URL(USER_INFO_URL), prefs2.getUserInfoUrl());
    }

    public void testSetRedirectUrl() throws Exception {
        final AuthorizationPreferencesProvider prefs1 = getPrefs();
        prefs1.setRedirectUrl(new URL(REDIRECT_URL));
        assertEquals(new URL(REDIRECT_URL), prefs1.getRedirectUrl());
        final AuthorizationPreferencesProvider prefs2 = getPrefs();
        assertEquals(new URL(REDIRECT_URL), prefs2.getRedirectUrl());
    }
    
    private AuthorizationPreferencesProviderImpl getPrefs() {
        return new AuthorizationPreferencesProviderImpl(getContext());
    }
}
