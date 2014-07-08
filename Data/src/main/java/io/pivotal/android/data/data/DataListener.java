package io.pivotal.android.data.data;

public interface DataListener {

    public void onSuccess(DataObject object);

    public void onUnauthorized(DataObject object);

    public void onFailure(DataObject object, String reason);
}
