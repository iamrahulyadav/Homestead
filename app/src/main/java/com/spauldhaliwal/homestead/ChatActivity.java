package com.spauldhaliwal.homestead;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity {
    //TODO extend BaseActivity, clean up code
    private static final String TAG = "ChatActivity";

    private ChatAdapter chatAdapter;
    Context mContext;

    RecyclerView messageListView;
    private LinearLayoutManager linearLayoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;

    private final List<MessageModel> messagesList = new ArrayList<>();

    private String homesteadId;

    Vibrator vibe;

    private static final int TOTAL_ITEMS_TO_ADD = 50;
    private int currentPage = 1;
//    private SwipeRefreshLayout swipeRefreshLayout;

    private int itemPos = 0;
    private String lastMessageKey;
    private String messageKey;
    private String prevMessageKey;
    LayoutInflater layoutInflater;

    ChildEventListener childEventListener;

    private int moreMessagesLoadedCount = 0;


//    SparseIntArray expandedItems = new SparseIntArray();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutInflater = getLayoutInflater();
        mContext = this;
        SharedPreferences sharedPref = this.getSharedPreferences("com.spauldhaliwal.homestead.SignInActivity.PREFERENCES_FILE_KEY",
                Context.MODE_PRIVATE);
        homesteadId = sharedPref.getString(UsersContract.HOMESTEAD_ID, null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Homestead Chat");
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        ImageButton sendButton = findViewById(R.id.chatSendButton);
        final EditText editMessage = findViewById(R.id.chatMessage);

        messageListView = findViewById(R.id.chatRecyclerView);
        messageListView.setHasFixedSize(true);


        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageListView.setLayoutManager(linearLayoutManager);
        messageListView.setItemAnimator(null);
        registerForContextMenu(messageListView);


//        ((SimpleItemAnimator) messageListView.getItemAnimator()).setSupportsChangeAnimations(false);

        chatAdapter = new ChatAdapter(messagesList, layoutInflater);

        messageListView.setAdapter(chatAdapter);

        //Endless scroll code
//        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//
//                currentPage++;
//                itemPos = 0;
//                loadMoreMessages();
//
//            }
//        };

//        messageListView.addOnScrollListener(scrollListener);
//

        loadMessages();

//        ItemClickSupport.addTo(messageListView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
//
//            @Override
//            public void onItemClicked(final RecyclerView recyclerView, final int position, View v) {
//
//                final ChatAdapter.MessageHolder holder = (ChatAdapter.MessageHolder) recyclerView
//                        .findViewHolderForAdapterPosition(position);
//
//                MessageModel message = (MessageModel) chatAdapter.getItem(position);
//                holder.setTimestamp(message.getTimeSent());
//
//                if (holder.getTimestamp().getVisibility() == View.GONE) {
//                    Transition fadeOut = new Fade().setStartDelay(60).setDuration(120);
//                    Transition expand = new ChangeBounds().setDuration(180);
//                    TransitionSet expandingAnimation = new TransitionSet()
//                            .addTransition(fadeOut)
//                            .addTransition(expand)
//                            .setOrdering(TransitionSet.ORDERING_TOGETHER);
//
//                    TransitionManager.beginDelayedTransition(recyclerView, expandingAnimation);
//                    holder.getTimestamp().setVisibility(View.VISIBLE);
////                    expandedItems.put(position, position);
//
//                } else {
//                    TransitionSet collapsingAnimation = new TransitionSet();
//                    collapsingAnimation.addTransition(new Fade().setDuration(90))
//                            .addTransition(new ChangeBounds().setDuration(180))
//                            .setOrdering(TransitionSet.ORDERING_TOGETHER);
//
//                    TransitionManager.beginDelayedTransition(recyclerView, collapsingAnimation);
//                    holder.getTimestamp().setVisibility(View.GONE);
////                    expandedItems.delete(position);
//                }
//
//            }
//        });

//        ItemClickSupport.addTo(messageListView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
//
//                Toast.makeText(mContext, "longClickListener activated.", Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            private static final String TAG = "ChatActivity";

            @Override
            public void onClick(View v) {
                String message = editMessage.getText().toString();

                if (FirebaseResolver.sendMessage(message,
                        CurrentUser.getUid(),
                        CurrentUser.getName(),
                        System.currentTimeMillis(),
                        CurrentUser.getProfileImage())) {
                    editMessage.setText("");
                    linearLayoutManager.smoothScrollToPosition(messageListView, null, chatAdapter.getItemCount() + 1);
                } else {
                    Toast.makeText(ChatActivity.this, "Message empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void loadMoreMessages() {
        Log.d(TAG, "loadMoreMessages: starts");
        DatabaseReference messageRef = FirebaseDatabase.getInstance()
                .getReference()
                .child(MessagesContract.ROOT_NODE)
                .child(homesteadId);

        Query messageQuery = messageRef
                .orderByKey()
                .endAt(lastMessageKey)
                .limitToLast(TOTAL_ITEMS_TO_ADD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                MessageModel message = dataSnapshot.getValue(MessageModel.class);

                String messageKey = dataSnapshot.getKey();

                if (!messageKey.equals("-000000000")) {
                    if (!prevMessageKey.equals(messageKey)) {
                        messagesList.add(itemPos++, message);
                    } else {
                        prevMessageKey = lastMessageKey;
                    }

                    if (itemPos == 1) {

                        lastMessageKey = messageKey;
                    }
                }


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                chatAdapter.notifyItemRangeChanged(0, chatAdapter.getItemCount());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void loadMessages() {

        DatabaseReference messageRef = FirebaseDatabase.getInstance()
                .getReference()
                .child(MessagesContract.ROOT_NODE)
                .child(homesteadId);

        Query messageQuery = messageRef.limitToLast(currentPage * TOTAL_ITEMS_TO_ADD);

        childEventListener = messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MessageModel message = dataSnapshot.getValue(MessageModel.class);

                itemPos++;

                if (itemPos == 1) {
                    String messageKey = dataSnapshot.getKey();
                    lastMessageKey = messageKey;
                    prevMessageKey = messageKey;

                }
                messagesList.add(message);
                currentPage++;

                chatAdapter.notifyDataSetChanged();
                linearLayoutManager.smoothScrollToPosition(messageListView, null, chatAdapter.getItemCount() + 1);
                Log.d(TAG, "loadMessages: messages loaded: " + linearLayoutManager.getItemCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menuChatItem).setEnabled(false).setVisible(false);
        invalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.chat_view;
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.chat_view_context_menu, menu);
//    }

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.chatViewContextItemCopyText:
//                Toast.makeText(mContext, "Copy Text Selected", Toast.LENGTH_SHORT).show();
//                return true;
//            default:
//                return super.onContextItemSelected(item);
//        }
//
//    }
}
