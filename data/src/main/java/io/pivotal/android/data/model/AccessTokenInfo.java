/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data.model;

import com.google.api.client.auth.oauth2.Credential;

import java.util.List;

public class AccessTokenInfo {

    public String value;
    public String id;

    public static AccessTokenInfo findItemMatchingCredential(final Credential credential, List<AccessTokenInfo> items) {
        final String accessToken = credential.getAccessToken();
        for (final AccessTokenInfo item : items) {
            if (item.value.equals(accessToken)) {
                return item;
            }
        }
        return null;
    }
}
