package com.spauldhaliwal.homestead;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public static final int RC_SIGN_IN = 1;
    public static final int CREATE_HOMESTEAD_REQUEST = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: SignIn Activity Starts");
        super.onCreate(savedInstanceState);
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        boolean signInSuccess = false;

        if (auth.getCurrentUser() != null) {
            // User is already signed in
            Log.d(TAG, "onCreate: already signed in");
            Log.d(TAG, "onCreate: CurrentUser.getHomesteadID: " + CurrentUser.getHomesteadUid());

            CurrentUser.buildUser(new CurrentUser.OnGetDataListener() {
                @Override
                public void onSuccess() {

                    if (CurrentUser.getHomesteadUid() != null) {
                        // User belongs to a homestead.
                        Intent launchMainActivityIntent = new Intent(SignInActivity.this, MainActivity.class);
                        startActivity(launchMainActivityIntent);
                        finish();

                    } else {
                        // User does not belong to a homestead
                        Log.d(TAG, "onSuccess: User does not belong to a homestead");

                        FirebaseDynamicLinks.getInstance()
                                .getDynamicLink(getIntent())
                                .addOnSuccessListener(SignInActivity.this,
                                        new OnSuccessListener<PendingDynamicLinkData>() {

                                            @Override
                                            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                                                // Retrieve homestead invite id if it exists.
                                                Uri deepLink = null;
                                                if (pendingDynamicLinkData != null) {
                                                    Intent intent = getIntent();
                                                    Uri uri = intent.getData();
                                                    String homesteadInviteId = uri.getQueryParameter("homesteadid");
                                                    Log.d(TAG, "GetQuery: Homestead ID = " + uri.getQueryParameter("homesteadid"));

                                                    Intent createJoinHomesteadIntent = new Intent(SignInActivity.this, HomesteadCreateJoinActivity.class);
                                                    createJoinHomesteadIntent.putExtra("homesteadInviteId", homesteadInviteId);
                                                    startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                } else {
                                                    Log.d(TAG, "onSuccess: Homestead invite id does not exist");
                                                    Intent createJoinHomesteadIntent = new Intent(SignInActivity.this, HomesteadCreateJoinActivity.class);
                                                    startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                }
                                            }

                                        }).addOnFailureListener(SignInActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.d(TAG, "getDynamicLink onFailure: " + e);
                                Toast.makeText(SignInActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                }
            });

        } else {
            // User is not signed in
            startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.GoogleBuilder().build()
                            )).build(),
                    RC_SIGN_IN);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: starts");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in.
                user = FirebaseAuth.getInstance().getCurrentUser();


                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(UsersContract.ROOT_NODE);

                ref.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User already has an account

                            Log.d(TAG, "onDataChange: dataSnapshot == " + dataSnapshot);

                            if (dataSnapshot.hasChild(UsersContract.HOMESTEAD_ID)) {
                                //User has a homesteadId
                                ref.child(user.getUid())
                                        .child(UsersContract.TOKEN_ID)
                                        .setValue(FirebaseInstanceId.getInstance().getToken());

                                CurrentUser.buildUser(new CurrentUser.OnGetDataListener() {
                                    @Override
                                    public void onSuccess() {
                                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                        FirebaseMessaging.getInstance()
                                                .subscribeToTopic(CurrentUser.getHomesteadUid() + HomesteadsContract.NOTIFICATIONS);
                                    }
                                });
                            } else {
                                Toast.makeText(SignInActivity.this, "User does not belong to any homestead.", Toast.LENGTH_LONG).show();
                                FirebaseDynamicLinks.getInstance()
                                        .getDynamicLink(getIntent())
                                        .addOnSuccessListener(SignInActivity.this,
                                                new OnSuccessListener<PendingDynamicLinkData>() {

                                                    @Override
                                                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                                                        // Retrieve homestead invite id if it exists.
                                                        Uri deepLink = null;
                                                        if (pendingDynamicLinkData != null) {
                                                            //Homestead id exists, loading join activity
                                                            // with id in intent.
                                                            Intent intent = getIntent();
                                                            Uri uri = intent.getData();
                                                            String homesteadInviteId = uri.getQueryParameter("homesteadid");
                                                            Log.d(TAG, "GetQuery: Homestead ID = " + uri.getQueryParameter("homesteadid"));

                                                            Intent createJoinHomesteadIntent = new Intent(SignInActivity.this, HomesteadCreateJoinActivity.class);
                                                            createJoinHomesteadIntent.putExtra("homesteadInviteId", homesteadInviteId);
                                                            startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                        } else {
                                                            Intent createJoinHomesteadIntent = new Intent(SignInActivity.this, HomesteadCreateJoinActivity.class);
                                                            startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                        }
                                                    }

                                                }).addOnFailureListener(SignInActivity.this, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "getDynamicLink onFailure: " + e);
                                        Toast.makeText(SignInActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                });
                            }

                        } else {
                            // User doesn't have an account. Creating new one.
                            Log.d(TAG, "onDataChange: creating user.");
                            UserModel userModel = new UserModel(user.getUid(),
                                    FirebaseInstanceId.getInstance().getToken(),
                                    user.getDisplayName(),
                                    user.getEmail(),
                                    user.getPhotoUrl().toString(),
                                    "node",
                                    "node");
                            Log.d(TAG, "onDataChange: creating user.." + userModel.toString());
                            ref.child(user.getUid()).setValue(userModel);

                            FirebaseDynamicLinks.getInstance()
                                    .getDynamicLink(getIntent())
                                    .addOnSuccessListener(SignInActivity.this,
                                            new OnSuccessListener<PendingDynamicLinkData>() {

                                                @Override
                                                public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                                                    // Retrieve homestead invite id if it exists.
                                                    Uri deepLink = null;
                                                    if (pendingDynamicLinkData != null) {
                                                        //Homestead id exists, loading join activity
                                                        // with id in intent.
                                                        Intent intent = getIntent();
                                                        Uri uri = intent.getData();
                                                        String homesteadInviteId = uri.getQueryParameter("homesteadid");
                                                        Log.d(TAG, "GetQuery: Homestead ID = " + uri.getQueryParameter("homesteadid"));

                                                        Intent createJoinHomesteadIntent = new Intent(SignInActivity.this, HomesteadCreateJoinActivity.class);
                                                        createJoinHomesteadIntent.putExtra("homesteadInviteId", homesteadInviteId);
                                                        startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                    } else {
                                                        Intent createJoinHomesteadIntent = new Intent(SignInActivity.this, HomesteadCreateJoinActivity.class);
                                                        startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                    }
                                                }

                                            }).addOnFailureListener(SignInActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "getDynamicLink onFailure: " + e);
                                    Toast.makeText(SignInActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            });

//                            CurrentUser.buildUser(new CurrentUser.OnGetDataListener() {
//                                @Override
//                                public void onSuccess() {
//                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
////                                    Intent intent = new Intent(MainActivity.this, HomesteadCreateJoinActivity.class);
//                                    startActivity(intent);
//                                    finish();
//                                }
//                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }


        } else if (requestCode == CREATE_HOMESTEAD_REQUEST) {
            if (resultCode == RESULT_OK) {

            } else if (resultCode == RESULT_CANCELED)
                Toast.makeText(this, "Error joining homestead.", Toast.LENGTH_SHORT).show();
            finish();

        } else {
            Log.d(TAG, "onActivityResult: sign in failed");
            // Sign in failed.
            Toast.makeText(this, "Sign-in Error", Toast.LENGTH_SHORT).show();
            finish();
        }

    }
}
