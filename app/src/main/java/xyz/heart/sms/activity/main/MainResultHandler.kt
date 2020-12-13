package xyz.heart.sms.activity.main

import android.app.Activity
import android.content.Intent
import android.os.Handler
import androidx.fragment.app.Fragment
import xyz.klinker.sms.R
import xyz.klinker.sms.activity.MessengerActivity
import xyz.klinker.sms.fragment.PrivateConversationListFragment
import xyz.klinker.sms.fragment.message.attach.AttachmentListener
import xyz.klinker.sms.activity.passcode.PasscodeVerificationActivity
import xyz.klinker.sms.shared.data.Settings
import xyz.klinker.sms.shared.util.TimeUtils

class MainResultHandler(private val activity: MessengerActivity) {

    fun handle(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PasscodeVerificationActivity.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                activity.navController.conversationActionDelegate.displayFragmentWithBackStack(PrivateConversationListFragment())
                Settings.setValue(activity, activity.getString(R.string.pref_private_conversation_passcode_last_entry), TimeUtils.now)
            } else {
                activity.navController.onNavigationItemSelected(R.id.drawer_conversation)
            }

            return
        }

        var fragment: Fragment? = activity.supportFragmentManager.findFragmentById(R.id.message_list_container)
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data)
        } else {
            if (requestCode == AttachmentListener.RESULT_CAPTURE_IMAGE_REQUEST) {
                Handler().postDelayed({
                    val messageList = activity.supportFragmentManager.findFragmentById(R.id.message_list_container)
                    messageList?.onActivityResult(requestCode, resultCode, data)
                }, 1000)
            }

            fragment = activity.supportFragmentManager.findFragmentById(R.id.conversation_list_container)
            fragment?.onActivityResult(requestCode, resultCode, data)
        }
    }
}