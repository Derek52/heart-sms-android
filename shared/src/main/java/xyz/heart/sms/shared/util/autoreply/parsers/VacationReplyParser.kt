package xyz.heart.sms.shared.util.autoreply.parsers

import android.content.Context
import xyz.klinker.sms.shared.data.Settings
import xyz.klinker.sms.shared.data.model.AutoReply
import xyz.klinker.sms.shared.data.model.Conversation
import xyz.klinker.sms.shared.data.model.Message
import xyz.klinker.sms.shared.util.autoreply.AutoReplyParser

class VacationReplyParser(context: Context?, reply: AutoReply) : AutoReplyParser(context, reply) {

    override fun canParse(conversation: Conversation, message: Message) = Settings.vacationMode

}
