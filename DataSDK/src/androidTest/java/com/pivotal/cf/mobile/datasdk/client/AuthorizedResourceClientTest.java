package com.pivotal.cf.mobile.datasdk.client;

import android.content.Context;

import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.authorization.AbstractAuthorizedResourceClientTest;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AuthorizedResourceClientTest extends AbstractAuthorizedResourceClientTest<AuthorizedResourceClient> {

    private URL url;
    private Map<String, String> headers;
    private AuthorizedResourceClient.Listener listener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        url = new URL("http://test.get.url");
        headers = new HashMap<String, String>();
        listener = new AuthorizedResourceClient.Listener() {

            @Override
            public void onSuccess(int httpStatusCode, String contentType, InputStream result) {

            }

            @Override
            public void onFailure(String reason) {

            }
        };
    }

    @Override
    protected AuthorizedResourceClient construct(Context context, AuthorizationPreferencesProvider preferencesProvider) {
        return new AuthorizedResourceClient(context, preferencesProvider);
    }

    private AuthorizedResourceClient getClient() {
        return new AuthorizedResourceClient(getContext(), preferences);
    }

    private void baseTestGetRequires(final URL url,
                                     final Map<String, String> headers,
                                     DataParameters parameters,
                                     final AuthorizedResourceClient.Listener listener) throws Exception {
        try {
            getClient().get(url, headers, parameters, listener);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testGetRequiresUrl() throws Exception {
        baseTestGetRequires(null, headers, parameters, listener);
    }

    public void testGetRequiresParameters() throws Exception {
        baseTestGetRequires(url, headers, null, listener);
    }

    public void testGetRequiresListener() throws Exception {
        baseTestGetRequires(url, headers, parameters, null);
    }


}
