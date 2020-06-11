package com.ethernom.helloworld.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CheckUpdateRequestBody {

    @Expose
    @SerializedName("fw_info")
    var fwInfoList: ArrayList<FwInfo>? = null
    @Expose
    @SerializedName("host_app")
    var hostApp: HostApp? = null
    @Expose
    @SerializedName("mfg_id")
    var mfgId: String? = null
    @Expose
    @SerializedName("sn")
    var sn: String? = null

}
