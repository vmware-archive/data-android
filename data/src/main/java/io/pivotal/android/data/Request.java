/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class Request<T> {

    public String accessToken;
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
        this(request.accessToken, request.object, request.force);
    }

    public Request(final String accessToken, final T object) {
        this(accessToken, object, false);
    }

    public Request(final String accessToken, final T object, final boolean force) {
        this.accessToken = accessToken;
        this.object = object;
        this.force = force;
    }
}
