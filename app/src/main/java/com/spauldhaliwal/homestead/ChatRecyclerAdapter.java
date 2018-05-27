package com.spauldhaliwal.homestead;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.vdurmont.emoji.EmojiParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.WEEK_IN_MILLIS;

public class ChatRecyclerAdapter extends FirebaseRecyclerAdapter<MessageModel, RecyclerView.ViewHolder> {

    FirebaseRecyclerAdapter adapter;
    RecyclerView recyclerView;

    private static final int VIEW_TYPE_MESSAGE_FIRST_MESSAGE = 0;
    private static final int VIEW_TYPE_MESSAGE_INCOMING = 1;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER = 2;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_SURROUNDED = 3;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST = 4;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST_FINAL = 5;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_ISOLATED = 6;
    private static final int VIEW_TYPE_MESSAGE_INCOMING_ISOLATED_FINAL = 7;
    private static final int VIEW_TYPE_MESSAGE_OUTGOING = 8;
    private static final int VIEW_TYPE_MESSAGE_OUTGOING_SAME_SENDER = 9;

    private Set<Integer> incomingMessages = new HashSet<>(Arrays.asList(VIEW_TYPE_MESSAGE_INCOMING,
            VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER,
            VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_SURROUNDED,
            VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST,
            VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST_FINAL,
            VIEW_TYPE_MESSAGE_INCOMING_ISOLATED,
            VIEW_TYPE_MESSAGE_INCOMING_ISOLATED_FINAL));

    private Set<Integer> outgoingMessages = new HashSet<>(Arrays.asList(VIEW_TYPE_MESSAGE_FIRST_MESSAGE,
            VIEW_TYPE_MESSAGE_OUTGOING,
            VIEW_TYPE_MESSAGE_OUTGOING_SAME_SENDER));

//    private HashMap<Integer, Integer> expandedItems = new HashMap<>();

    ChatRecyclerAdapter(@NonNull FirebaseRecyclerOptions<MessageModel> options) {
        super(options);
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel model = this.getItem(position);

        MessageModel lastMessage;
        MessageModel nextMessage;

        if (position >= 1) {
            lastMessage = this.getItem(position - 1);

        } else {
            lastMessage = this.getItem(position);
        }

        if (position == this.getItemCount() - 1) {
            nextMessage = this.getItem(position);
        } else {
            nextMessage = this.getItem(position + 1);

        }

        //TODO Fix message expansion animations

//        if (model.getSenderUid().equals(CurrentUser.getUid())) {
//            return VIEW_TYPE_MESSAGE_OUTGOING;
//        } else {
//            return VIEW_TYPE_MESSAGE_INCOMING;
//        }

        if (position == 0) {
            return VIEW_TYPE_MESSAGE_FIRST_MESSAGE;
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
//
//        if (viewType == VIEW_TYPE_MESSAGE_INCOMING) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.chat_message_incoming, parent, false);
//            return new IncomingMessageHolder(view);
//        } else if (viewType == VIEW_TYPE_MESSAGE_OUTGOING) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.chat_message_outgoing, parent, false);
//            return new OutgoingMessageHolder(view);
//        }
//
//        return null;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position, @NonNull MessageModel model) {
        // Bind the Chat object to the ChatHolder
        // ...

        MessageModel messageModel = this.getItem(position);

        MessageModel lastMessage;
        MessageModel nextMessage;

        if (position >= 1) {
            lastMessage = this.getItem(position - 1);

        } else {
            lastMessage = this.getItem(position);
        }

        if (position == this.getItemCount() - 1) {
            nextMessage = this.getItem(position);
        } else {
            nextMessage = this.getItem(position + 1);

        }

//        if (expandedItems.containsKey(position)) {
//            ((MessageHolder) holder).timestamp.setVisibility(View.VISIBLE);
//        } else {
//            ((MessageHolder) holder).timestamp.setVisibility(View.GONE);
//        }

        String message;

        switch (holder.getItemViewType()) {

            case VIEW_TYPE_MESSAGE_FIRST_MESSAGE:
                ((MessageHolder) holder).setMessage(model.getMessage());
                ((MessageHolder) holder).incommingMessagePadding.setVisibility(View.INVISIBLE);
                ((MessageHolder) holder).messageTopSpacer.setVisibility(View.INVISIBLE);

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

                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
                    emojisOnly.setTextSize(30);
                    emojisOnly.setText(message);
                    emojisOnly.setPadding(16, 0, 0, 0);
                    emojisOnly.setBackgroundColor(80000000);
                } else {
                    ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                }
                break;
            case VIEW_TYPE_MESSAGE_INCOMING_ISOLATED:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).setSenderName(model.getSenderName());
                ((IncomingMessageHolder) holder).setProfileImage(model.getProfileImage());
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());

                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
                    emojisOnly.setTextSize(30);
                    emojisOnly.setText(message);
                    emojisOnly.setPadding(16, 0, 0, 0);
                    emojisOnly.setBackgroundColor(80000000);
                } else {
                    ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                }
                break;
            case VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST_FINAL:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).setProfileImage(model.getProfileImage());
                ((IncomingMessageHolder) holder).getSenderName().setVisibility(View.GONE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());

                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
                    emojisOnly.setTextSize(30);
                    emojisOnly.setText(message);
                    emojisOnly.setPadding(16, 0, 0, 0);
                    emojisOnly.setBackgroundColor(80000000);
                } else {
                    ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                }


                break;
            case VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_LAST:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).setProfileImage(model.getProfileImage());
                ((IncomingMessageHolder) holder).getSenderName().setVisibility(View.GONE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
                    emojisOnly.setTextSize(30);
                    emojisOnly.setText(message);
                    emojisOnly.setPadding(16, 0, 0, 0);
                    emojisOnly.setBackgroundColor(80000000);
                } else {
                    ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                }
                break;
            case VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER_SURROUNDED:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).getSenderName().setVisibility(View.GONE);
                ((IncomingMessageHolder) holder).getProfileImage().setVisibility(View.INVISIBLE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
                    emojisOnly.setTextSize(30);
                    emojisOnly.setText(message);
                    emojisOnly.setPadding(16, 0, 0, 0);
                    emojisOnly.setBackgroundColor(80000000);
                } else {
                    ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                }
                break;
            case VIEW_TYPE_MESSAGE_INCOMING_SAME_SENDER:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).setSenderName(model.getSenderName());
                ((IncomingMessageHolder) holder).getProfileImage().setVisibility(View.INVISIBLE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
                    emojisOnly.setTextSize(30);
                    emojisOnly.setText(message);
                    emojisOnly.setPadding(16, 0, 0, 0);
                    emojisOnly.setBackgroundColor(80000000);
                } else {
                    ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                }
                break;
            case VIEW_TYPE_MESSAGE_INCOMING:
                ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                ((IncomingMessageHolder) holder).setSenderName(model.getSenderName());
                ((IncomingMessageHolder) holder).getProfileImage().setVisibility(View.INVISIBLE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
                    TextView emojisOnly = ((IncomingMessageHolder) holder).message;
                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
                    emojisOnly.setTextSize(30);
                    emojisOnly.setText(message);
                    emojisOnly.setPadding(16, 0, 0, 0);
                    emojisOnly.setBackgroundColor(80000000);
                } else {
                    ((IncomingMessageHolder) holder).setMessage(model.getMessage());
                }
                break;
            case VIEW_TYPE_MESSAGE_OUTGOING:
                ((OutgoingMessageHolder) holder).setMessage(model.getMessage());
                ((MessageHolder) holder).incommingMessagePadding.setVisibility(View.INVISIBLE);
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
                    TextView emojisOnly = ((OutgoingMessageHolder) holder).message;
                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
                    emojisOnly.setTextSize(30);
                    emojisOnly.setText(message);
                    emojisOnly.setPadding(0, 0, 16, 0);
                    emojisOnly.setBackgroundColor(80000000);
                } else {
                    ((OutgoingMessageHolder) holder).setMessage(model.getMessage());
                }

                break;
            case VIEW_TYPE_MESSAGE_OUTGOING_SAME_SENDER:
                ((OutgoingMessageHolder) holder).setMessage(model.getMessage());
                ((MessageHolder) holder).setTimestamp(model.getTimeSent());
                //                        Code to parse standalone emojis and make them larger.
                message = model.getMessage();

                if (EmojiParser.removeAllEmojis(message).trim().isEmpty()) {
                    TextView emojisOnly = ((OutgoingMessageHolder) holder).message;
                    ((MessageHolder) holder).messageBackground.setVisibility(View.GONE);
                    emojisOnly.setTextSize(30);
                    emojisOnly.setText(message);
                    emojisOnly.setPadding(0, 0, 16, 0);
                    emojisOnly.setBackgroundColor(80000000);
                } else {
                    ((OutgoingMessageHolder) holder).setMessage(model.getMessage());
                }
                break;

        }


    }

    static class MessageHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "MessageHolder";

        private TextView message;
        private TextView senderName;
        private ImageView profileImage;

        private TextView timestamp;
        private Space incommingMessagePadding;
        private Space messageTopSpacer;
        private final TextView messageBackground;


        MessageHolder(final View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.messageIncomingView);
            senderName = itemView.findViewById(R.id.messageSenderName);
            timestamp = itemView.findViewById(R.id.messageTimeStamp);
            profileImage = itemView.findViewById(R.id.messageSenderProfileImage);
            incommingMessagePadding = itemView.findViewById(R.id.incommingMessagePadding);
            messageTopSpacer = itemView.findViewById(R.id.messageTopSpacer);
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

        public Space getMessageTopSpacer() {
            return messageTopSpacer;
        }

        public void setMessageTopSpacer(Space messageTopSpacer) {
            this.messageTopSpacer = messageTopSpacer;
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
    }

    static class IncomingMessageHolder extends MessageHolder {
        private static final String TAG = "TaskViewHolder";

        private final TextView message;
        private final TextView senderName;
        private final TextView timestamp;
        private final ImageView profileImage;

        IncomingMessageHolder(final View itemView) {
            super(itemView);
            Log.d(TAG, "TaskViewHolder: starts");

            message = itemView.findViewById(R.id.messageIncomingView);
            senderName = itemView.findViewById(R.id.messageSenderName);
            timestamp = itemView.findViewById(R.id.messageTimeStamp);
            profileImage = itemView.findViewById(R.id.messageSenderProfileImage);


            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(profileImage.getContext(), AddEditActivity.class);
                    profileImage.getContext().startActivity(intent);

                }
            });

            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    final ChatRecyclerAdapter.MessageHolder holder = (ChatRecyclerAdapter.MessageHolder) recyclerView
//                            .findViewHolderForAdapterPosition(position);
//
//                    MessageModel message = (MessageModel) this.getItem(position);
//                    holder.setTimestamp(message.getTimeSent());

                    if (timestamp.getVisibility() == View.GONE) {
                        Transition fadeOut = new Fade().setStartDelay(60).setDuration(120);
                        Transition expand = new ChangeBounds().setDuration(180);
                        TransitionSet expandingAnimation = new TransitionSet()
                                .addTransition(fadeOut)
                                .addTransition(expand)
                                .setOrdering(TransitionSet.ORDERING_TOGETHER);

                        TransitionManager.beginDelayedTransition((ViewGroup) itemView.getParent(), expandingAnimation);
                        timestamp.setVisibility(View.VISIBLE);
//                    expandedItems.put(position, position);

                    } else {
                        TransitionSet collapsingAnimation = new TransitionSet();
                        collapsingAnimation.addTransition(new Fade().setDuration(90))
                                .addTransition(new ChangeBounds().setDuration(180))
                                .setOrdering(TransitionSet.ORDERING_TOGETHER);

                        TransitionManager.beginDelayedTransition((ViewGroup) itemView.getParent(), collapsingAnimation);
                        timestamp.setVisibility(View.GONE);
//                    expandedItems.delete(position);
                    }
                }
            });


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

        private final TextView message;

        OutgoingMessageHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "TaskViewHolder: starts");
            message = itemView.findViewById(R.id.messageOutgoingView);
        }

        public void setMessage(String n) {
            message.setText(n);
        }
    }
}