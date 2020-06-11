package com.ethernom.helloworld.util

enum class StateMachine(val value: String) {

    INITIAL("0000"),
    CARD_DISCOVERY_BLE_LOCATION_OFF("1000"),
    CARD_DISCOVERY_BLE_LOCATION_ON("1001"),
    GET_FIRMWARE_INFO("1002"),
    CHECKING_UPDATE_FIRMWARE("1003"),
    GET_PRIVATE_KEY("1004"),
    CARD_REGISTER("1005"),
    VERIFY_PIN("1006"),
    WAITING_FOR_BEACON("2000"),
    WAITING_FOR_BEACON_BLE_OF_STATE("2001"),
    WAITING_FOR_BEACON_LOCATION_OF_STATE("2002"),
    WAITING_FOR_BEACON_BLE_AND_LOCATION_OF_STATE("2003");

}