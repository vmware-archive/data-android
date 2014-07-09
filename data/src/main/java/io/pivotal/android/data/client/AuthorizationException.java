package io.pivotal.android.data.client;

public class AuthorizationException extends RuntimeException {

    public AuthorizationException(String detailMessage) {
        super(detailMessage);
    }
}
