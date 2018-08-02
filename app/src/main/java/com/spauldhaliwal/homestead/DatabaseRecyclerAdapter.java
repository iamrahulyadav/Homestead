package com.spauldhaliwal.homestead;

import android.graphics.Paint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.spauldhaliwal.homestead.DatabaseRecyclerAdapter.JobViewHolder;

/**
 * Created by pauldhaliwal on 2018-03-24.
 */

class DatabaseRecyclerAdapter extends FirebaseRecyclerAdapter<JobModel, JobViewHolder> {
    private static final String TAG = "DatabaseRecyclerViewAda";

    DatabaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions<JobModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull JobViewHolder holder, int position, @NonNull JobModel model) {
        Log.d(TAG, "onBindViewHolder: starts");
        holder.setName(model.getName());
        holder.setDescription(model.getDescription());
        holder.setCreatorIdImage(model.getCreatorIdImage());

        if (holder.getItemViewType() == JobsContract.STATUS_CLOSED) {
            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.description.setPaintFlags(holder.description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

    }

    @Override
    public int getItemViewType(int position) {
        JobModel jobModel = getItem(position);
        int jobStatus = jobModel.getStatus();

        if (jobStatus == JobsContract.STATUS_OPEN) {
            return JobsContract.STATUS_OPEN;
        } else if (jobStatus == JobsContract.STATUS_CLAIMED) {
            return JobsContract.STATUS_CLAIMED;
        } else if (jobStatus == JobsContract.STATUS_CLOSED) {
            return JobsContract.STATUS_CLOSED;
        } else {
            return -1;
        }


    }

    @Override
    public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: starts");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_board_list_item, parent, false);
        return new JobViewHolder(view);
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "TaskViewHolder";

        private final TextView name;
        private final TextView description;
        private final ImageView creatorIdImage;

        JobViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "TaskViewHolder: starts");
            name = itemView.findViewById(R.id.job_name);
            description = itemView.findViewById(R.id.job_description);
            creatorIdImage = itemView.findViewById(R.id.personalProfileImageList);
        }

        public void setName(String n) {
            name.setText(n);
        }

        public void setDescription(String d) {
            description.setText(d);
        }

        public void setCreatorIdImage(String d) {
            Log.d(TAG, "setCreatorImage: description == " + description);
            Log.d(TAG, "setCreatorImage: == " + creatorIdImage);
//            if (creatorIdImage != null) {
            Glide.with(name.getContext())
                    .load(Uri.parse(d))
                    .apply(RequestOptions.circleCropTransform())
                    .into(creatorIdImage);
//            }

        }

    }
}
