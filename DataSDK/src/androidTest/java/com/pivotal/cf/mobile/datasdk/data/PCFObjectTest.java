package com.pivotal.cf.mobile.datasdk.data;

import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.datasdk.api.FakeApiProvider;
import com.pivotal.cf.mobile.datasdk.client.AuthorizedResourceClient;
import com.pivotal.cf.mobile.datasdk.prefs.FakeAuthorizationPreferences;

import java.util.Collections;

public class PCFObjectTest extends AndroidTestCase {

    private static final String TEST_CLASS_NAME = "test_class_name";
    private static final String TEST_KEY = "test_key";
    private static final String TEST_VALUE = "test_value";

    private AuthorizedResourceClient client;
    private FakeApiProvider apiProvider;
    private FakeAuthorizationPreferences preferences;
    private PCFObject obj;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preferences = new FakeAuthorizationPreferences();
        apiProvider = new FakeApiProvider();
        client = new AuthorizedResourceClient(apiProvider, preferences);
        obj = new PCFObject(client, TEST_CLASS_NAME);
    }

    public void testRequiresClient() {
        try {
            new PCFObject(null, TEST_CLASS_NAME);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresNotNullClassName() {
        try {
            new PCFObject(client, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresNotEmptyClassName() {
        try {
            new PCFObject(client, "");
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testImplementsMap() {
        obj.put(TEST_KEY, TEST_VALUE);
        verifyPopulatedObject();

        obj.clear();
        verifyEmptyObject();

        obj.putAll(Collections.singletonMap(TEST_KEY, TEST_VALUE));
        verifyPopulatedObject();

        obj.remove(TEST_KEY);
        verifyEmptyObject();
    }

    private void verifyEmptyObject() {
        assertEquals(0, obj.size());
        assertFalse(obj.containsKey(TEST_KEY));
        assertFalse(obj.containsValue(TEST_VALUE));
        assertEquals(0, obj.entrySet().size());
        assertTrue(obj.isEmpty());
        assertFalse(obj.keySet().contains(TEST_KEY));
        assertEquals(0, obj.values().size());
    }

    private void verifyPopulatedObject() {
        assertEquals(TEST_VALUE, obj.get(TEST_KEY));
        assertEquals(1, obj.size());
        assertTrue(obj.containsKey(TEST_KEY));
        assertTrue(obj.containsValue(TEST_VALUE));
        assertEquals(1, obj.entrySet().size());
        assertFalse(obj.isEmpty());
        assertTrue(obj.keySet().contains(TEST_KEY));
        assertEquals(1, obj.values().size());
    }


}
