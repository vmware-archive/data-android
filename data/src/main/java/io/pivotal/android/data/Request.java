/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class Request<T> {

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
        this(request.object, request.fallback, request.force);
    }

    public Request(final T object, final boolean force) {
        this(object, null, force);
    }

    public Request(final T object, final T fallback, final boolean force) {
        this.object = object;
        this.fallback = fallback;
        this.force = force;
    }
}
