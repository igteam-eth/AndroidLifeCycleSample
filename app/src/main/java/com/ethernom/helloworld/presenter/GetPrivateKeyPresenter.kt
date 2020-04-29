package com.ethernom.helloworld

import android.util.Log
import com.ethernom.helloworld.model.DataModel
import com.ethernom.helloworld.model.DataResponseModel
import com.ethernom.helloworld.model.HostModel
import com.ethernom.helloworld.presenter.GetAppKeyCallback
import com.ethernom.helloworld.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GetPrivateKeyPresenter(val getAppKeyCallback: GetAppKeyCallback?, private val sn: String, private val menuFac : String) {

    fun getData() {
        val token = "6$9a(_@|A3nr+p3y-wL$1@8*VFKW,Qt.m@O:fnqo<8#_4wG}pwJvIB*pxKb.sL3r"
        val auth = "Bearer $token"

        Log.d("GetPrivateKeyPresenter", "$menuFac : $sn")

        var mHost = HostModel("Ethernom, Inc.", "BLE Tracker", "com.ethernom.ble.tracker", "android","1.0.15" )

        var mData =  DataModel(sn, menuFac, mHost)
        val call: Call<DataResponseModel> = ApiClient.getClient.getAppKey(auth, mData)
        call.enqueue(object : Callback<DataResponseModel> {

            override fun onResponse(call: Call<DataResponseModel>?, response: Response<DataResponseModel>?) {
                if (response!!.isSuccessful){
                    getAppKeyCallback?.getSucceeded(response.body()!!.pkey)
                    Log.d("GetPrivateKeyPresenter", response.body()!!.pkey)
                }else{
                    getAppKeyCallback?.getFailed("Failed to get App Key.")
                }
            }

            override fun onFailure(call: Call<DataResponseModel>?, t: Throwable?) {
                getAppKeyCallback?.getFailed(t!!.message)
            }

        })
    }


}
