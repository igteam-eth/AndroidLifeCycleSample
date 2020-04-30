package com.ethernom.helloworld.model

data class GetPrivateKeyReqBody(
    val sn: String,
    val mfg_id: String,
    val host_app: HostApp
)

