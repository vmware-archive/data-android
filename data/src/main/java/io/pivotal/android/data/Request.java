/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class Request<T> {

    public static final class Methods {
        public static final int GET = 1;
        public static final int PUT = 2;
        public static final int DELETE = 3;
    }

    public int method;
    public boolean force;

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type"
    )
    public T object;

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type"
    )
    public T fallback;

    public Request() {}

    public Request(final Request<T> request) {
        this(request.method, request.object, request.fallback, request.force);
    }

    public Request(final int method, final T object, final boolean force) {
        this(method, object, null, force);
    }

    public Request(final int method, final T object, final T fallback, final boolean force) {
        this.method = method;
        this.object = object;
        this.fallback = fallback;
        this.force = force;
    }

    public static class Get<T> extends Request<T> {

        public Get(final Request<T> request) {
            this(request.object, request.fallback, request.force);
        }

        public Get(final T object, final boolean force) {
            this(object, null, force);
        }

        public Get(final T object, final T fallback, final boolean force) {
            super(Methods.GET, object, fallback, force);
        }
    }

    public static class Put<T> extends Request<T> {

        public Put(final Request<T> request) {
            this(request.object, request.fallback, request.force);
        }

        public Put(final T object, final boolean force) {
            this(object, null, force);
        }

        public Put(final T object, final T fallback, final boolean force) {
            super(Methods.PUT, object, fallback, force);
        }
    }

    public static class Delete<T> extends Request<T> {

        public Delete(final Request<T> request) {
            this(request.object, request.fallback, request.force);
        }

        public Delete(final T object, final boolean force) {
            this(object, null, force);
        }

        public Delete(final T object, final T fallback, final boolean force) {
            super(Methods.DELETE, object, fallback, force);
        }
    }
}
