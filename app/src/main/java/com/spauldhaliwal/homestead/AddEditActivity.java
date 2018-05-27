package com.spauldhaliwal.homestead;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddEditActivity extends AppCompatActivity {
    private static final String TAG = "AddEditActivity";

    public enum editMode {ADD, EDIT}

    DatabaseReference jobsDatabseReference;
    DatabaseReference homesteadsDatabaseReerence;
    Vibrator vibe;


    static String jobId;
    static boolean jobScope;

    private editMode mMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        setContentView(R.layout.activity_add_edit);
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

        ImageButton saveButton = findViewById(R.id.editSaveButton);
        ImageButton deleteButon = findViewById(R.id.editDeleteButton);

        final Switch privacySwitch = findViewById(R.id.privacySwitch);
        final TextView privacyText = findViewById(R.id.privacyText);

        if (privacySwitch.isChecked()) {
            privacyText.setText("Private");
        } else {
            privacyText.setText("Public");
        }

        if (arguments != null) {
            Log.d(TAG, "onCreate: Previous job found. Retrieving job details");
            // retrieve task details
            job = (JobModel) arguments.getSerializable(JobsContract.ROOT_NODE);

            if (!job.getCreatorId().equals(CurrentUser.getUid())) {
                editName.setKeyListener(null);
                editDescription.setKeyListener(null);
                saveButton.setVisibility(View.GONE);
                deleteButon.setVisibility(View.GONE);
            }
            jobId = job.getId();
            jobScope = job.isIsPrivate();
            Log.d(TAG, "onCreate: Job isPrivate: " + jobScope);

            editName.setText(job.getName());
            editDescription.setText(job.getDescription());
            mMode = editMode.EDIT;
            privacySwitch.setVisibility(View.GONE);
            privacyText.setVisibility(View.GONE);

        } else {
            Log.d(TAG, "onCreate: No previous job found. Creating new job");

            deleteButon.setVisibility(View.GONE);
            editName.requestFocus();
            mMode = editMode.ADD;
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
                    case EDIT:
                        Log.d(TAG, "Save Button onClick: EditMode is " + mMode);
                        if (FirebaseResolver.updateJob(jobId,
                                name,
                                description,
                                jobScope)) {
                            finish();
                        } else {
                            Toast.makeText(AddEditActivity.this, "Name is required.", Toast.LENGTH_LONG).show();
                        }
                        break;
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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