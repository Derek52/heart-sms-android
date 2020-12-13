package xyz.heart.sms.shared.util

import android.content.Context
import xyz.klinker.sms.api.implementation.Account
import xyz.klinker.sms.api.implementation.ApiUtils
import xyz.klinker.sms.shared.R
import xyz.klinker.sms.shared.data.FeatureFlags
import xyz.klinker.sms.shared.data.MmsSettings
import xyz.klinker.sms.shared.data.Settings

object KotlinObjectInitializers {

    fun initializeObjects(context: Context) {
        try {
            ApiUtils.environment = context.getString(R.string.environment)
        } catch (e: Exception) {
            ApiUtils.environment = "release"
        }

        Account.init(context)
        FeatureFlags.init(context)
        Settings.init(context)
        MmsSettings.init(context)
        DualSimUtils.init(context)
        EmojiInitializer.initializeEmojiCompat(context)
    }
}