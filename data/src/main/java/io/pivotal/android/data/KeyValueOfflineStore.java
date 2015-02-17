/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import android.content.Context;

public class KeyValueOfflineStore extends OfflineStore<KeyValue> {

    public static OfflineStore<KeyValue> create(final Context context) {
        final LocalStore<KeyValue> localStore = new KeyValueLocalStore(context);
        final RemoteStore<KeyValue> remoteStore = new KeyValueRemoteStore(context);
        return new KeyValueOfflineStore(context, localStore, remoteStore);
    }

    public KeyValueOfflineStore(final Context context, final LocalStore<KeyValue> localStore, final RemoteStore<KeyValue> remoteStore) {
        super(context, localStore, remoteStore);
    }
}
