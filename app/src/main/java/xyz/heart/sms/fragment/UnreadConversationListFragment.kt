package xyz.heart.sms.fragment

import android.view.View
import com.google.android.material.navigation.NavigationView
import xyz.klinker.sms.R
import xyz.klinker.sms.activity.MessengerActivity
import xyz.klinker.sms.adapter.view_holder.ConversationViewHolder
import xyz.klinker.sms.fragment.conversation.ConversationListFragment

class UnreadConversationListFragment : ConversationListFragment() {

    // always consume the back event and send us to the conversation list
    override fun onBackPressed(): Boolean {
        if (!super.onBackPressed()) {
            val navView = activity?.findViewById<View>(R.id.navigation_view) as NavigationView?
            navView?.menu?.findItem(R.id.drawer_conversation)?.isChecked = true

            activity?.title = getString(R.string.app_title)
            (activity as MessengerActivity).displayConversations()
        }

        return true
    }

    override fun onConversationContracted(viewHolder: ConversationViewHolder) {
        super.onConversationContracted(viewHolder)

        val navView = activity?.findViewById<View>(R.id.navigation_view) as NavigationView?
        navView?.menu?.findItem(R.id.drawer_unread)?.isChecked = true
    }
}
