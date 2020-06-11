package com.ethernom.helloworld.callback

interface StateMachineCallback {
    fun hideProgressBarState()
    fun showMessageErrorState(message: String)
    fun getPrivateKeyFailed(message: String)
    fun checkUpdateFailed(message: String)
    fun getPinSucceeded(pin: String)
    fun onGetMajorMinorSucceeded(data: String)
    fun appRequiredToUpdate()
    fun appMustBeUpdate()
}
