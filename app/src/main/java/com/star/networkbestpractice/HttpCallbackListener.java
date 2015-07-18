package com.star.networkbestpractice;


public interface HttpCallbackListener {

    public void onFinish(String response);
    public void onError(Exception e);
}
