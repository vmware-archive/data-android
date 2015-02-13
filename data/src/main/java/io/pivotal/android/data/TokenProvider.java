/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

public interface TokenProvider {
    public String provideAccessToken(final Context context);
}
