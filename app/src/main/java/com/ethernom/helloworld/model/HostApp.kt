package com.ethernom.helloworld.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class HostApp {
    @Expose
    @SerializedName("version")
    var version: String? = null
    @Expose
    @SerializedName("os")
    var os: String? = null
    @Expose
    @SerializedName("app_id")
    var app_id: String? = null
    @Expose
    @SerializedName("name")
    var name: String? = null
    @Expose
    @SerializedName("company")
    var company: String? = null
}