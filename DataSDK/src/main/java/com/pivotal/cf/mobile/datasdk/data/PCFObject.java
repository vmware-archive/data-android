package com.pivotal.cf.mobile.datasdk.data;

import com.pivotal.cf.mobile.datasdk.client.AuthorizedResourceClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO - should the value type "Object" be limited to items that are JSON-izable?
public class PCFObject implements Map<String, Object> {

    private AuthorizedResourceClient client;
    private String className;
    private Map<String, Object> map;

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
