package com.ethernom.helloworld.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FwInfo {

    constructor(version: String?, type: String?) {
        this.version = version
        this.type = type
    }

    @Expose
    @SerializedName("version")
    var version: String? = null
    @Expose
    @SerializedName("type")
    var type: String? = null
    @Expose
    @SerializedName("required")
    var required: Boolean? = null

}