package io.pivotal.android.data;

import java.net.URL;

public class DataParameters {

    private final String clientId;
    private final String clientSecret;
    private final URL authorizationUrl;
    private final URL tokenUrl;
    private final String redirectUrl;
    private final URL dataServicesUrl;

    public DataParameters(String clientId, String clientSecret, URL authorizationUrl, URL tokenUrl, String redirectUrl, URL dataServicesUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authorizationUrl = authorizationUrl;
        this.tokenUrl = tokenUrl;
        this.redirectUrl = redirectUrl;
        this.dataServicesUrl = dataServicesUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public URL getAuthorizationUrl() {
        return authorizationUrl;
    }

    public URL getTokenUrl() {
        return tokenUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public URL getDataServicesUrl() {
        return dataServicesUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DataParameters other = (DataParameters) o;

        if (clientId != null ? !clientId.equals(other.clientId) : other.clientId != null) {
            return false;
        }
        if (clientSecret != null ? !clientSecret.equals(other.clientSecret) : other.clientSecret != null) {
            return false;
        }
        if (authorizationUrl != null ? !authorizationUrl.equals(other.authorizationUrl) : other.authorizationUrl != null) {
            return false;
        }
        if (tokenUrl != null ? !tokenUrl.equals(other.tokenUrl) : other.tokenUrl != null) {
            return false;
        }
        if (redirectUrl != null ? !redirectUrl.equals(other.redirectUrl) : other.redirectUrl != null) {
            return false;
        }
        if (dataServicesUrl != null ? !dataServicesUrl.equals(other.dataServicesUrl) : other.dataServicesUrl != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = clientId != null ? clientId.hashCode() : 0;
        result = 31 * result + (clientSecret != null ? clientSecret.hashCode() : 0);
        result = 31 * result + (authorizationUrl != null ? authorizationUrl.hashCode() : 0);
        result = 31 * result + (tokenUrl != null ? tokenUrl.hashCode() : 0);
        result = 31 * result + (redirectUrl != null ? redirectUrl.hashCode() : 0);
        result = 31 * result + (dataServicesUrl != null ? dataServicesUrl.hashCode() : 0);
        return result;
    }
}
