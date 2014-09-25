/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

public class DataException extends Exception {

    private int mStatusCode;

    public DataException(int statusCode, final String message) {
        super(message);
        mStatusCode = statusCode;
    }

    public int getStatusCode() {
        return mStatusCode;
    }
}
