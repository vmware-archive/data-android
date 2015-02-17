/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import java.util.ArrayList;

public class PendingRequest<T> extends Request<T> {

    public PendingRequest() {}

    public PendingRequest(final Request<T> request) {
        super(request);
    }

    public static class List<T> extends ArrayList<PendingRequest<T>> {
        public static final long serialVersionUID = 0L;
    }
}
