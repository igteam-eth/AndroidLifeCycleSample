package com.ethernom.helloworld.presenter.checkupdate

import android.util.Log
import com.ethernom.helloworld.model.*
import com.ethernom.helloworld.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckUpdatePresenter {

    fun checkUpdate(checkUpdateCallback: CheckUpdateCallback?, fwInfoList: ArrayList<FwInfo>, sn: String, mfgId : String) {


        Log.d("check update", "check update : $fwInfoList" )

        val token = "6$9a(_@|A3nr+p3y-wL$1@8*VFKW,Qt.m@O:fnqo<8#_4wG}pwJvIB*pxKb.sL3r"
        val auth = "Bearer $token"

        val mHost = HostApp()
        mHost.company = "Ethernom, Inc."
        mHost.name = "BLE Tracker"
        mHost.app_id =  "com.ethernom.ble.tracker"
        mHost.os = "android"
        mHost.version =  "1.0.15"

        val checkUpdateRequestBody = CheckUpdateRequestBody()
        checkUpdateRequestBody.fwInfoList = fwInfoList
        checkUpdateRequestBody.hostApp = mHost
        checkUpdateRequestBody.mfgId = mfgId
        checkUpdateRequestBody.sn = sn

        val call: Call<CheckUpdateResponse> = ApiClient.getClient.checkUpdate(auth, checkUpdateRequestBody)
        call.enqueue(object : Callback<CheckUpdateResponse> {

            override fun onResponse(call: Call<CheckUpdateResponse>?, response: Response<CheckUpdateResponse>?) {
                if (response!!.isSuccessful){
                    if (response.body()!!.fwInfoList!!.isEmpty()){
                        checkUpdateCallback?.checkUpdateSuccess(require = false)
                    }else{
                        checkUpdateCallback?.checkUpdateSuccess(require = true)
                    }

                }else{
                    checkUpdateCallback?.checkUpdatedFailed("Failed to check card update")
                }
            }
            override fun onFailure(call: Call<CheckUpdateResponse>?, t: Throwable?) {
                checkUpdateCallback?.checkUpdatedFailed(t!!.message!!)
            }

        })
    }


}