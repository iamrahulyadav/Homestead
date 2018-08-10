package com.spauldhaliwal.homestead;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.transition.AutoTransition;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.widget.Space;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.WEEK_IN_MILLIS;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ChatAdapter";

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

    private HashMap<Integer, Integer> expandedItems = new HashMap<>();

    private List<MessageModel> messagesList;

    public ChatAdapter(List<MessageModel> messagesList) {
        this.messagesList = messagesList;
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
                && model.getSenderUid().equals(CurrentUser.getUid())) {
            return VIEW_TYPE_MESSAGE_FIRST_MESSAGE_OUTGOING;
        } else if (position == 0
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            return VIEW_TYPE_MESSAGE_FIRST_MESSAGE_INCOMING;
        } else if ((position == this.getItemCount() - 1)
                && !lastMessage.getSenderUid().equals(model.getSenderUid())
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            return VIEW_TYPE_MESSAGE_INCOMING_ISOLATED_FINAL;

        } else if (!model.getSenderUid().equals(nextMessage.getSenderUid())
                && !lastMessage.getSenderUid().equals(model.getSenderUid())
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            return VIEW_TYPE_MESSAGE_INCOMING_ISOLATED;

        } else if ((position == this.getItemCount() - 1)
                && lastMessage.getSenderUid().equals(model.getSenderUid())
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            return VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST_FINAL;

        } else if (model.getSenderUid().equals(nextMessage.getSenderUid())
                && lastMessage.getSenderUid().equals(model.getSenderUid())
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            return VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_SURROUNDED;

        } else if (!model.getSenderUid().equals(nextMessage.getSenderUid())
                && lastMessage.getSenderUid().equals(model.getSenderUid())
                && !model.getSenderUid().equals(CurrentUser.getUid())) {
            return VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST;

        } else if (!model.getSenderUid().equals(CurrentUser.getUid())
                && lastMessage.getSenderUid().equals(model.getSenderUid())) {
            // messageSender is someone else but also sender of last message
            return VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER;

        } else if (!model.getSenderUid().equals(CurrentUser.getUid())) {
            // Message sender is someone else
            return VIEW_TYPE_MESSAGE_INCOMING;

        } else if (model.getSenderUid().equals(CurrentUser.getUid())
                && lastMessage.getSenderUid().equals(model.getSenderUid())) {
            // Current user is the sender of the message but not the last message
            return VIEW_TYPE_MESSAGE_OUTGOING_SAME_SENDER;

        } else if (model.getSenderUid().equals(CurrentUser.getUid())) {
            // Current user is the sender of the message but not the last message
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
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Bind the Chat object to the ChatHolder
        // ...

        MessageModel model = (MessageModel) this.getItem(position);

        if (expandedItems.containsKey(position)) {
            ((MessageHolder) holder).timestamp.setVisibility(View.VISIBLE);
        } else {
            ((MessageHolder) holder).timestamp.setVisibility(View.GONE);
        }

        String message;

        switch (holder.getItemViewType()) {

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
                ((MessageHolder) holder).setMessage(model.getMessage());
                ((MessageHolder) holder).incommingMessagePadding.setVisibility(View.INVISIBLE);
                ((MessageHolder) holder).messageTopSpacer.setVisibility(View.INVISIBLE);

                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();
                break;
            case VIEW_TYPE_MESSAGE_OUTGOING:
                ((OutgoingMessageHolder) holder).setMessage(model.getMessage());
                ((MessageHolder) holder).incommingMessagePadding.setVisibility(View.INVISIBLE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
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
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
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

    static class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        private static final String TAG = "MessageHolder";

        protected TextView message;
        protected TextView senderName;
        protected ImageView profileImage;

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

        public void setSenderName(String n) {
            senderName.setText(n);
        }

        public void setProfileImage(String d) {

            Glide.with(message.getContext())
                    .load(Uri.parse(d))
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImage);

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

        @Override
        public void onClick(View v) {

            ChangeBounds changeBounds = new ChangeBounds();
            changeBounds.setResizeClip(false);
            AutoTransition autoTransition = new AutoTransition();


            if (getTimestamp().getVisibility() == View.GONE) {
                TransitionManager.endTransitions(mRecyclerView);
                Transition fadeOut = new Fade().setStartDelay(60).setDuration(120);
                Transition expand = new ChangeBounds().setDuration(180);
                TransitionSet expandingAnimation = new TransitionSet()
                        .addTransition(fadeOut)
                        .addTransition(expand)
                        .setOrdering(TransitionSet.ORDERING_TOGETHER);

                TransitionManager.beginDelayedTransition(mRecyclerView, expandingAnimation);
                getTimestamp().setVisibility(View.VISIBLE);

            } else {
                TransitionManager.endTransitions(mRecyclerView);
                TransitionSet collapsingAnimation = new TransitionSet();
                collapsingAnimation.addTransition(new Fade().setDuration(90))
                        .addTransition(new ChangeBounds().setDuration(180))
                        .setOrdering(TransitionSet.ORDERING_TOGETHER);

                TransitionManager.beginDelayedTransition(mRecyclerView, collapsingAnimation);
                getTimestamp().setVisibility(View.GONE);
            }


        }


        @Override
        public boolean onLongClick(View v) {
            Toast.makeText(itemView.getContext(), message.getText(), Toast.LENGTH_SHORT).show();
            return true;
        }
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

            message.setOnClickListener(this);
            message.setOnLongClickListener(this);

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

            Glide.with(message.getContext())
                    .load(Uri.parse(d))
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImage);

        }
    }

    static class OutgoingMessageHolder extends MessageHolder {
        private static final String TAG = "TaskViewHolder";

//        private final TextView message;

        OutgoingMessageHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "TaskViewHolder: starts");
            message = itemView.findViewById(R.id.messageOutgoingView);

            message.setOnClickListener(this);
            message.setOnLongClickListener(this);
        }

        public TextView getMessage() {
            return message;
        }

        public void setMessage(String n) {
            message.setText(n);
        }

    }

    public MessageModel getItem(int position) {
        return messagesList.get(position);
    }
}
