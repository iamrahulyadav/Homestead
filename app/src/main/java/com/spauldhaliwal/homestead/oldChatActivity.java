package com.spauldhaliwal.homestead;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.ArrayList;
import java.util.List;

public class oldChatActivity extends AppCompatActivity {
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

    Drawable userProfileDrawable;


    ChildEventListener childEventListener;

    private int moreMessagesLoadedCount = 0;


//    SparseIntArray expandedItems = new SparseIntArray();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mContext = this;
        SharedPreferences sharedPref = this.getSharedPreferences("com.spauldhaliwal.homestead.SignInActivity.PREFERENCES_FILE_KEY",
                Context.MODE_PRIVATE);
        homesteadId = sharedPref.getString(UsersContract.HOMESTEAD_ID, null);

        super.onCreate(savedInstanceState);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        setContentView(R.layout.chat_view);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Homestead Chat");
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        Log.d(TAG, "Toolbar onCreate: ChatActivity Toolbar: " + toolbar);

        ImageButton sendButton = findViewById(R.id.chatSendButton);
        final EditText editMessage = findViewById(R.id.chatMessage);

        messageListView = findViewById(R.id.chatRecyclerView);
        messageListView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageListView.setLayoutManager(linearLayoutManager);

        ((SimpleItemAnimator) messageListView.getItemAnimator()).setSupportsChangeAnimations(false);
        LayoutInflater layoutInflater = getLayoutInflater();
        chatAdapter = new ChatAdapter(messagesList, layoutInflater);
        messageListView.setAdapter(chatAdapter);

        //Set overflow icon to user's profile image
        Glide.with(this)
                .load(CurrentUser.getProfileImage())
                .apply(RequestOptions.circleCropTransform().override(80, 80))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                                                @Nullable com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                        userProfileDrawable = resource;
                        toolbar.setOverflowIcon(userProfileDrawable);
                    }
                });

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

        ItemClickSupport.addTo(messageListView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {

            @Override
            public void onItemClicked(final RecyclerView recyclerView, final int position, View v) {

                final ChatAdapter.MessageHolder holder = (ChatAdapter.MessageHolder) recyclerView
                        .findViewHolderForAdapterPosition(position);

                MessageModel message = (MessageModel) chatAdapter.getItem(position);
                holder.setTimestamp(message.getTimeSent());

                if (holder.getTimestamp().getVisibility() == View.GONE) {
                    Transition fadeOut = new Fade().setStartDelay(60).setDuration(120);
                    Transition expand = new ChangeBounds().setDuration(180);
                    TransitionSet expandingAnimation = new TransitionSet()
                            .addTransition(fadeOut)
                            .addTransition(expand)
                            .setOrdering(TransitionSet.ORDERING_TOGETHER);

                    TransitionManager.beginDelayedTransition(recyclerView, expandingAnimation);
                    holder.getTimestamp().setVisibility(View.VISIBLE);
//                    expandedItems.put(position, position);

                } else {
                    TransitionSet collapsingAnimation = new TransitionSet();
                    collapsingAnimation.addTransition(new Fade().setDuration(90))
                            .addTransition(new ChangeBounds().setDuration(180))
                            .setOrdering(TransitionSet.ORDERING_TOGETHER);

                    TransitionManager.beginDelayedTransition(recyclerView, collapsingAnimation);
                    holder.getTimestamp().setVisibility(View.GONE);
//                    expandedItems.delete(position);
                }

            }
        });

//        ItemClickSupport.addTo(messageListView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
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
                    Toast.makeText(oldChatActivity.this, "Message empty", Toast.LENGTH_SHORT).show();
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


//    @Override
//    protected void onStart() {
//        super.onStart();
//        chatAdapter.startListening();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        adapter.stopListening();
//    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                vibe.vibrate(2);
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
                return true;
            default:
                return super.onKeyLongPress(keyCode, event);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menuChatItem).setEnabled(false).setVisible(false);
        menu.findItem(R.id.menuTutorial).setEnabled(false).setVisible(false);
        invalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (id) {
            case (R.id.action_signout):
                AuthUI.getInstance().signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // User is now signed out
                                if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                                    ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE))
                                            .clearApplicationUserData(); // note: it has a return value!
                                } else {
                                    // use old hacky way, which can be removed
                                    // once minSdkVersion goes above 19 in a few years.
                                    Log.d(TAG, "onComplete: Application is pre kitkat.");
                                }
                                Intent intent = new Intent(mContext, MainActivity.class);
                                startActivity(intent);
                                finish();
                                recreate();

                            }
                        });
                return true;
                case android.R.id.home:
                    finish();
                    return true;
            case (R.id.menuTutorial):
                invalidateOptionsMenu();
                return true;
            case (R.id.menuInvite):
                String userId = CurrentUser.getUid();
                String homesteadId = CurrentUser.getHomesteadUid();
                String homesteadName = CurrentUser.getHomesteadName();
                String inviteTitle = "Join " + CurrentUser.getName() + "'s Homestead!";
                String inviteDescription = CurrentUser.getName() + " has invited you to join their homestead: " + CurrentUser.getHomesteadName() + ". Follow the link to accept their invitation!";
                Uri inviteImageUrl = Uri.parse(CurrentUser.getProfileImage());

                Task<ShortDynamicLink> dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse("https://homesteadapp.com/?homesteadid=" + homesteadId
                                + "&userid="
                                + userId))

                        .setDynamicLinkDomain("homesteadapp.page.link")
                        // Open links with this app on android
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                        // Open links with this app on iOS
                        .setIosParameters(new DynamicLink.IosParameters
                                .Builder("com.spauldhaliwal.homestead").build())
                        .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(inviteTitle)
                                .setDescription(inviteDescription)
                                .setImageUrl(inviteImageUrl)
                                .build())
                        .buildShortDynamicLink().addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                            @Override
                            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                if (task.isSuccessful()) {
                                    // Short Link created
                                    Uri shortDynamicLinkUri = task.getResult().getShortLink();
                                    Uri flowchartLink = task.getResult().getPreviewLink();

                                    Log.d(TAG, "onOptionsItemSelected: inviteUri: " + shortDynamicLinkUri);

                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Invite URL");
                                    intent.putExtra(Intent.EXTRA_TEXT, shortDynamicLinkUri.toString());
                                    startActivity(Intent.createChooser(intent, "Invite URL"));

                                } else {
                                    // Error
                                    // ...
                                }

                            }
                        });

                return true;
            case (R.id.menuChatItem):
                Intent intent = new Intent(this, oldChatActivity.class);
                startActivity(intent);
                return true;
            case (R.id.menuLeaveHomestead):
                new AlertDialog.Builder(this)
                        .setTitle("Leave Homestead")
                        .setMessage("Are you sure you want to leave this Homestead?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseResolver.leaveHomestead();

                                AuthUI.getInstance().signOut(mContext)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                // User is now signed out
                                                if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                                                    ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE))
                                                            .clearApplicationUserData(); // note: it has a return value!
                                                } else {
                                                    // use old hacky way, which can be removed
                                                    // once minSdkVersion goes above 19 in a few years.
                                                    Log.d(TAG, "onComplete: Application is pre kitkat.");
                                                }
                                                Intent intent = new Intent(mContext, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                                recreate();

                                            }
                                        });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityState.setActivity(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mContext = null;
        ActivityState.clearActivity(this);
    }
}
