package com.ethernom.helloworld.presenter.privatekey;

public interface  GetAppKeyCallback {
    void getSucceeded(String appKey);
    void getFailed(String message);
}
