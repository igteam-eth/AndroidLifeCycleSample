package com.ethernom.helloworld.model

import com.google.gson.annotations.SerializedName

class CheckUpdateResponse {


    @SerializedName("fw_info")
    var fwInfoList: ArrayList<FwInfo>? = null
    @SerializedName("sn")
    var sn: String? = null
}
