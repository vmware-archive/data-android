package com.pivotal.mss.datasdk.data;

public interface DataListener {

    public void onSuccess(PCFObject object);

    public void onUnauthorized(PCFObject object);

    public void onFailure(PCFObject object, String reason);
}
