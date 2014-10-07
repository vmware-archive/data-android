/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

/* package */ class PreconditionFailedException extends DataException {

    public PreconditionFailedException(final int statusCode, final String message) {
        super(statusCode, message);
    }
}
