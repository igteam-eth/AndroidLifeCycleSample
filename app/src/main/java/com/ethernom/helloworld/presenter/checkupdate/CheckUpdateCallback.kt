package com.ethernom.helloworld.presenter.checkupdate

interface CheckUpdateCallback {
    fun checkUpdateSuccess(require: Boolean)
    fun checkUpdatedFailed(message: String)
}