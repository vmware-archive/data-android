package io.pivotal.android.data.api;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import io.pivotal.android.data.client.AuthorizationException;
import io.pivotal.android.data.prefs.AuthorizationPreferencesProvider;

public interface ApiProvider {

    public HttpTransport getTransport();
    public HttpRequestFactory getFactory(Credential credential);
    public AuthorizedApiRequest getAuthorizedApiRequest(AuthorizationPreferencesProvider authorizationPreferencesProvider) throws AuthorizationException;

}
