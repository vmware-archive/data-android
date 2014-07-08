package io.pivotal.android.data.data;

public interface DataListener {

    public void onSuccess(MSSObject object);

    public void onUnauthorized(MSSObject object);

    public void onFailure(MSSObject object, String reason);
}
