/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data.data;

public interface DataListener {

    public void onSuccess(DataObject object);

    public void onUnauthorized(DataObject object);

    public void onFailure(DataObject object, String reason);
}
