package xyz.heart.sms.api.implementation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Base64

import java.util.Date

import javax.crypto.spec.SecretKeySpec

import xyz.heart.sms.encryption.EncryptionUtils
import xyz.heart.sms.encryption.KeyUtils

@SuppressLint("ApplySharedPref")
object Account {

    @JvmStatic
    val QUICK_SIGN_UP_SYSTEM = false

    enum class SubscriptionType constructor(var typeCode: Int) {
        TRIAL(1), SUBSCRIBER(2), LIFETIME(3), FREE_TRIAL(4), FINISHED_FREE_TRIAL_WITH_NO_ACCOUNT_SETUP(5);

        companion object {
            fun findByTypeCode(code: Int): _root_ide_package_.xyz.heart.sms.api.implementation.Account.SubscriptionType? {
                return values().firstOrNull { it.typeCode == code }
            }
        }
    }

    var encryptor: _root_ide_package_.xyz.heart.sms.encryption.EncryptionUtils? = null
        private set

    var primary: Boolean = false
    var trialStartTime: Long = 0
    var subscriptionType: _root_ide_package_.xyz.heart.sms.api.implementation.Account.SubscriptionType? = null
    var subscriptionExpiration: Long = 0
    var myName: String? = null
    var myPhoneNumber: String? = null
    var deviceId: String? = null
    var accountId: String? = null
    var salt: String? = null
    var passhash: String? = null
    var key: String? = null

    var hasPurchased: Boolean = false

    fun init(context: Context) {
        val sharedPrefs = _root_ide_package_.xyz.heart.sms.api.implementation.Account.getSharedPrefs(context)

        // account info
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.primary = sharedPrefs.getBoolean(context.getString(R.string.api_pref_primary), false)
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.subscriptionType = _root_ide_package_.xyz.heart.sms.api.implementation.Account.SubscriptionType.Companion.findByTypeCode(sharedPrefs.getInt(context.getString(R.string.api_pref_subscription_type), 1))
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.subscriptionExpiration = sharedPrefs.getLong(context.getString(R.string.api_pref_subscription_expiration), -1)
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.trialStartTime = sharedPrefs.getLong(context.getString(R.string.api_pref_trial_start), -1)
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.myName = sharedPrefs.getString(context.getString(R.string.api_pref_my_name), null)
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.myPhoneNumber = sharedPrefs.getString(context.getString(R.string.api_pref_my_phone_number), null)
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.deviceId = sharedPrefs.getString(context.getString(R.string.api_pref_device_id), null)
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.accountId = sharedPrefs.getString(context.getString(R.string.api_pref_account_id), null)
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.salt = sharedPrefs.getString(context.getString(R.string.api_pref_salt), null)
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.passhash = sharedPrefs.getString(context.getString(R.string.api_pref_passhash), null)
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.key = sharedPrefs.getString(context.getString(R.string.api_pref_key), null)

        _root_ide_package_.xyz.heart.sms.api.implementation.Account.hasPurchased = sharedPrefs.getBoolean(context.getString(R.string.api_pref_has_purchased), false)

        if (_root_ide_package_.xyz.heart.sms.api.implementation.Account.key == null && _root_ide_package_.xyz.heart.sms.api.implementation.Account.passhash != null && _root_ide_package_.xyz.heart.sms.api.implementation.Account.accountId != null && _root_ide_package_.xyz.heart.sms.api.implementation.Account.salt != null) {
            // we have all the requirements to recompute the key,
            // not sure why this wouldn't have worked in the first place..
            _root_ide_package_.xyz.heart.sms.api.implementation.Account.recomputeKey(context)
            _root_ide_package_.xyz.heart.sms.api.implementation.Account.key = sharedPrefs.getString(context.getString(R.string.api_pref_key), null)

            val secretKey = SecretKeySpec(Base64.decode(_root_ide_package_.xyz.heart.sms.api.implementation.Account.key, Base64.DEFAULT), "AES")
            _root_ide_package_.xyz.heart.sms.api.implementation.Account.encryptor = _root_ide_package_.xyz.heart.sms.encryption.EncryptionUtils(secretKey)
        } else if (_root_ide_package_.xyz.heart.sms.api.implementation.Account.key == null && _root_ide_package_.xyz.heart.sms.api.implementation.Account.accountId != null) {
            // we cannot compute the key, uh oh. lets just start up the login activity and grab them...
            // This will do little good if they are on the api utils and trying to send a message or
            // something, or receiving a message. But they will have to re-login sometime I guess
            context.startActivity(Intent(context, _root_ide_package_.xyz.heart.sms.api.implementation.LoginActivity::class.java))
        } else if (_root_ide_package_.xyz.heart.sms.api.implementation.Account.key != null) {
            val secretKey = SecretKeySpec(Base64.decode(_root_ide_package_.xyz.heart.sms.api.implementation.Account.key, Base64.DEFAULT), "AES")
            _root_ide_package_.xyz.heart.sms.api.implementation.Account.encryptor = _root_ide_package_.xyz.heart.sms.encryption.EncryptionUtils(secretKey)
        }

        val application = context.applicationContext
        if (application is _root_ide_package_.xyz.heart.sms.api.implementation.AccountInvalidator) {
            application.onAccountInvalidated(_root_ide_package_.xyz.heart.sms.api.implementation.Account)
        }
    }

    fun getSharedPrefs(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    }

    fun forceUpdate(context: Context): _root_ide_package_.xyz.heart.sms.api.implementation.Account {
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.init(context)
        return _root_ide_package_.xyz.heart.sms.api.implementation.Account
    }

    fun clearAccount(context: Context) {
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.getSharedPrefs(context).edit()
                .remove(context.getString(R.string.api_pref_account_id))
                .remove(context.getString(R.string.api_pref_salt))
                .remove(context.getString(R.string.api_pref_passhash))
                .remove(context.getString(R.string.api_pref_key))
                .remove(context.getString(R.string.api_pref_subscription_type))
                .remove(context.getString(R.string.api_pref_subscription_expiration))
                .commit()

        _root_ide_package_.xyz.heart.sms.api.implementation.Account.init(context)
    }

    fun updateSubscription(context: Context, type: _root_ide_package_.xyz.heart.sms.api.implementation.Account.SubscriptionType, expiration: Date?) {
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.updateSubscription(context, type, expiration?.time, true)
    }

    fun updateSubscription(context: Context, type: _root_ide_package_.xyz.heart.sms.api.implementation.Account.SubscriptionType?, expiration: Long?, sendToApi: Boolean) {
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.subscriptionType = type
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.subscriptionExpiration = expiration!!

        _root_ide_package_.xyz.heart.sms.api.implementation.Account.getSharedPrefs(context).edit()
                .putInt(context.getString(R.string.api_pref_subscription_type), type?.typeCode ?: 0)
                .putLong(context.getString(R.string.api_pref_subscription_expiration), expiration)
                .commit()

        if (sendToApi) {
            _root_ide_package_.xyz.heart.sms.api.implementation.ApiUtils.updateSubscription(_root_ide_package_.xyz.heart.sms.api.implementation.Account.accountId, type?.typeCode, expiration)
        }
    }

    fun setName(context: Context, name: String?) {
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.myName = name

        _root_ide_package_.xyz.heart.sms.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(R.string.api_pref_my_name), name)
                .commit()
    }

    fun setPhoneNumber(context: Context, phoneNumber: String?) {
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.myPhoneNumber = phoneNumber

        _root_ide_package_.xyz.heart.sms.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(R.string.api_pref_my_name), phoneNumber)
                .commit()
    }

    fun setPrimary(context: Context, primary: Boolean) {
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.primary = primary

        _root_ide_package_.xyz.heart.sms.api.implementation.Account.getSharedPrefs(context).edit()
                .putBoolean(context.getString(R.string.api_pref_primary), primary)
                .commit()
    }

    fun setDeviceId(context: Context, deviceId: String?) {
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.deviceId = deviceId

        _root_ide_package_.xyz.heart.sms.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(R.string.api_pref_device_id), deviceId)
                .commit()
    }

    fun setHasPurchased(context: Context, hasPurchased: Boolean) {
        _root_ide_package_.xyz.heart.sms.api.implementation.Account.hasPurchased = hasPurchased

        _root_ide_package_.xyz.heart.sms.api.implementation.Account.getSharedPrefs(context).edit()
                .putBoolean(context.getString(R.string.api_pref_has_purchased), hasPurchased)
                .commit()
    }

    fun recomputeKey(context: Context) {
        val keyUtils = _root_ide_package_.xyz.heart.sms.encryption.KeyUtils()
        val key = keyUtils.createKey(_root_ide_package_.xyz.heart.sms.api.implementation.Account.passhash, _root_ide_package_.xyz.heart.sms.api.implementation.Account.accountId, _root_ide_package_.xyz.heart.sms.api.implementation.Account.salt)

        val encodedKey = Base64.encodeToString(key.encoded, Base64.DEFAULT)

        _root_ide_package_.xyz.heart.sms.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(R.string.api_pref_key), encodedKey)
                .commit()
    }

    fun exists(): Boolean {
        return _root_ide_package_.xyz.heart.sms.api.implementation.Account.accountId != null && !_root_ide_package_.xyz.heart.sms.api.implementation.Account.accountId!!.isEmpty() && _root_ide_package_.xyz.heart.sms.api.implementation.Account.deviceId != null && _root_ide_package_.xyz.heart.sms.api.implementation.Account.salt != null && _root_ide_package_.xyz.heart.sms.api.implementation.Account.passhash != null
                && _root_ide_package_.xyz.heart.sms.api.implementation.Account.key != null
    }

    private const val TRIAL_LENGTH = 7 // days
    fun getDaysLeftInTrial(): Int {
        return if (_root_ide_package_.xyz.heart.sms.api.implementation.Account.subscriptionType == _root_ide_package_.xyz.heart.sms.api.implementation.Account.SubscriptionType.FREE_TRIAL) {
            val now = Date().time
            val timeInTrial = now - _root_ide_package_.xyz.heart.sms.api.implementation.Account.trialStartTime
            val trialLength = 1000 * 60 * 60 * 24 * _root_ide_package_.xyz.heart.sms.api.implementation.Account.TRIAL_LENGTH
            if (timeInTrial > trialLength) {
                0
            } else {
                val timeLeftInTrial = trialLength - timeInTrial
                val timeInDays = (timeLeftInTrial / (1000 * 60 * 60 * 24)) + 1
                timeInDays.toInt()
            }
        } else {
            0
        }
    }
}
