/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

public class DataError extends Error {

    private int mCode = -1;

    public DataError(final Exception e) {
        super(e.getLocalizedMessage(), e);

        if (e instanceof DataHttpException) {
            mCode = ((DataHttpException) e).getStatusCode();
        }
    }

    public int getCode() {
        return mCode;
    }

    public boolean isUnauthorized() {
        return mCode == 401;
    }

    public boolean hasPreconditionFailed() {
        return mCode == 412;
    }

    public boolean isNotModified() {
        return mCode == 304;
    }
}
