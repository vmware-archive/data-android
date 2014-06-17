package com.pivotal.cf.mobile.datasdk.data;

import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.datasdk.client.FakeAuthorizedResourceClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class PCFObjectTest extends AndroidTestCase {

    private static final String TEST_CLASS_NAME = "test_class_name";
    private static final String TEST_OBJECT_ID = "test_object_id";
    private static final String TEST_KEY = "test_key";
    private static final String TEST_VALUE = "test_value";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String TEST_CONTENT_ENCODING = "utf-8";
    private static final String TEST_SIMPLE_JSON_CONTENT = String.format("{ \"%s\" : \"%s\" }", TEST_KEY, TEST_VALUE);

    private FakeAuthorizedResourceClient client;
    private PCFObject obj;
    private Semaphore semaphore;

    private class SimpleSuccessfulDataListener implements DataListener {

        // overriding methods should call onSuccess after their own logic
        // so that the semaphore is released at the end.
        @Override
        public void onSuccess(PCFObject returnedObject) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            semaphore.release();
        }

        @Override
        public void onUnauthorized(PCFObject returnedObject) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            fail();
            semaphore.release();
        }

        @Override
        public void onFailure(PCFObject returnedObject, String reason) {
            assertEquals(0, returnedObject.size());
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            fail();
            semaphore.release();
        }
    }

    private class SimpleFailedDataListener implements DataListener {

        @Override
        public void onSuccess(PCFObject returnedObject) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            fail();
            semaphore.release();
        }

        @Override
        public void onUnauthorized(PCFObject returnedObject) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            fail();
            semaphore.release();
        }

        // overriding methods should call onSuccess after their own logic
        // so that the semaphore is released at the end.
        @Override
        public void onFailure(PCFObject returnedObject, String reason) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            assertEquals(0, returnedObject.size());
            semaphore.release();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = new FakeAuthorizedResourceClient();
        obj = new PCFObject(client, TEST_CLASS_NAME);
        semaphore = new Semaphore(0);
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

    public void testFetchRequiresNotNullObjectId() throws Exception {
        try {
            obj.setObjectId(null);
            obj.fetch(new SimpleFailedDataListener());
            fail();
        } catch(DataException e) {
            // success
        }
    }

    public void testFetchRequiresNotEmptyObjectId() throws Exception {
        try {
            obj.setObjectId("");
            obj.fetch(new SimpleFailedDataListener());
            fail();
        } catch(DataException e) {
            // success
        }
    }

    // TODO - restore this test once the server starts to send meaningful content-types.
//    public void testRequiresJsonContentType() throws Exception {
//        testFailedFetch("application/text", TEST_CONTENT_ENCODING, TEST_SIMPLE_JSON_CONTENT);
//    }

    public void testFetchesEmptyResponse() throws Exception {
        testFailedFetch(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "");
    }

    public void testFetchesMalformedResponse1() throws Exception {
        testFailedFetch(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "{\"PANTS");
    }

    public void testFetchesMalformedResponse2() throws Exception {
        testFailedFetch(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "\"PANTS");
    }

    public void testFetchesArray() throws Exception {
        testFailedFetch(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "[" + TEST_SIMPLE_JSON_CONTENT + "]");
    }

    public void testFetchWithFailedHttpStatus() throws Exception {
        client.setupFailedHttpStatusCode(404);
        obj.setObjectId(TEST_OBJECT_ID);
        obj.fetch(new SimpleFailedDataListener());
        semaphore.acquire();
    }

    public void testFetchesEmptyObject() throws Exception {
        client.setupSuccessfulGetResults(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "{}");
        obj.setObjectId(TEST_OBJECT_ID);
        obj.fetch(new SimpleSuccessfulDataListener() {

            @Override
            public void onSuccess(PCFObject returnedObject) {
                assertEquals(0, returnedObject.size());
                super.onSuccess(returnedObject);
            }
        });
        semaphore.acquire();
    }

    public void testFetchesOneStringField() throws Exception {
        baseTestFetchSuccessfully(TEST_SIMPLE_JSON_CONTENT, new HashMap<String, Object>() {{
            put(TEST_KEY, TEST_VALUE);
        }});
    }

    public void testFetchesThreeStringFields() throws Exception {
        baseTestFetchSuccessfully("{\"cats\":\"fuzzy\", \"dogs\":\"stinky\", \"goats\":\"noisy\"}", new HashMap<String, Object>() {{
            put("cats", "fuzzy");
            put("dogs", "stinky");
            put("goats", "noisy");
        }});
    }

    public void testFetchesTrueBooleanField() throws Exception {
        baseTestFetchSuccessfully("{\"boolean_field\":true}", new HashMap<String, Object>() {{
            put("boolean_field", true);
        }});
    }

    public void testFetchesFalseBooleanField() throws Exception {
        baseTestFetchSuccessfully("{\"boolean_field\":false}", new HashMap<String, Object>() {{
            put("boolean_field", false);
        }});
    }

    public void testFetchesIntegerFields() throws Exception {
        baseTestFetchSuccessfully("{\"score\":1337}", new HashMap<String, Object>() {{
            put("score", 1337.0); // GSON forces us to assume that all numbers are doubles
        }});
    }

    public void testFetchesFloatingPointFields() throws Exception {
        baseTestFetchSuccessfully("{\"limit\":13.37}", new HashMap<String, Object>() {{
            put("limit", 13.37);
        }});
    }

    private void baseTestFetchSuccessfully(String contentData, final Map<String, Object> expectedData) throws Exception {
        client.setupSuccessfulGetResults(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, contentData);
        obj.setObjectId(TEST_OBJECT_ID);
        obj.fetch(new SimpleSuccessfulDataListener() {

            @Override
            public void onSuccess(PCFObject returnedObject) {
                assertEquals(expectedData.size(), returnedObject.size());
                for(final Map.Entry<String, Object> entry : expectedData.entrySet()) {
                    assertEquals(entry.getValue(), returnedObject.get(entry.getKey()));
                }
                super.onSuccess(returnedObject);
            }
        });
        semaphore.acquire();
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

    private void testFailedFetch(String contentType, String contentEncoding, String contentData) throws Exception {
        client.setupSuccessfulGetResults(contentType, contentEncoding, contentData);
        obj.setObjectId(TEST_OBJECT_ID);
        obj.fetch(new SimpleFailedDataListener());
        semaphore.acquire();
    }

}
