package io.pivotal.android.data.data;

import android.os.Parcel;
import android.test.AndroidTestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import io.pivotal.android.data.client.AuthorizationException;
import io.pivotal.android.data.client.FakeAuthorizedResourceClient;

public class PivotalMSSObjectTest extends AndroidTestCase {

    private static final String TEST_CLASS_NAME = "test_class_name";
    private static final String TEST_OBJECT_ID = "test_object_id";
    private static final String TEST_KEY = "test_key";
    private static final String TEST_VALUE = "test_value";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String TEST_CONTENT_ENCODING = "utf-8";
    private static final String TEST_SIMPLE_JSON_CONTENT = String.format("{ \"%s\" : \"%s\" }", TEST_KEY, TEST_VALUE);

    private FakeAuthorizedResourceClient client;
    private PivotalMSSObject obj;
    private Semaphore semaphore;

    private class SimpleSuccessfulDataListener implements DataListener {

        // overriding methods should call onSuccess after their own logic
        // so that the semaphore is released at the end.
        @Override
        public void onSuccess(PivotalMSSObject returnedObject) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            semaphore.release();
        }

        @Override
        public void onUnauthorized(PivotalMSSObject returnedObject) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            fail();
            semaphore.release();
        }

        @Override
        public void onFailure(PivotalMSSObject returnedObject, String reason) {
            assertEquals(0, returnedObject.size());
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            fail();
            semaphore.release();
        }
    }

    private class SimpleFailedDataListener implements DataListener {

        @Override
        public void onSuccess(PivotalMSSObject returnedObject) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            fail();
            semaphore.release();
        }

        @Override
        public void onUnauthorized(PivotalMSSObject returnedObject) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            fail();
            semaphore.release();
        }

        // overriding methods should call onSuccess after their own logic
        // so that the semaphore is released at the end.
        @Override
        public void onFailure(PivotalMSSObject returnedObject, String reason) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            assertEquals(0, returnedObject.size());
            semaphore.release();
        }
    }

    private class SimpleUnauthorizedDataListener implements DataListener {

        @Override
        public void onSuccess(PivotalMSSObject returnedObject) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            fail();
            semaphore.release();
        }

        @Override
        public void onUnauthorized(PivotalMSSObject returnedObject) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            semaphore.release();
        }

        // overriding methods should call onSuccess after their own logic
        // so that the semaphore is released at the end.
        @Override
        public void onFailure(PivotalMSSObject returnedObject, String reason) {
            assertEquals(obj.getObjectId(), returnedObject.getObjectId());
            fail();
            semaphore.release();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = new FakeAuthorizedResourceClient();
        obj = new PivotalMSSObject(TEST_CLASS_NAME);
        semaphore = new Semaphore(0);
    }

    public void testRequiresNotNullClassName() {
        try {
            new PivotalMSSObject(null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresNotEmptyClassName() {
        try {
            new PivotalMSSObject("");
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testClassName() {
        obj.setClassName("PENGUINS");
        assertEquals("PENGUINS", obj.getClassName());
    }

    public void testClassNameCanNotBeNull() {
        try {
            obj.setClassName(null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testObjectId() {
        obj.setObjectId(TEST_OBJECT_ID);
        assertEquals(TEST_OBJECT_ID, obj.getObjectId());
    }

    public void testSetObjectIDCanNotBeNull() {
        try {
            obj.setObjectId(null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testSetObjectIDCanNotBeEmpty() {
        try {
            obj.setObjectId("");
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

    public void testFetchRequiresObjectId() throws Exception {
        try {
            obj.fetch(client, new SimpleFailedDataListener());
            fail();
        } catch(DataException e) {
            // success
        }
    }

    public void testFetchRequiresClient() throws Exception {
        try {
            obj.setObjectId(TEST_OBJECT_ID);
            obj.fetch(null, new SimpleFailedDataListener());
            fail();
        } catch(IllegalArgumentException e) {
            // success
        }
    }

    // TODO - restore this test once the server starts to send meaningful content-types.
//    public void testRequiresJsonContentType() throws Exception {
//        baseTestFailedFetch("application/text", TEST_CONTENT_ENCODING, TEST_SIMPLE_JSON_CONTENT);
//    }

    public void testFetchesEmptyResponse() throws Exception {
        baseTestFailedFetch(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "");
    }

    public void testFetchesMalformedResponse1() throws Exception {
        baseTestFailedFetch(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "{\"PANTS");
    }

    public void testFetchesMalformedResponse2() throws Exception {
        baseTestFailedFetch(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "\"PANTS");
    }

    public void testFetchesArray() throws Exception {
        baseTestFailedFetch(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "[" + TEST_SIMPLE_JSON_CONTENT + "]");
    }

    public void testFetchWithFailedHttpStatus() throws Exception {
        client.setupFailedHttpStatusCode(404);
        obj.setObjectId(TEST_OBJECT_ID);
        obj.fetch(client, new SimpleFailedDataListener());
        semaphore.acquire();
    }

    public void testFetchWithUnauthorizedHttpStatus() throws Exception {
        client.setupUnauthorizedHttpStatusCode();
        obj.setObjectId(TEST_OBJECT_ID);
        obj.fetch(client, new SimpleUnauthorizedDataListener());
        semaphore.acquire();
    }

    public void testFetchesEmptyObject() throws Exception {
        client.setupSuccessfulRequestResults(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "{}");
        obj.setObjectId(TEST_OBJECT_ID);
        obj.fetch(client, new SimpleSuccessfulDataListener() {

            @Override
            public void onSuccess(PivotalMSSObject returnedObject) {
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

    public void testSaveRequiresObjectId() throws Exception {
        try {
            obj.save(client, new SimpleFailedDataListener());
            fail();
        } catch(DataException e) {
            // success
        }
    }

    public void testSaveRequiresClient() throws Exception {
        try {
            obj.setObjectId(TEST_OBJECT_ID);
            obj.save(null, new SimpleFailedDataListener());
            fail();
        } catch(IllegalArgumentException e) {
            // success
        }
    }

    public void testSaveWithFailedHttpStatus() throws Exception {
        client.setupFailedHttpStatusCode(404);
        obj.setObjectId(TEST_OBJECT_ID);
        obj.save(client, new SimpleFailedDataListener());
        semaphore.acquire();
    }

    public void testSaveWithUnauthorizedHttpStatus() throws Exception {
        client.setupUnauthorizedHttpStatusCode();
        obj.setObjectId(TEST_OBJECT_ID);
        obj.save(client, new SimpleUnauthorizedDataListener());
        semaphore.acquire();
    }

    public void testSavesEmptyObject() throws Exception {
        baseTestSavesSuccessfully(new HashMap<String, Object>() {{
        }});
    }

    public void testSavesOneStringField() throws Exception {
        baseTestSavesSuccessfully(new HashMap<String, Object>() {{
            put(TEST_KEY, TEST_VALUE);
        }});
    }

    public void testSavesThreeStringFields() throws Exception {
        baseTestSavesSuccessfully(new HashMap<String, Object>() {{
            put("cats", "fuzzy");
            put("dogs", "stinky");
            put("goats", "noisy");
        }});
    }

    public void testSavesBooleanFields() throws Exception {
        baseTestSavesSuccessfully(new HashMap<String, Object>() {{
            put("boolean_field_1", true);
            put("boolean_field_2", false);
        }});
    }

    public void testSavesNumericFields() throws Exception {
        baseTestSavesSuccessfully(new HashMap<String, Object>() {{
            put("float_field_1", 1337.0F);
            put("float_field_2", 0.0F);
            put("double_field_1", 1337.0D);
            put("double_field_2", 0.0D);
            put("integer_field_1", 1337);
            put("integer_field_2", 0);
            put("short_field_1", (short) 1337);
            put("short_field_2", (short) 0);
            put("byte_field_1", (byte) 1337);
            put("byte_field_2", (byte) 0);
            put("long_field_1", 1337L);
            put("long_field_2", 0L);
        }});
    }

    public void testDeletesRequiresObjectId() throws Exception {
        try {
            obj.delete(client, new SimpleFailedDataListener());
            fail();
        } catch(DataException e) {
            // success
        }
    }

    public void testDeletesRequiresClient() throws Exception {
        try {
            obj.setObjectId(TEST_OBJECT_ID);
            obj.delete(null, new SimpleFailedDataListener());
            fail();
        } catch(IllegalArgumentException e) {
            // success
        }
    }

    public void testDeletesSuccessfully() throws Exception {
        client.setupSuccessfulRequestResults(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "");
        obj.setObjectId(TEST_OBJECT_ID);
        obj.delete(client, new SimpleSuccessfulDataListener());
        semaphore.acquire();
    }

    public void testDeleteWithFailedHttpStatus() throws Exception {
        client.setupFailedHttpStatusCode(404);
        obj.setObjectId(TEST_OBJECT_ID);
        obj.delete(client, new SimpleFailedDataListener());
        semaphore.acquire();
    }

    public void testDeleteWithUnauthorizedHttpStatus() throws Exception {
        client.setupUnauthorizedHttpStatusCode();
        obj.setObjectId(TEST_OBJECT_ID);
        obj.delete(client, new SimpleUnauthorizedDataListener());
        semaphore.acquire();
    }

    private void baseTestSavesSuccessfully(Map<String, Object> data) throws AuthorizationException, DataException, InterruptedException, JSONException {
        client.setupSuccessfulRequestResults(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, "");
        obj.setObjectId(TEST_OBJECT_ID);
        for(final Map.Entry<String, Object> entry : data.entrySet()) {
            obj.put(entry.getKey(), entry.getValue());
        }
        obj.save(client, new SimpleSuccessfulDataListener());
        semaphore.acquire();
        JSONObject jsonObject = new JSONObject(new String(client.getRequestContentData()));
        if (data.size() == 0) {
            assertNull(jsonObject.names());
        } else {
            assertEquals(data.size(), jsonObject.names().length());
        }
        for(final Map.Entry<String, Object> entry : data.entrySet()) {
            final Object value = entry.getValue();
            final String key = entry.getKey();
            if (value instanceof String) {
                assertEquals(value, jsonObject.getString(key));
            } else if (value instanceof Boolean) {
                assertEquals(value, jsonObject.getBoolean(key));
            } else if (value instanceof Float) {
                assertEquals(value, (float) jsonObject.getDouble(key));
            } else if (value instanceof Double) {
                assertEquals(value, jsonObject.getDouble(key));
            } else if (value instanceof Byte) {
                assertEquals(value, (byte) jsonObject.getInt(key));
            } else if (value instanceof Short) {
                assertEquals(value, (short) jsonObject.getInt(key));
            } else if (value instanceof Integer) {
                assertEquals(value, jsonObject.getInt(key));
            } else if (value instanceof Long) {
                assertEquals(value, jsonObject.getLong(key));
            } else {
                throw new JSONException("unknown field type '" + value.getClass() + "'.");
            }
        }
    }

    private void baseTestFetchSuccessfully(String contentData, final Map<String, Object> expectedData) throws Exception {
        client.setupSuccessfulRequestResults(JSON_CONTENT_TYPE, TEST_CONTENT_ENCODING, contentData);
        obj.setObjectId(TEST_OBJECT_ID);
        obj.fetch(client, new SimpleSuccessfulDataListener() {

            @Override
            public void onSuccess(PivotalMSSObject returnedObject) {
                assertEquals(expectedData.size(), returnedObject.size());
                for(final Map.Entry<String, Object> entry : expectedData.entrySet()) {
                    assertEquals(entry.getValue(), returnedObject.get(entry.getKey()));
                }
                super.onSuccess(returnedObject);
            }
        });
        semaphore.acquire();
    }

    private void baseTestFailedFetch(String contentType, String contentEncoding, String contentData) throws Exception {
        client.setupSuccessfulRequestResults(contentType, contentEncoding, contentData);
        obj.setObjectId(TEST_OBJECT_ID);
        obj.fetch(client, new SimpleFailedDataListener());
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


    public void testEquals() {
        final PivotalMSSObject object1 = new PivotalMSSObject(TEST_CLASS_NAME);
        object1.setObjectId(TEST_OBJECT_ID);
        object1.put(TEST_KEY, TEST_VALUE);

        final PivotalMSSObject object2 = new PivotalMSSObject(TEST_CLASS_NAME);
        object2.setObjectId(TEST_OBJECT_ID);
        object2.put(TEST_KEY, TEST_VALUE);

        final PivotalMSSObject object3 = new PivotalMSSObject(TEST_CLASS_NAME + "X");
        object3.setObjectId(TEST_OBJECT_ID);
        object3.put(TEST_KEY, TEST_VALUE);

        final PivotalMSSObject object4 = new PivotalMSSObject(TEST_CLASS_NAME);
        object4.setObjectId(TEST_OBJECT_ID + "X");
        object4.put(TEST_KEY, TEST_VALUE);

        final PivotalMSSObject object5 = new PivotalMSSObject(TEST_CLASS_NAME);
        object5.setObjectId(TEST_OBJECT_ID);
        object5.put(TEST_KEY, TEST_VALUE + "X");

        final PivotalMSSObject object6 = new PivotalMSSObject(TEST_CLASS_NAME);
        object6.setObjectId(TEST_OBJECT_ID);
        object6.put(TEST_KEY + "X", TEST_VALUE);

        assertEquals(object1, object2);
        assertFalse(object1.equals(object3));
        assertFalse(object1.equals(object4));
        assertFalse(object1.equals(object5));
        assertFalse(object1.equals(object6));
        assertFalse(object3.equals(object4));
        assertFalse(object3.equals(object5));
        assertFalse(object3.equals(object6));
        assertFalse(object4.equals(object5));
        assertFalse(object4.equals(object6));
        assertFalse(object5.equals(object6));
    }
    
    public void testIsParcelable() {
        final PivotalMSSObject inputPivotalMSSObject = new PivotalMSSObject(TEST_CLASS_NAME);
        inputPivotalMSSObject.setObjectId(TEST_OBJECT_ID);
        inputPivotalMSSObject.put(TEST_KEY, TEST_VALUE);
        final PivotalMSSObject outputPivotalMSSObject = getObjectViaParcel(inputPivotalMSSObject);
        assertNotNull(outputPivotalMSSObject);
        assertEquals(inputPivotalMSSObject, outputPivotalMSSObject);
    }

    private PivotalMSSObject getObjectViaParcel(PivotalMSSObject inputObject) {
        final Parcel inputParcel = Parcel.obtain();
        inputObject.writeToParcel(inputParcel, 0);
        final byte[] bytes = inputParcel.marshall();
        assertNotNull(bytes);
        final Parcel outputParcel = Parcel.obtain();
        outputParcel.unmarshall(bytes, 0, bytes.length);
        outputParcel.setDataPosition(0);
        final PivotalMSSObject outputEvent = PivotalMSSObject.CREATOR.createFromParcel(outputParcel);
        inputParcel.recycle();
        outputParcel.recycle();
        return outputEvent;
    }
}
