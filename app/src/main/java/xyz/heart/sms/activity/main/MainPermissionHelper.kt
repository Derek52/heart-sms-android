package xyz.heart.sms.activity.main

import xyz.klinker.sms.activity.MessengerActivity
import xyz.klinker.sms.api.implementation.Account
import xyz.klinker.sms.shared.MessengerActivityExtras
import xyz.klinker.sms.shared.util.PermissionsUtils

class MainPermissionHelper(private val activity: MessengerActivity) {

    fun requestPermissions() {
        if (PermissionsUtils.checkRequestMainPermissions(activity)) {
            PermissionsUtils.startMainPermissionRequest(activity)
        }
    }

    fun requestDefaultSmsApp() {
        if (Account.primary && !PermissionsUtils.isDefaultSmsApp(activity)) {
            PermissionsUtils.setDefaultSmsApp(activity)
        }
    }

    fun handlePermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        try {
            PermissionsUtils.processPermissionRequest(activity, requestCode, permissions, grantResults)
            if (requestCode == MessengerActivityExtras.REQUEST_CALL_PERMISSION) {
                activity.navController.messageActionDelegate.callContact()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}