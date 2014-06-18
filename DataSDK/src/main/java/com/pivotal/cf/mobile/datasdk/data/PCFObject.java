package com.pivotal.cf.mobile.datasdk.data;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.client.AuthorizationException;
import com.pivotal.cf.mobile.datasdk.client.AuthorizedResourceClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO - should the value type "Object" be limited to items that are JSON-izable?
public class PCFObject implements Map<String, Object> {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private AuthorizedResourceClient client;
    private String className;
    private Map<String, Object> map;
    private String objectId;

    public PCFObject(AuthorizedResourceClient client, String className) {
        verifyArguments(client, className);
        saveArguments(client, className);
        initializeData();
    }

    private void verifyArguments(AuthorizedResourceClient client, String className) {
        if (client == null) {
            throw new IllegalArgumentException("client may not be null");
        }
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("className may not be null or empty");
        }
    }

    private void saveArguments(AuthorizedResourceClient client, String className) {
        this.client = client;
        this.className = className;
    }

    private void initializeData() {
        map = new HashMap<String, Object>();
    }

    // Properties

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getClassName() {
        return className;
    }

    // Data synchronization methods

    public void fetch(final DataListener listener) throws AuthorizationException, DataException {
        if (objectId == null || objectId.isEmpty()) {
            throw new DataException("objectId may not be null or empty");
        }

        client.executeDataServicesRequest("GET", className, objectId, null, "", "", null, new AuthorizedResourceClient.Listener() {

            @Override
            public void onSuccess(int httpStatusCode, String contentType, String contentEncoding, InputStream inputStream) {

                if (!isSuccessfulHttpStatusCode(httpStatusCode)) {
                    returnError("Received failure status code " + httpStatusCode + ".");
                    return;
                }

                // TODO - restore this test once the server starts to send meaningful content-types.

//                if (!contentType.contains(JSON_CONTENT_TYPE)) {
//                    returnError("Unsupported content type \"" + contentType + "\".");
//                    return;
//                }

                try {
                    parseJsonAndSetFields(inputStream, contentEncoding);
                } catch (Exception e) {
                    returnError(e.getLocalizedMessage());
                    return;
                }

                if (listener != null) {
                    listener.onSuccess(PCFObject.this);
                }
            }

            @Override
            public void onUnauthorized() {
                if (listener != null) {
                    listener.onUnauthorized(PCFObject.this);
                };
            }

            @Override
            public void onFailure(String reason) {
                Logger.e("Error fetching PCFObject data: \"" + reason + "\".");
                returnError(reason);
            }

            private void returnError(String reason) {
                if (listener != null) {
                    listener.onFailure(PCFObject.this, reason);
                }
            }
        });
    }

    private boolean isSuccessfulHttpStatusCode(int httpStatusCode) {
        return httpStatusCode >= 200 && httpStatusCode < 300;
    }

    private void parseJsonAndSetFields(InputStream in, String contentEncoding) throws Exception {

        if (contentEncoding == null) {
            contentEncoding = "utf-8";
        }

        final JsonReader reader = new JsonReader(new InputStreamReader(in, contentEncoding));
        reader.beginObject();
        while(reader.hasNext()) {
            final String key = reader.nextName();
            final JsonToken token = reader.peek();
            // TODO - support more complicated object types
            if (token == JsonToken.STRING) {
                final String value = reader.nextString();
                map.put(key, value);
            } else if (token == JsonToken.BOOLEAN) {
                map.put(key, reader.nextBoolean());
            } else if (token == JsonToken.NUMBER) {
                // Sadly, GSON gives no help determining if a value is an int, double, or long.
                // We must always assume that it's a double value.
                map.put(key, reader.nextDouble());
            }
        }
        reader.endObject();
    }

    // Map<String, Object> methods

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> incomingMap) {
        map.putAll(incomingMap);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

}
