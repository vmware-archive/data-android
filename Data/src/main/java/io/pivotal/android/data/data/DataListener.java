package io.pivotal.android.data.data;

public interface DataListener {

    public void onSuccess(PivotalMSSObject object);

    public void onUnauthorized(PivotalMSSObject object);

    public void onFailure(PivotalMSSObject object, String reason);
}
