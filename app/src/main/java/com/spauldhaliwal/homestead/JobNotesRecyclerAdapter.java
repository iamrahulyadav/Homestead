package com.spauldhaliwal.homestead;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class JobNotesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "JobNotesRecyclerAdapter";
    //    FirebaseRecyclerAdapter adapter;
    RecyclerView recyclerView;

    private List<JobNote> noteList;

    public JobNotesRecyclerAdapter(List<JobNote> noteList) {
        this.noteList = noteList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new instance of the ViewHolder

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_message_incoming, parent, false);
            return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: note binded");
        JobNote model = this.getItem(position);


        ((NoteHolder) holder).setContent(model.getContent());
        ((NoteHolder) holder).setProfileImage(model.getCreatorIdImage());

    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    static class NoteHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "MessageHolder";

        private TextView content;
        private ImageView profileImage;

        NoteHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.messageIncomingView);
            profileImage = itemView.findViewById(R.id.messageSenderProfileImage);
            }


        public void setContent(String n) {
            content.setText(n);
        }

        public void setProfileImage(String d) {

            Glide.with(content.getContext())
                    .load(Uri.parse(d))
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImage);

        }

        public TextView getContent() {
            return content;
        }

        public ImageView getProfileImage() {
            return profileImage;
        }
    }


    public JobNote getItem(int position) {
        return noteList.get(position);
    }

}
