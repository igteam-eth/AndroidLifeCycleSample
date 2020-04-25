package com.ethernom.helloworld.webservice

import com.ethernom.helloworld.model.DataModel
import com.ethernom.helloworld.model.DataResponseModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiInterface {

    @POST("/get_app_key")
    fun getAppKey(@Header("Authorization") authorization: String, @Body dataModel: DataModel): Call<DataResponseModel>

}