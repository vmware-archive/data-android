package com.pivotal.cf.mobile.datasdk.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.client.AuthorizationException;
import com.pivotal.cf.mobile.datasdk.client.AuthorizedResourceClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO - should the value type "Object" be limited to items that are JSON-izable?
public class PCFObject implements Map<String, Object>, Parcelable {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String UTF8_ENCODING = "utf-8";
    private String className;
    private HashMap<String, Object> map;
    private String objectId;

    public PCFObject(String className) {
        setClassName(className);
        initializeData();
    }

    private void initializeData() {
        map = new HashMap<String, Object>();
    }

    // Properties

    public void setObjectId(String objectId) {
        if (objectId == null || objectId.isEmpty()) {
            throw new IllegalArgumentException("objectId may not be null or empty");
        }
        this.objectId = objectId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setClassName(String className) {
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("className may not be null or empty");
        }
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    // Data synchronization methods

    public void fetch(AuthorizedResourceClient client, final DataListener listener) throws AuthorizationException, DataException {
        verifyState(client);

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

    private void verifyState(AuthorizedResourceClient client) throws DataException {
        if (objectId == null || objectId.isEmpty()) {
            throw new DataException("objectId may not be null or empty");
        }
        if (className == null) {
            throw new DataException("className may not be null");
        }
        if (client == null) {
            throw new IllegalArgumentException("client may not be null");
        }
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

    public void save(AuthorizedResourceClient client, final DataListener listener) throws AuthorizationException, DataException {
        verifyState(client);

        final byte[] json = toJson();

        client.executeDataServicesRequest("PUT", className, objectId, null, JSON_CONTENT_TYPE, UTF8_ENCODING, json, new AuthorizedResourceClient.Listener() {

            @Override
            public void onSuccess(int httpStatusCode, String contentType, String contentEncoding, InputStream inputStream) {

                if (!isSuccessfulHttpStatusCode(httpStatusCode)) {
                    returnError("Received failure status code " + httpStatusCode + ".");
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

    /**
     * Generates the JSON-representation of the data in this object.
     *
     * @return  the JSON-representation of the data in this object.
     *
     * @throws  DataException will be thrown if the object could not be serialized to JSON.
     */
    public byte[] toJson() throws DataException {

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStreamWriter osw = new OutputStreamWriter(out);
        final JsonWriter writer = new JsonWriter(osw);
        try {
            writer.beginObject();
            for (final Entry<String, Object> entry : map.entrySet()) {
                writer.name(entry.getKey());
                if (entry.getValue() instanceof String) {
                    writer.value((String) entry.getValue());
                } else if (entry.getValue() instanceof Boolean) {
                    writer.value((Boolean) entry.getValue());
                } else if (entry.getValue() instanceof Float) {
                    final Float f = (Float) entry.getValue();
                    writer.value((Double) f.doubleValue());
                } else if (entry.getValue() instanceof Double) {
                    writer.value((Double) entry.getValue());
                } else if (entry.getValue() instanceof Integer || entry.getValue() instanceof Short || entry.getValue() instanceof Byte) {
                    writer.value((Number) entry.getValue());
                } else if (entry.getValue() instanceof Long) {
                    writer.value((Long) entry.getValue());
                }
            }
            writer.endObject();
            writer.close();
            return out.toByteArray();
        } catch (Exception e) {
            Logger.ex(e);
            throw new DataException("Could not serialize data to JSON: '" + e.getLocalizedMessage() + "'.");
        }
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

    // Parcelable stuff

    public static final Parcelable.Creator<PCFObject> CREATOR = new Parcelable.Creator<PCFObject>() {

        public PCFObject createFromParcel(Parcel in) {
            return new PCFObject(in);
        }

        public PCFObject[] newArray(int size) {
            return new PCFObject[size];
        }
    };

    private PCFObject(Parcel in) {
        className = in.readString();
        objectId = in.readString();
        map = (HashMap<String, Object>)in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(className);
        out.writeString(objectId);
        out.writeSerializable(map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof  PCFObject)) return false;

        PCFObject pcfObject = (PCFObject) o;

        if (!className.equals(pcfObject.className)) return false;
        if (!map.equals(pcfObject.map)) return false;
        if (!objectId.equals(pcfObject.objectId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = className.hashCode();
        result = 31 * result + map.hashCode();
        result = 31 * result + objectId.hashCode();
        return result;
    }
}
