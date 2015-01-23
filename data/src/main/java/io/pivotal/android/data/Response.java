/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

public class Response<T> {

    public T object;
    public DataError error;

    public Response() {}

    public Response(final T object) {
        this(object, null);
    }

    public Response(final T object, final DataError error) {
        this.object = object;
        this.error = error;
    }

    public boolean isSuccess() {
        return this.error == null;
    }

    public boolean isFailure() {
        return this.error != null;
    }

    public boolean isNotModified() {
        return this.error != null && this.error.isNotModified();
    }

    public boolean isNotFound() {
        return this.error != null && this.error.isNotFound();
    }

    public boolean hasPreconditionFailed() {
        return this.error != null && this.error.hasPreconditionFailed();
    }
}
