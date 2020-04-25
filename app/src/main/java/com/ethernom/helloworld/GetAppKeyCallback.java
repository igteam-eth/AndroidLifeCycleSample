package com.ethernom.helloworld;

public interface  GetAppKeyCallback {
    void getSucceeded(String appKey);
    void getFailed(String message);
}
