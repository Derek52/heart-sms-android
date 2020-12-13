package xyz.heart.sms.activity.share

import android.net.Uri
import xyz.klinker.sms.R
import xyz.klinker.sms.api.implementation.Account
import xyz.klinker.sms.shared.data.DataSource
import xyz.klinker.sms.shared.data.MimeType
import xyz.klinker.sms.shared.data.model.Message
import xyz.klinker.sms.shared.receiver.ConversationListUpdatedReceiver
import xyz.klinker.sms.shared.receiver.MessageListUpdatedReceiver
import xyz.klinker.sms.shared.util.DualSimUtils
import xyz.klinker.sms.shared.util.PhoneNumberUtils
import xyz.klinker.sms.shared.util.SendUtils
import xyz.klinker.sms.shared.util.TimeUtils
import xyz.klinker.sms.shared.widget.MessengerAppWidgetProvider

class ShareSender(private val page: QuickSharePage) {

    fun sendMessage(): Boolean {
        try {
            if (page.messageEntry.text.isEmpty() && page.mediaData == null) {
                return false
            }

            var conversationId: Long? = null

            val messageText = page.messageEntry.text.toString().trim { it <= ' ' }
            val phoneNumbers = page.contactEntry.recipients
                    .joinToString(", ") { PhoneNumberUtils.clearFormatting(it.entry.destination) }

            if (messageText.isNotEmpty()) {
                val textMessage = createMessage(messageText)
                conversationId = DataSource.insertMessage(textMessage, phoneNumbers, page.context)
            }

            if (page.mediaData != null) {
                val imageMessage = createMessage(page.mediaData!!)
                imageMessage.mimeType = page.mimeType!!
                conversationId = DataSource.insertMessage(imageMessage, phoneNumbers, page.context)
            }

            DataSource.readConversation(page.context, conversationId!!)
            val conversation = DataSource.getConversation(page.activity, conversationId)
                    ?: return false

            Thread {
                if (page.mediaData != null) {
                    SendUtils(conversation.simSubscriptionId).send(page.context, messageText, phoneNumbers, Uri.parse(page.mediaData!!), page.mimeType!!)
                } else {
                    SendUtils(conversation.simSubscriptionId).send(page.context, messageText, phoneNumbers)
                }

                //MarkAsSentJob.scheduleNextRun(page.context, messageId)
            }.start()

            ConversationListUpdatedReceiver.sendBroadcast(page.context, conversation.id, page.context.getString(R.string.you) + ": " + messageText, true)
            MessageListUpdatedReceiver.sendBroadcast(page.context, conversation.id)
            MessengerAppWidgetProvider.refreshWidget(page.context)

            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun createMessage(text: String, mimeType: String = MimeType.TEXT_PLAIN): Message {
        val message = Message()
        message.type = Message.TYPE_SENDING
        message.data = text
        message.timestamp = TimeUtils.now
        message.mimeType = mimeType
        message.read = true
        message.seen = true
        message.simPhoneNumber = if (DualSimUtils.availableSims.size > 1) DualSimUtils.defaultPhoneNumber else null
        message.sentDeviceId = if (Account.exists()) Account.deviceId!!.toLong() else -1L

        return message
    }
}