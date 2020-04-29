package com.ethernom.helloworld.presenter;

public interface  GetAppKeyCallback {
    void getSucceeded(String appKey);
    void getFailed(String message);
}
