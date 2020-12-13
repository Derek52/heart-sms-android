package xyz.heart.sms.utils.swipe_to_dismiss.actions

import android.graphics.Color
import xyz.klinker.sms.R
import xyz.klinker.sms.adapter.conversation.ConversationListAdapter
import xyz.klinker.sms.shared.data.Settings

open class SwipeArchiveAction : BaseSwipeAction() {

    override fun getIcon() = R.drawable.ic_archive

    override fun getBackgroundColor(): Int {
        val set = Settings.mainColorSet

        return if (set.colorLight == Color.WHITE) {
            set.colorDark
        } else {
            set.colorLight
        }
    }

    override fun onPerform(listener: ConversationListAdapter, index: Int) {
        listener.archiveItem(index)
    }

}