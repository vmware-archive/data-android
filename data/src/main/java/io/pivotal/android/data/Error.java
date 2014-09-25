/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

public class Error {

    private int mCode = -1;
    private String mMessage;

    public Error(final Exception e) {
        if (e instanceof DataException) {
            mCode = ((DataException) e).getStatusCode();
        }
        mMessage = e.getLocalizedMessage();
    }

    public int getCode() {
        return mCode;
    }

    public String getMessage() {
        return mMessage;
    }

    public boolean isUnauthorized() {
        return mCode == 401;
    }
}
