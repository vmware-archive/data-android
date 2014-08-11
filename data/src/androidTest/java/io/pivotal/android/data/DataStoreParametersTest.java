/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */

package io.pivotal.android.data;

import android.test.AndroidTestCase;

public class DataStoreParametersTest extends AndroidTestCase {

    private static final String TEST_CLIENT_ID = "test-client-id";
    private static final String TEST_CLIENT_SECRET = "test-client-secret";
    private static final String TEST_AUTHORIZATION_URL = "http://test.authorization.url";
    private static final String TEST_REDIRECT_URL = "http://test.redirect.url";
    private static final String TEST_DATA_SERVICES_URL = "http://test.data.services.url";

    public void testAllParametersNull() {
        final DataStoreParameters parameters = new DataStoreParameters(null, null, null, null, null);
        assertNull(parameters.getClientId());
        assertNull(parameters.getClientSecret());
        assertNull(parameters.getAuthorizationUrl());
        assertNull(parameters.getRedirectUrl());
        assertNull(parameters.getDataServicesUrl());
    }

    public void testAllParametersSet() {
        final DataStoreParameters parameters = new DataStoreParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL, TEST_REDIRECT_URL, TEST_DATA_SERVICES_URL);
        assertEquals(TEST_CLIENT_ID, parameters.getClientId());
        assertEquals(TEST_CLIENT_SECRET, parameters.getClientSecret());
        assertEquals(TEST_AUTHORIZATION_URL, parameters.getAuthorizationUrl());
        assertEquals(TEST_REDIRECT_URL, parameters.getRedirectUrl());
        assertEquals(TEST_DATA_SERVICES_URL, parameters.getDataServicesUrl());
    }

    public void testWhitespaceTrimmed() {
        final DataStoreParameters parameters = new DataStoreParameters(" " + TEST_CLIENT_ID + " ", "\t" + TEST_CLIENT_SECRET + "\t", "\n" + TEST_AUTHORIZATION_URL + "\n", "  " + TEST_REDIRECT_URL + "\t", "\r" + TEST_DATA_SERVICES_URL + "\r\n");
        assertEquals(TEST_CLIENT_ID, parameters.getClientId());
        assertEquals(TEST_CLIENT_SECRET, parameters.getClientSecret());
        assertEquals(TEST_AUTHORIZATION_URL, parameters.getAuthorizationUrl());
        assertEquals(TEST_REDIRECT_URL, parameters.getRedirectUrl());
        assertEquals(TEST_DATA_SERVICES_URL, parameters.getDataServicesUrl());
    }

    public void testTrailingSlashesStrippedFromPaths() {
        final DataStoreParameters parameters = new DataStoreParameters(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_AUTHORIZATION_URL + "/", TEST_REDIRECT_URL + "/", TEST_DATA_SERVICES_URL + "/");
        assertEquals(TEST_AUTHORIZATION_URL, parameters.getAuthorizationUrl());
        assertEquals(TEST_REDIRECT_URL, parameters.getRedirectUrl());
        assertEquals(TEST_DATA_SERVICES_URL, parameters.getDataServicesUrl());
    }
}
