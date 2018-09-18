package com.spauldhaliwal.homestead;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.widget.Space;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.WEEK_IN_MILLIS;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ChatAdapter";

    private int mExpandedPosition = -1;
    private int mPreviousExpandedPosition = -1;
    private int mSelectedPosition = -1;
    private int mPreviousSelectedPosition = -1;

    //TODO Add VIEW_TYPE_MESSAGE_FIRST_MESSAGE_INCOMING_ISOLATED viewType to hide profile image of grouped messages

    private static final int VIEW_TYPE_MESSAGE_FIRST_MESSAGE_INCOMING = 0;
    private static final int VIEW_TYPE_MESSAGE_FIRST_MESSAGE_OUTGOING = 1;
    private static final int VIEW_TYPE_MESSAGE_INCOMING = 2;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER = 3;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_SURROUNDED = 4;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST = 5;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST_FINAL = 6;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_ISOLATED = 7;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_ISOLATED_FINAL = 8;
    private static final int VIEW_TYPE_MESSAGE_OUTGOING = 9;
    private static final int VIEW_TYPE_MESSAGE_OUTGOING_SAME_SENDER = 10;


    private static final int VIEW_TYPE_MESSAGE_HOMESTEAD_FIRST = -2;
    private static final int VIEW_TYPE_MESSAGE_HOMESTEAD = -1;

    private Set<Integer> incomingMessages = new HashSet<Integer>(Arrays.asList(VIEW_TYPE_MESSAGE_FIRST_MESSAGE_INCOMING,
            VIEW_TYPE_MESSAGE_INCOMING,
            VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER,
            VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_SURROUNDED,
            VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST,
            VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST_FINAL,
            VIEW_TYPE_MESSAGE_INCOMING_ISOLATED,
            VIEW_TYPE_MESSAGE_INCOMING_ISOLATED_FINAL));

    private Set<Integer> outgoingMessages = new HashSet<Integer>(Arrays.asList(VIEW_TYPE_MESSAGE_FIRST_MESSAGE_OUTGOING,
            VIEW_TYPE_MESSAGE_OUTGOING,
            VIEW_TYPE_MESSAGE_OUTGOING_SAME_SENDER));

    private String homesteadMessageId = HomesteadsContract.HOMESTEAD_MESSAGES_ID;

    private Set<Integer> homesteadMessages = new HashSet<Integer>(Arrays.asList(VIEW_TYPE_MESSAGE_HOMESTEAD, VIEW_TYPE_MESSAGE_HOMESTEAD_FIRST));

    private List<MessageModel> messagesList;
    private LayoutInflater layoutInflater;

    public ChatAdapter(List<MessageModel> messagesList, LayoutInflater layoutInflater) {
        this.messagesList = messagesList;
        this.layoutInflater = layoutInflater;
    }

    static RecyclerView mRecyclerView;

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType: ChatAdapter being used");

        MessageModel model = (MessageModel) this.getItem(position);

        MessageModel lastMessage;
        MessageModel nextMessage;

        if (position >= 1) {
            lastMessage = (MessageModel) this.getItem(position - 1);

        } else {
            lastMessage = (MessageModel) this.getItem(position);
        }

        if (position == this.getItemCount() - 1) {
            nextMessage = (MessageModel) this.getItem(position);
        } else {
            nextMessage = (MessageModel) this.getItem(position + 1);

        }
        if (position == 0
                && model.getSenderUid().equals(homesteadMessageId)) {
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_HOMESTEAD_FIRST);
            return VIEW_TYPE_MESSAGE_HOMESTEAD_FIRST;
        } else if (model.getSenderUid().equals(homesteadMessageId)) {
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_HOMESTEAD);
            return VIEW_TYPE_MESSAGE_HOMESTEAD;
        } else if (position == 0
                && model.getSenderUid().equals(CurrentUser.getUid())) {
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_FIRST_MESSAGE_OUTGOING);

            return VIEW_TYPE_MESSAGE_FIRST_MESSAGE_OUTGOING;
        } else if (position == 0
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_FIRST_MESSAGE_INCOMING);
            return VIEW_TYPE_MESSAGE_FIRST_MESSAGE_INCOMING;
        } else if ((position == this.getItemCount() - 1)
                && !lastMessage.getSenderUid().equals(model.getSenderUid())
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_INCOMING_ISOLATED_FINAL);
            return VIEW_TYPE_MESSAGE_INCOMING_ISOLATED_FINAL;

        } else if (!model.getSenderUid().equals(nextMessage.getSenderUid())
                && !lastMessage.getSenderUid().equals(model.getSenderUid())
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_INCOMING_ISOLATED);
            return VIEW_TYPE_MESSAGE_INCOMING_ISOLATED;

        } else if ((position == this.getItemCount() - 1)
                && lastMessage.getSenderUid().equals(model.getSenderUid())
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST_FINAL);
            return VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST_FINAL;

        } else if (model.getSenderUid().equals(nextMessage.getSenderUid())
                && lastMessage.getSenderUid().equals(model.getSenderUid())
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_SURROUNDED);
            return VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_SURROUNDED;

        } else if (!model.getSenderUid().equals(nextMessage.getSenderUid())
                && lastMessage.getSenderUid().equals(model.getSenderUid())
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST);
            return VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST;

        } else if (!model.getSenderUid().equals(CurrentUser.getUid())
                && lastMessage.getSenderUid().equals(model.getSenderUid())) {
            // messageSender is someone else but also sender of last message
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER);
            return VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER;

        } else if (!model.getSenderUid().equals(CurrentUser.getUid())) {
            // Message sender is someone else
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_INCOMING);
            return VIEW_TYPE_MESSAGE_INCOMING;

        } else if (model.getSenderUid().equals(CurrentUser.getUid())
                && lastMessage.getSenderUid().equals(model.getSenderUid())) {
            // Current user is the sender of the message but not the last message
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_OUTGOING_SAME_SENDER);
            return VIEW_TYPE_MESSAGE_OUTGOING_SAME_SENDER;

        } else if (model.getSenderUid().equals(CurrentUser.getUid())) {
            // Current user is the sender of the message but not the last message
            Log.d(TAG, "getItemViewType: " + VIEW_TYPE_MESSAGE_OUTGOING);
            return VIEW_TYPE_MESSAGE_OUTGOING;

        } else {
            return 0;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new instance of the ViewHolder

        if (incomingMessages.contains(viewType)) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_message_incoming, parent, false);
            return new IncomingMessageHolder(view);
        } else if (outgoingMessages.contains(viewType)) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_message_outgoing, parent, false);
            return new OutgoingMessageHolder(view);
        } else if (homesteadMessages.contains(viewType)) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_message_utility, parent, false);
            return new UtilityMessageHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        // Bind the Chat object to the ChatHolder
        // ...
//        Log.d(TAG, "onBindViewHolder: starts with: " + ((MessageHolder) holder).message.getText());


        final MessageModel model = (MessageModel) this.getItem(position);
        if (!model.getSenderUid().equals(homesteadMessageId)) {
            final MessageHolder messageHolder = (MessageHolder) holder;

            final boolean isExpanded = position == mExpandedPosition;
            final boolean isSelected = position == mSelectedPosition;

            ((MessageHolder) holder).timestamp.setVisibility(isExpanded ?
                    View.VISIBLE :
                    View.GONE);

            if (incomingMessages.contains(holder.getItemViewType())) {
                messageHolder.message.setBackgroundResource(isSelected ?
                        R.drawable.message_incoming_selected :
                        R.drawable.message_incoming);

            } else if (outgoingMessages.contains(holder.getItemViewType())) {

                messageHolder.message.setBackgroundResource(isSelected ?
                        R.drawable.message_outgoing_selected :
                        R.drawable.message_outgoing);
            }

            holder.itemView.setActivated(isExpanded);

            if (isExpanded)
                mPreviousExpandedPosition = position;
            if (isSelected)
                mPreviousSelectedPosition = position;

            ((MessageHolder) holder).message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Select onDismiss: mSelectedPosition = " + mSelectedPosition
                            + " isSelected = " + isSelected
                            + " position = " + position);
                    TransitionManager.endTransitions(mRecyclerView);
                    Transition fadeOut = new Fade().setStartDelay(90).setDuration(120);
                    Transition expand = new ChangeBounds().setDuration(225);
                    TransitionSet expandingAnimation = new TransitionSet()
                            .addTransition(fadeOut)
                            .addTransition(expand)
                            .setOrdering(TransitionSet.ORDERING_TOGETHER);
                    TransitionManager.beginDelayedTransition(mRecyclerView, expandingAnimation);
                    mExpandedPosition = isExpanded ? -1 : position;
                    mSelectedPosition = isSelected ? -1 : position;

                    notifyItemChanged(mPreviousExpandedPosition);
                    notifyItemChanged(mPreviousSelectedPosition);
                    notifyItemChanged(position);

                }
            });

            messageHolder.message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "Select onDismiss: mSelectedPosition = " + mSelectedPosition
                            + " isSelected = " + isSelected
                            + " position = " + position);

                    mSelectedPosition = isSelected ? -1 : position;
//                notifyItemChanged(mPreviousSelectedPosition);
                    notifyItemChanged(position);
                    BottomSheetDialog bottomSheetDialog =
                            showBottomSheet(((MessageHolder) holder));

                    bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Log.d(TAG, "Select onDismiss: mSelectedPosition = " + mSelectedPosition
                                    + " isSelected = " + isSelected
                                    + " position = " + position);

                            mSelectedPosition = isSelected ? position : -1;
                            notifyItemChanged(position);
                        }
                    });
                    Log.d(TAG, "Select onDismiss timestamp visibility set to GONE: ");
                    ((MessageHolder) holder).timestamp.setVisibility(View.GONE);
                    return true;
                }

            });
        } else if (model.getSenderUid().equals(homesteadMessageId)) {
            final View actionView = ((UtilityMessageHolder) holder).actionView;
            actionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPref = actionView.getContext().getSharedPreferences("com.spauldhaliwal.homestead.SignInActivity.PREFERENCES_FILE_KEY",
                            Context.MODE_PRIVATE);
                    String homesteadId = sharedPref.getString(UsersContract.HOMESTEAD_ID, null);
                    Log.d(TAG, "onClick: model.getAttachment " + model.getAttachments());
                    DatabaseReference jobRef = FirebaseDatabase.getInstance()
                            .getReference()
                            .child(HomesteadsContract.ROOT_NODE)
                            .child(homesteadId)
                            .child(JobsContract.ROOT_NODE)
                            //temporary value, model classes need to be remodeled to include jobId.
                            .child(model.getAttachments().getPayload());

                    jobRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            JobModel jobModel = dataSnapshot.getValue(JobModel.class);
                            Log.d(TAG, "onDataChange: jobModel = " + jobModel);

                            Intent intent = new Intent(holder.itemView.getContext(), newAddEditActivity.class);
                            intent.putExtra(JobsContract.ROOT_NODE, jobModel);
                            actionView.getContext().startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
        }


        String message;

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_HOMESTEAD:
                Log.d(TAG, "onBindViewHolder: " + VIEW_TYPE_MESSAGE_HOMESTEAD);
                String title = model.getSenderName() + " added a new task to the Homestead";
                String body = model.getMessage();
                ((UtilityMessageHolder) holder).setMessageTitle(title);
                ((UtilityMessageHolder) holder).setMessageBody(body);
                ((UtilityMessageHolder) holder).setMessageTimestamp(model.getTimeSent());
                break;
            case VIEW_TYPE_MESSAGE_HOMESTEAD_FIRST:
                Log.d(TAG, "onBindViewHolder: " + VIEW_TYPE_MESSAGE_HOMESTEAD_FIRST);
                title = model.getSenderName() + " added a new task to the Homestead";
                body = model.getMessage();
                ((UtilityMessageHolder) holder).setMessageTitle(title);
                ((UtilityMessageHolder) holder).setMessageBody(body);
                ((UtilityMessageHolder) holder).setMessageTimestamp(model.getTimeSent());
                ((UtilityMessageHolder) holder).utilityMessageTopSpacer.setVisibility(View.INVISIBLE);
                break;
            case VIEW_TYPE_MESSAGE_FIRST_MESSAGE_INCOMING:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
//                ((MessageHolder) holder).incommingMessagePadding.setVisibility(View.INVISIBLE);
                ((IncomingMessageHolder) holder).messageTopSpacer.setVisibility(View.INVISIBLE);
                ((IncomingMessageHolder) holder).setSenderName(model.getSenderName());
                ((IncomingMessageHolder) holder).setProfileImage(model.getProfileImage());
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();
                break;
            case VIEW_TYPE_MESSAGE_INCOMING_ISOLATED_FINAL:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).setSenderName(model.getSenderName());
                ((IncomingMessageHolder) holder).setProfileImage(model.getProfileImage());
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

//                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
//                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
//                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
//                    emojisOnly.setTextSize(30);
//                    emojisOnly.setText(message);
//                    emojisOnly.setPadding(16, 0, 0, 0);
//                    emojisOnly.setBackgroundColor(80000000);
//                } else {
//                    ((IncomingMessageHolder) holder).setContent(model.getMessage());
//                }
                break;
            case VIEW_TYPE_MESSAGE_INCOMING_ISOLATED:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).setSenderName(model.getSenderName());
                ((IncomingMessageHolder) holder).setProfileImage(model.getProfileImage());
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());

                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

//                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
//                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
//                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
//                    emojisOnly.setTextSize(30);
//                    emojisOnly.setText(message);
//                    emojisOnly.setPadding(16, 0, 0, 0);
//                    emojisOnly.setBackgroundColor(80000000);
//                } else {
//                    ((IncomingMessageHolder) holder).setContent(model.getMessage());
//                }
                break;
            case VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST_FINAL:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).setProfileImage(model.getProfileImage());
                ((IncomingMessageHolder) holder).getSenderName().setVisibility(View.GONE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());

                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

//                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
//                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
//                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
//                    emojisOnly.setTextSize(30);
//                    emojisOnly.setText(message);
//                    emojisOnly.setPadding(16, 0, 0, 0);
//                    emojisOnly.setBackgroundColor(80000000);
//                } else {
//                    ((IncomingMessageHolder) holder).setContent(model.getMessage());
//                }
                break;
            case VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).setProfileImage(model.getProfileImage());
                ((IncomingMessageHolder) holder).getSenderName().setVisibility(View.GONE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

//                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
//                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
//                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
//                    emojisOnly.setTextSize(30);
//                    emojisOnly.setText(message);
//                    emojisOnly.setPadding(16, 0, 0, 0);
//                    emojisOnly.setBackgroundColor(80000000);
//                } else {
//                    ((IncomingMessageHolder) holder).setContent(model.getMessage());
//                }
                break;
            case VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_SURROUNDED:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).getSenderName().setVisibility(View.GONE);
                ((IncomingMessageHolder) holder).getProfileImage().setVisibility(View.INVISIBLE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

//                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
//                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
//                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
//                    emojisOnly.setTextSize(30);
//                    emojisOnly.setText(message);
//                    emojisOnly.setPadding(16, 0, 0, 0);
//                    emojisOnly.setBackgroundColor(80000000);
//                } else {
//                    ((IncomingMessageHolder) holder).setContent(model.getMessage());
//                }
                break;
            case VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).setSenderName(model.getSenderName());
                ((IncomingMessageHolder) holder).getProfileImage().setVisibility(View.INVISIBLE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

//                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
//                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
//                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
//                    emojisOnly.setTextSize(30);
//                    emojisOnly.setText(message);
//                    emojisOnly.setPadding(16, 0, 0, 0);
//                    emojisOnly.setBackgroundColor(80000000);
//                } else {
//                    ((IncomingMessageHolder) holder).setContent(model.getMessage());
//                }
                break;
            case VIEW_TYPE_MESSAGE_INCOMING:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).setSenderName(model.getSenderName());
                ((IncomingMessageHolder) holder).getProfileImage().setVisibility(View.INVISIBLE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

//                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
//                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
//                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
//                    emojisOnly.setTextSize(30);
//                    emojisOnly.setText(message);
//                    emojisOnly.setPadding(16, 0, 0, 0);
//                    emojisOnly.setBackgroundColor(80000000);
//                } else {
//                    ((IncomingMessageHolder) holder).setContent(model.getMessage());
//                }
                break;
            case VIEW_TYPE_MESSAGE_FIRST_MESSAGE_OUTGOING:
                ((OutgoingMessageHolder) holder).setMessage(model.getMessage());
                ((OutgoingMessageHolder) holder).incommingMessagePadding.setVisibility(View.INVISIBLE);
                ((OutgoingMessageHolder) holder).messageTopSpacer.setVisibility(View.INVISIBLE);

                ((OutgoingMessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();
                break;
            case VIEW_TYPE_MESSAGE_OUTGOING:
                ((OutgoingMessageHolder) holder).setMessage(model.getMessage());
                ((OutgoingMessageHolder) holder).incommingMessagePadding.setVisibility(View.INVISIBLE);
                ((OutgoingMessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

//                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
//                    TextView emojisOnly = ((OutgoingMessageHolder) holder).message;
//                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
//                    emojisOnly.setTextSize(30);
//                    emojisOnly.setText(message);
//                    emojisOnly.setPadding(0, 0, 16, 0);
//                    emojisOnly.setBackgroundColor(80000000);
//                } else {
//                    ((OutgoingMessageHolder) holder).setContent(model.getMessage());
//                }

                break;
            case VIEW_TYPE_MESSAGE_OUTGOING_SAME_SENDER:
                ((OutgoingMessageHolder) holder).setMessage(model.getMessage());
                ((OutgoingMessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

//                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
//                    TextView emojisOnly = ((OutgoingMessageHolder) holder).message;
//                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
//                    emojisOnly.setTextSize(30);
//                    emojisOnly.setText(message);
//                    emojisOnly.setPadding(0, 0, 16, 0);
//                    emojisOnly.setBackgroundColor(80000000);
//                } else {
//                    ((OutgoingMessageHolder) holder).setContent(model.getMessage());
//                }
                break;

        }

    }


    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    static class MessageHolder extends RecyclerView.ViewHolder {
        //            implements View.OnClickListener, View.OnLongClickListener
        private static final String TAG = "MessageHolder";

        protected TextView message;
        protected TextView senderName;
        protected ImageView profileImage;
        protected boolean isSelected;

        protected TextView timestamp;
        protected Space incommingMessagePadding;
        protected Space messageTopSpacer;
        protected final TextView messageBackground;


        MessageHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.messageIncomingView);
            senderName = itemView.findViewById(R.id.messageSenderName);
            timestamp = itemView.findViewById(R.id.messageTimeStamp);
            profileImage = itemView.findViewById(R.id.messageSenderProfileImage);
            incommingMessagePadding = itemView.findViewById(R.id.incommingMessagePadding);
            messageTopSpacer = itemView.findViewById(R.id.ougoingMessageTopSpacer);
            messageBackground = itemView.findViewById(R.id.messageBackground);
        }


        public void setMessage(String n) {
            message.setText(n);
        }

        public TextView getMessage() {
            return message;
        }

        public TextView getSenderName() {
            return senderName;
        }

        public ImageView getProfileImage() {
            return profileImage;
        }

        public void setTimestamp(TextView timestamp) {
            this.timestamp = timestamp;
        }

        public Space getIncommingMessagePadding() {
            return incommingMessagePadding;
        }

        public void setIncommingMessagePadding(Space incommingMessagePadding) {
            this.incommingMessagePadding = incommingMessagePadding;
        }

        public TextView getMessageBackground() {
            return messageBackground;
        }

        public TextView getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long time) {
            String timeSent = DateUtils.getRelativeDateTimeString(timestamp.getContext(),
                    time,
                    MINUTE_IN_MILLIS,
                    WEEK_IN_MILLIS,
                    0).toString();
            timestamp.setText(timeSent);
        }

//        @Override
//        public void onClick(View v) {
//
//            ChangeBounds changeBounds = new ChangeBounds();
//            changeBounds.setResizeClip(false);
//
//            if (getTimestamp().getVisibility() == View.GONE) {
//                TransitionManager.endTransitions(mRecyclerView);
//                Transition fadeOut = new Fade().setStartDelay(90).setDuration(120);
//                Transition expand = new ChangeBounds().setDuration(225);
//                TransitionSet expandingAnimation = new TransitionSet()
//                        .addTransition(fadeOut)
//                        .addTransition(expand)
//                        .setOrdering(TransitionSet.ORDERING_TOGETHER);
//                TransitionManager.beginDelayedTransition(mRecyclerView, expandingAnimation);
//                getTimestamp().setVisibility(View.VISIBLE);
////                isSelected = true;
//
//            } else {
//                TransitionManager.endTransitions(mRecyclerView);
//                TransitionSet collapsingAnimation = new TransitionSet();
//                collapsingAnimation.addTransition(new Fade().setDuration(90))
//                        .addTransition(new ChangeBounds().setDuration(225))
//                        .setOrdering(TransitionSet.ORDERING_TOGETHER);
//
//                TransitionManager.beginDelayedTransition(mRecyclerView, collapsingAnimation);
//                getTimestamp().setVisibility(View.GONE);
//                message.clearFocus();
//
////                isSelected = false;
//            }
//
//
//        }
//
//
//        @Override
//        public boolean onLongClick(View v) {
//            Toast.makeText(itemView.getContext(), message.getText(), Toast.LENGTH_SHORT).show();
//            return true;
//        }
    }


    static class IncomingMessageHolder extends MessageHolder {
        private static final String TAG = "TaskViewHolder";

        IncomingMessageHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "TaskViewHolder: starts");

            message = itemView.findViewById(R.id.messageIncomingView);
            senderName = itemView.findViewById(R.id.messageSenderName);
            timestamp = itemView.findViewById(R.id.messageTimeStamp);
            profileImage = itemView.findViewById(R.id.messageSenderProfileImage);
            messageTopSpacer = itemView.findViewById(R.id.incomingMessageTopSpacer);
//
//            message.setOnClickListener(this);
//            message.setOnLongClickListener(this);

        }

        public TextView getMessage() {
            return message;
        }

        public TextView getSenderName() {
            return senderName;
        }

        public TextView getTimestamp() {
            return timestamp;
        }

        public ImageView getProfileImage() {
            return profileImage;
        }

        public void setMessage(String n) {
            message.setText(n);
        }

        public void setSenderName(String n) {
            senderName.setText(n);
        }

        public void setProfileImage(String d) {

            Glide.with(itemView.getContext())
                    .load(Uri.parse(d))
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImage);

        }

//        @Override
//        public boolean onLongClick(View v) {
//            super.onClick(v);
//            if (message.hasFocus()) {
//                message.setTextColor(message.getContext().getResources().getColor(R.color.messageTextSelected));
//                message.setBackgroundResource(R.drawable.message_incoming_selected);
//                return true;
//            } else {
//                message.setTextColor(message.getContext().getResources().getColor(R.color.messageText));
//                message.setBackgroundResource(R.drawable.message_incoming);
//                return true;
//            }
//        }
    }

    static class OutgoingMessageHolder extends MessageHolder {
        private static final String TAG = "TaskViewHolder";

//        private final TextView message;

        OutgoingMessageHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "TaskViewHolder: starts");
            message = itemView.findViewById(R.id.messageOutgoingView);

//            message.setOnClickListener(this);
//            message.setOnLongClickListener(this);
        }

        public TextView getMessage() {
            return message;
        }

        public void setMessage(String n) {
            message.setText(n);
        }

    }

    static class UtilityMessageHolder extends MessageHolder {
        TextView messageTitle;
        TextView messageBody;
        TextView messageTimestamp;
        View actionView;
        Space utilityMessageTopSpacer;

        public UtilityMessageHolder(View itemView) {
            super(itemView);
            messageTitle = itemView.findViewById(R.id.utitlityMessageTitle);
            messageBody = itemView.findViewById(R.id.utilityMessageBody);
            messageTimestamp = itemView.findViewById(R.id.utilityMessageTimeStamp);
            actionView = itemView.findViewById(R.id.utilityMessageActionView);
            utilityMessageTopSpacer = itemView.findViewById(R.id.utilityMessageTopSpacer);
        }

        public TextView getMessageTitle() {
            return messageTitle;
        }

        public void setMessageTitle(String n) {
            messageTitle.setText(n);
        }

        public TextView getMessageBody() {
            return messageBody;
        }

        public void setMessageBody(String n) {
            messageBody.setText(n);
        }

        public void setMessageTimestamp(long time) {
            String timeSent = DateUtils.getRelativeDateTimeString(messageTimestamp.getContext(),
                    time,
                    MINUTE_IN_MILLIS,
                    WEEK_IN_MILLIS,
                    0).toString();
            messageTimestamp.setText(timeSent);
        }

        public TextView getMessageTimestamp() {
            return messageTimestamp;
        }

        public View getActionView() {
            return actionView;
        }

        public void setActionView(View actionView) {
            this.actionView = actionView;
        }
    }

    public MessageModel getItem(int position) {
        return messagesList.get(position);
    }

    public BottomSheetDialog showBottomSheet(final MessageHolder messageHolder) {
        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(layoutInflater.getContext());
        View sheetView = layoutInflater.inflate(R.layout.chat_view_bottom_sheet, null);
//        mBottomSheetDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();


        final String messageText = (String) messageHolder.message.getText();
        final String messageLabel = "Message Text";

        ConstraintLayout copy = sheetView.findViewById(R.id.chat_view_bottom_sheet_copy);
        ConstraintLayout share = sheetView.findViewById(R.id.chat_view_bottom_sheet_share);

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) messageHolder
                        .itemView
                        .getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData clip = ClipData.newPlainText(messageLabel, messageText);
                clipboardManager.setPrimaryClip(clip);

                Toast.makeText(messageHolder.itemView.getContext(), "Copied", Toast.LENGTH_SHORT).show();
                mBottomSheetDialog.dismiss();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = messageHolder.itemView.getContext();
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, messageText);
                shareIntent.setType("text/plain");
                context.startActivity(Intent.createChooser(shareIntent, "Share"));
                mBottomSheetDialog.dismiss();
            }
        });

        return mBottomSheetDialog;
    }
}