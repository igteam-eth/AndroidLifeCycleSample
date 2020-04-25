package com.ethernom.helloworld.model

data class DataModel(
    val sn: String,
    val mfg_id: String,
    val host_app: HostModel
)

data class HostModel (
    val company: String,
    val name: String,
    val app_id: String,
    val os: String,
    val version: String
)
