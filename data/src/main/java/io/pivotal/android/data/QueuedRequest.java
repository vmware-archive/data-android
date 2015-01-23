/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import java.util.ArrayList;

public class QueuedRequest<T> extends Request<T> {

    public static final class Methods {
        public static final int GET = 1;
        public static final int PUT = 2;
        public static final int DELETE = 3;
    }

    public int method;

    public QueuedRequest() {}

    public QueuedRequest(final Request<T> request) {
        this(request, 0);
    }

    public QueuedRequest(final Request<T> request, final int method) {
        super(request.accessToken, request.object, request.force);
        this.method = method;
    }

    public static class List<T> extends ArrayList<QueuedRequest<T>> {
        public static final long serialVersionUID = 0L;
    }
}
