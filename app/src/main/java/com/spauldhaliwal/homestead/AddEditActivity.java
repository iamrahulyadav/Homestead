package com.spauldhaliwal.homestead;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class AddEditActivity extends AppCompatActivity {

    private static final String TAG = "AddEditActivity";

    public enum openMode {ADD, VIEW, AVAILABLE, CREATOR, OWNER}

    DatabaseReference jobsDatabseReference;
    DatabaseReference homesteadsDatabaseReerence;
    Vibrator vibe;

    RecyclerView jobNoteRecyclerView;
    JobNotesRecyclerAdapter jobNotesRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;

    private final List<JobNote> notesList = new ArrayList<>();



    static String jobId;
    static int jobStatus;
    static String jobOwner;
    static boolean jobScope;

    private openMode mMode;

    Dialog addNoteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        setContentView(R.layout.task_details);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        jobsDatabseReference = FirebaseDatabase.getInstance().getReference(JobsContract.ROOT_NODE);
        homesteadsDatabaseReerence = FirebaseDatabase.getInstance().getReference(HomesteadsContract.ROOT_NODE);


        final Bundle arguments = getIntent().getExtras();

        final JobModel job;

        final EditText editName = findViewById(R.id.editName);
        KeyListener editNameListener = editName.getKeyListener();
        editName.setKeyListener(editNameListener);

        final EditText editDescription = findViewById(R.id.editDescription);
        KeyListener editDescriptionListener = editName.getKeyListener();
        editDescription.setKeyListener(editDescriptionListener);

        final CheckBox claimCompleteTaskCheckBox = findViewById(R.id.claimTaskCheckBox);
        TextView claimTaskTextView = findViewById(R.id.claimTaskText);

        ImageButton saveButton = findViewById(R.id.editSaveButton);
        ImageButton deleteButon = findViewById(R.id.editDeleteButton);

        final Button addNoteButton = findViewById(R.id.addNoteButton);

        final Switch privacySwitch = findViewById(R.id.privacySwitch);
        final TextView privacyText = findViewById(R.id.privacyText);

        jobNoteRecyclerView = findViewById(R.id.notesRecyclerView);
        jobNotesRecyclerAdapter = new JobNotesRecyclerAdapter(notesList);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        jobNoteRecyclerView.setLayoutManager(linearLayoutManager);

        jobNoteRecyclerView.setAdapter(jobNotesRecyclerAdapter);

        if (privacySwitch.isChecked()) {
            privacyText.setText(JobsContract.PRIVATE);
        } else {
            privacyText.setText(JobsContract.PRIVATE);
        }

        if (arguments != null) {
            Log.d(TAG, "onCreate: Previous job found. Retrieving job details");
            // retrieve task details
            job = (JobModel) arguments.getSerializable(JobsContract.ROOT_NODE);
            KeyListener editNameKeyListener = editName.getKeyListener();
            KeyListener editDescriptionKeyListener = editDescription.getKeyListener();

            if (job.getStatus() == JobsContract.STATUS_OPEN) {
                //Job has not been claimed.
                claimCompleteTaskCheckBox.setEnabled(true);
                if (!job.getCreatorId().equals(CurrentUser.getUid())) {
                    //User is not the creator of this job
                    editName.setKeyListener(null);
                    editDescription.setKeyListener(null);
                    deleteButon.setVisibility(View.GONE);
                    mMode = openMode.AVAILABLE;
                    Toast.makeText(AddEditActivity.this, "Opening task as: " + mMode, Toast.LENGTH_LONG).show();


                }  else if (job.getCreatorId().equals(CurrentUser.getUid())) {
                    // User is the creator of this job and it has not been claimed yet. Can still
                    // be deleted/edited.
                    editName.setKeyListener(editNameKeyListener);
                    editDescription.setKeyListener(editDescriptionKeyListener);
                    deleteButon.setVisibility(View.VISIBLE);
                    mMode = openMode.CREATOR;
                    Toast.makeText(AddEditActivity.this, "Opening task as: " + mMode, Toast.LENGTH_LONG).show();

                }

            } else if (job.getStatus() == JobsContract.STATUS_CLAIMED){
                //Job has been claimed.
                claimCompleteTaskCheckBox.setEnabled(false);
                deleteButon.setVisibility(View.GONE);
                editName.setKeyListener(null);
                editDescription.setKeyListener(null);
                saveButton.setVisibility(View.GONE);

                if (!job.getCreatorId().equals(CurrentUser.getUid())
                        && job.getOwner().equals(CurrentUser.getUid())) {
                    //User is not the creator of this task but has claimed it.
                    claimCompleteTaskCheckBox.setEnabled(true);
                    claimTaskTextView.setText("Mark as complete");
                    saveButton.setVisibility(View.VISIBLE);
                    mMode = openMode.OWNER;
                    Toast.makeText(AddEditActivity.this, "Opening task as: " + mMode, Toast.LENGTH_LONG).show();

                } else if (!job.getOwner().equals(CurrentUser.getUid())) {
                    // User is not the claimant of this task.
                    claimCompleteTaskCheckBox.setVisibility(View.GONE);
                    claimTaskTextView.setText("Task has been claimed");
                    mMode = openMode.VIEW;
                    Toast.makeText(AddEditActivity.this, "Opening task as: " + mMode, Toast.LENGTH_LONG).show();

                }

                else if (job.getOwner().equals(CurrentUser.getUid())) {
                    //User is the creator of this job and the claimant.
                    claimCompleteTaskCheckBox.setEnabled(true);
                    saveButton.setVisibility(View.VISIBLE);
                    claimTaskTextView.setText("Mark as complete");
                    mMode = openMode.OWNER;
                    Toast.makeText(AddEditActivity.this, "Opening task as: " + mMode, Toast.LENGTH_LONG).show();
                }
            } else if (job.getStatus() == JobsContract.STATUS_CLOSED) {
                // Task has been completed.
                claimCompleteTaskCheckBox.setEnabled(false);
                deleteButon.setVisibility(View.GONE);
                editName.setKeyListener(null);
                editDescription.setKeyListener(null);
                saveButton.setVisibility(View.GONE);
                claimCompleteTaskCheckBox.setVisibility(View.GONE);
                claimTaskTextView.setText("Task has been completed");
                addNoteButton.setEnabled(false);
                mMode = openMode.VIEW;
                Toast.makeText(AddEditActivity.this, "Opening task as: " + mMode, Toast.LENGTH_LONG).show();

            }

            jobId = job.getId();
            jobScope = job.isIsPrivate();
            jobStatus = job.getStatus();
            Log.d(TAG, "onCreate: Job isPrivate: " + jobScope);

            editName.setText(job.getName());
            editDescription.setText(job.getDescription());
            privacySwitch.setVisibility(View.GONE);
            privacyText.setVisibility(View.GONE);
            loadNotes();
            Log.d(TAG, "notesLists: " + notesList.toString());

        } else {
            job = null;
            Log.d(TAG, "onCreate: No previous job found. Creating new job");

            claimTaskTextView.setVisibility(View.GONE);
            Log.d(TAG, "onCreate: claimTaskTextViewVisibility: " + claimTaskTextView.getVisibility());
            claimCompleteTaskCheckBox.setVisibility(View.GONE);
            deleteButon.setVisibility(View.GONE);
            editName.requestFocus();
            addNoteButton.setEnabled(false);
            mMode = openMode.ADD;
        }


        privacySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (privacySwitch.isChecked()) {
                    privacyText.setText("Private");
                } else {
                    privacyText.setText("Public");
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting data from widgets instead of JobModel object in case edits are made.
                String name = editName.getText().toString();
                String description = editDescription.getText().toString();

                switch (mMode) {
                    case ADD:
                        Log.d(TAG, "Save Button onClick: EditMode is " + mMode);
                        if (FirebaseResolver.insertJob(name,
                                description,
                                CurrentUser.getUid(),
                                CurrentUser.getProfileImage(),
                                privacySwitch.isChecked())) {
                            finish();
                        } else {
                            Toast.makeText(AddEditActivity.this, "Name is required.", Toast.LENGTH_LONG).show();
                        }
                        break;

                    case CREATOR:
                        Log.d(TAG, "Save Button onClick: EditMode is " + mMode);
                        Log.d(TAG, "Checkbox onClick: jobOwner == " + job.getOwner());
                        if (claimCompleteTaskCheckBox.isChecked() && job.getOwner() == null) {

                            if (FirebaseResolver.updateJob(jobId,
                                    name,
                                    description,
                                    JobsContract.STATUS_CLAIMED,
                                    CurrentUser.getUid(),
                                    jobScope)) {
                                Toast.makeText(AddEditActivity.this, "Task was claimed by the " + mMode, Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(AddEditActivity.this, "Name is required.", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            if (FirebaseResolver.updateJob(jobId,
                                    name,
                                    description,
                                    jobStatus,
                                    jobOwner,
                                    jobScope)) {
                                Toast.makeText(AddEditActivity.this, "Task was edited but not claimed", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(AddEditActivity.this, "Name is required.", Toast.LENGTH_LONG).show();
                            }
                        }
                        break;

                    case AVAILABLE:
                        Log.d(TAG, "Save Button onClick: EditMode is: " + mMode);
                        Toast.makeText(AddEditActivity.this, "marking as claimed", Toast.LENGTH_SHORT).show();
                        if (FirebaseResolver.updateJob(jobId,
                                name,
                                description,
                                JobsContract.STATUS_CLAIMED,
                                CurrentUser.getUid(),
                                jobScope)) {
                            finish();
                        } else {
                            Toast.makeText(AddEditActivity.this, "Name is required.", Toast.LENGTH_LONG).show();
                        }
                        break;

                    case VIEW:
                        Log.d(TAG, "Save Button onClick: EditMode is: " + mMode + ". This should never happen," +
                                " as the task is in " + mMode + " mode only and cannot be edited/claimed.");
                        break;

                    case OWNER:
                        Log.d(TAG, "Save Button onClick: EditMode is: " + mMode);
                        if (claimCompleteTaskCheckBox.isChecked()) {
                            Log.d(TAG, "Save Button onClick: Current user is marking task as completed");

                            FirebaseResolver.updateJob(jobId,
                                    name,
                                    description,
                                    JobsContract.STATUS_CLOSED,
                                    CurrentUser.getUid(),
                                    jobScope);
                            finish();
                            break;
                    }

                }
            }
        });

        deleteButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseResolver.delete(jobId, jobScope);
                finish();
            }
        });

        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNoteDialog = new Dialog(AddEditActivity.this);
                addNoteDialog.setContentView(R.layout.add_note_dialog);
                addNoteDialog.show();

                Button addNoteSaveButton = addNoteDialog.findViewById(R.id.addNoteSaveButton);
                final EditText addNoteText = addNoteDialog.findViewById(R.id.addNoteEditText);

                addNoteSaveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseResolver.insertJobNote(job.getId(),
                                CurrentUser.getUid(),
                                CurrentUser.getProfileImage(),
                                addNoteText.getText().toString(),
                                jobScope);
                        addNoteDialog.dismiss();
                    }
                });
            }
        });
    }

    private void loadNotes() {
        DatabaseReference messageRef = FirebaseDatabase.getInstance()
                .getReference(HomesteadsContract.ROOT_NODE)
                .child(CurrentUser.getHomesteadUid())
                .child(HomesteadsContract.JOBS_NODE)
                .child(jobId)
                .child(JobsContract.NOTES);

        Query messageQuery = messageRef.limitToLast(50);

        ChildEventListener childEventListener = messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                JobNote jobNote = dataSnapshot.getValue(JobNote.class);

                notesList.add(jobNote);
                Log.d(TAG, "onChildAdded: " + jobNote.toString());
                Log.d(TAG, "onChildAdded: notesLit: "+ notesList.toString());
                jobNotesRecyclerAdapter.notifyDataSetChanged();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case (R.id.action_signout):
                AuthUI.getInstance().signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // User is now signed out
                                if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                                    ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE))
                                            .clearApplicationUserData();
                                } else {
                                    // use old hacky way, which can be removed
                                    // once minSdkVersion goes above 19 in a few years.
                                    Log.d(TAG, "onComplete: Application is <kitkat.");
                                }
                                Intent intent = new Intent(AddEditActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                recreate();

                            }
                        });
                return true;
            case (R.id.menuChatItem):
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

}