/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.MalformedURLException;
import java.net.URL;

public class KeyValue {
    public String key, value, collection;

    public KeyValue() {}

    public KeyValue(final KeyValue object) {
        this(object.collection, object.key, object.value);
    }

    public KeyValue(final String collection, final String key, final String value) {
        this.collection = collection;
        this.key = key;
        this.value = value;
    }

    @JsonIgnore
    public String getUrl() throws MalformedURLException {
        return new URL(Pivotal.getServiceUrl() + "/" + collection + "/" + key).toString();
    }

    @Override
    public String toString() {
        return "key: " + key + ", value: " + value;
    }
}
