package xyz.klinker.messenger.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.wearable.view.WearableRecyclerView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xyz.klinker.messenger.R;
import xyz.klinker.messenger.adapter.WearableConversationListAdapter;
import xyz.klinker.messenger.api.implementation.Account;
import xyz.klinker.messenger.shared.MessengerActivityExtras;
import xyz.klinker.messenger.shared.data.DataSource;
import xyz.klinker.messenger.shared.data.model.Conversation;
import xyz.klinker.messenger.shared.receiver.ConversationListUpdatedReceiver;
import xyz.klinker.messenger.shared.shared_interfaces.IConversationListAdapter;
import xyz.klinker.messenger.shared.shared_interfaces.IConversationListFragment;
import xyz.klinker.messenger.shared.util.ColorUtils;
import xyz.klinker.messenger.shared.util.PermissionsUtils;
import xyz.klinker.messenger.shared.util.TimeUtils;
import xyz.klinker.messenger.util.CircularOffsettingHelper;

public class MessengerActivity extends AppCompatActivity implements IConversationListFragment {

    private WearableRecyclerView recyclerView;
    private WearableConversationListAdapter adapter;

    private ConversationListUpdatedReceiver updatedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Account account = Account.get(this);
        if (account.accountId == null) {
            startActivity(new Intent(this, InitialLoadWearActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_messenger);

        recyclerView = (WearableRecyclerView) findViewById(R.id.recycler_view);

        adapter = new WearableConversationListAdapter(new ArrayList<Conversation>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setOffsettingHelper(new CircularOffsettingHelper());

        loadConversations();
        displayConversations();

        updatedReceiver = new ConversationListUpdatedReceiver(this);
        registerReceiver(updatedReceiver,
                ConversationListUpdatedReceiver.getIntentFilter());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (updatedReceiver != null) {
            unregisterReceiver(updatedReceiver);
        }
    }
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        long convoId = intent.getLongExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, -1L);
        if (convoId != -1L) {
            getIntent().putExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, convoId);
            displayConversations();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        requestPermissions();
        ColorUtils.checkBlackBackground(this);
        TimeUtils.setupNightTheme(this);
    }

    private void requestPermissions() {
        if (PermissionsUtils.checkRequestMainPermissions(this)) {
            PermissionsUtils.startMainPermissionRequest(this);
        }

        if (Account.get(this).primary && !PermissionsUtils.isDefaultSmsApp(this)) {
            PermissionsUtils.setDefaultSmsApp(this);
        }
    }

    private void loadConversations() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataSource source = DataSource.getInstance(MessengerActivity.this);
                source.open();

                final List<Conversation> conversations = source.getUnarchivedConversationsAsList();

                source.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setConversations(conversations);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    public void displayConversations() {
        long convoId = getIntent().getLongExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, -1L);

        if (convoId != -1L) {
            MessageListActivity.startActivity(this, convoId);
        }
    }

    @Override
    public boolean isAdded() {
        return true;
    }

    @Override
    public long getExpandedId() {
        return -1; // don't ignore any, since this is a separate activity for wear
    }

    @Override
    public IConversationListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void checkEmptyViewDisplay() {

    }
}