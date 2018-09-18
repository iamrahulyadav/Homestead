package com.spauldhaliwal.homestead;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "TaskAdapter";

    private List<JobModel> taskList;

    public TaskAdapter(List<JobModel> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_board_list_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: starts");

        JobModel model = this.getItem(position);
        TaskViewHolder taskViewHolder = (TaskViewHolder) holder;
        taskViewHolder.setName(model.getName());
        taskViewHolder.setDescription(model.getDescription());
        taskViewHolder.setCreatorIdImage(model.getCreatorIdImage());

        if (holder.getItemViewType() == JobsContract.STATUS_CLOSED) {
            taskViewHolder.name.setPaintFlags(taskViewHolder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            taskViewHolder.description.setPaintFlags(taskViewHolder.description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
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

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "TaskViewHolder";

        private final TextView name;
        private final TextView description;
        private final ImageView creatorIdImage;

        TaskViewHolder(View itemView) {
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

//            Glide.with(name.getContext())
//                    .load(Uri.parse(d))
//                    .apply(RequestOptions.circleCropTransform())
//                    .into(creatorIdImage);
        }

    }

    public JobModel getItem(int position) {
        return taskList.get(position);
    }
}
