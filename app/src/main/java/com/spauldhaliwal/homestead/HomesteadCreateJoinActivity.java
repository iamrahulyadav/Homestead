package com.spauldhaliwal.homestead;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomesteadCreateJoinActivity extends AppCompatActivity {

    private static final String TAG = "HomesteadCreateJoinActi";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: HomesteadCreateJoinActivity start.");

        final String homesteadInviteId = getIntent().getStringExtra("homesteadInviteId");
        Log.d(TAG, "onCreate: HomesteadInviteId: " + homesteadInviteId);

        if (homesteadInviteId != null) {
            // HomesteadInviteId found. Confirming whether or not the user wishes to join the homestead.
            new AlertDialog.Builder(this)
                    .setTitle("Join Homestead")
                    .setMessage("Are you sure you want to join this Homestead?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(UsersContract.ROOT_NODE);
                            user = FirebaseAuth.getInstance().getCurrentUser();

                            ref.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(TAG, "onCancelled: DatabaseError: " + databaseError);
                                    Intent intent = new Intent();
                                    setResult(Activity.RESULT_CANCELED, intent);
                                    finish();
                                }

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // User already has an account
                                        Log.d(TAG, "onDataChange: User has account, assigning to homestead: " + homesteadInviteId);
                                        ref.child(user.getUid())
                                                .child(UsersContract.HOMESTEAD_ID)
                                                .setValue(homesteadInviteId);

                                        CurrentUser.buildUser(new CurrentUser.OnGetDataListener() {
                                            @Override
                                            public void onSuccess() {
                                                //Persist user data using SharedPrefences
                                                SharedPreferences sharedPref = mContext.getSharedPreferences(
                                                        "com.spauldhaliwal.homestead.SignInActivity.PREFERENCES_FILE_KEY",
                                                        Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPref.edit();
                                                editor.putString(UsersContract.HOMESTEAD_ID, CurrentUser.getHomesteadUid());
                                                Log.d(TAG, "SignInActivity onCreate: CurrentUser.getHomesteadID: " + CurrentUser.getHomesteadUid());
                                                editor.apply();

                                                Intent intent = new Intent(HomesteadCreateJoinActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);

                                            }
                                        });

                                    } else {
                                        Log.d(TAG, "onDataChange: Error creating account.");
                                        Intent intent = new Intent();
                                        setResult(Activity.RESULT_CANCELED, intent);
                                        finish();
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();

        }

        setContentView(R.layout.activity_homestead_create_join);
        Button createHomesteadButton = findViewById(R.id.createHomesteadButton);
        Button joinHomesteadButton = findViewById(R.id.joinHomesteadButton);

        final EditText homesteadName = findViewById(R.id.createHomesteadName);
        final TextView title = findViewById(R.id.createJoinHomesteadTitleTextView);

        createHomesteadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseResolver.createHomestead(homesteadName.getText().toString())) {
                    Log.d(TAG, "Homestead create onClick: creating homestead");
                    CurrentUser.buildUser(new CurrentUser.OnGetDataListener() {
                        @Override
                        public void onSuccess() {
                            //Persist user data using SharedPrefences
                            SharedPreferences sharedPref = mContext.getSharedPreferences(
                                    "com.spauldhaliwal.homestead.SignInActivity.PREFERENCES_FILE_KEY",
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(UsersContract.HOMESTEAD_ID, CurrentUser.getHomesteadUid());
                            Log.d(TAG, "SignInActivity onCreate: CurrentUser.getHomesteadID: " + CurrentUser.getHomesteadUid());
                            editor.commit();

                            Intent intent = new Intent(HomesteadCreateJoinActivity.this,
                                    MainActivity.class);
                            setResult(Activity.RESULT_OK, intent);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });

                } else {
                    Toast.makeText(HomesteadCreateJoinActivity.this,
                            "Homesteads require a name.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });


        joinHomesteadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO implement invite link parsing functionality.
                final Snackbar snackbar = Snackbar.make(v, "Follow an invite link to join an existing Homestead", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
                    snackbar.show();
            }
        });

    }
}
