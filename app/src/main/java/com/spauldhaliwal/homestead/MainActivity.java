package com.spauldhaliwal.homestead;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;

import static com.facebook.internal.CallbackManagerImpl.RequestCodeOffset.AppInvite;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    Vibrator vibe;

    // When requested, this adapter returns a HomeBoardFragment,
    // representing an object in the collection.
    ViewPager mViewPager;
    HomeBoardPagerAdapter mAdapter;
    public static final int RC_SIGN_IN = 1;
    public static final int CREATE_HOMESTEAD_REQUEST = 2;

    String homesteadInviteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity onCreate: starts");
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        final FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            // User is already signed in
            Log.d(TAG, "onCreate: already signed in");
            Log.d(TAG, "onCreate: CurrentUser.getHomesteadID: " + CurrentUser.getHomesteadUid());

            CurrentUser.buildUser(new CurrentUser.OnGetDataListener() {
                @Override
                public void onSuccess() {

                    if (CurrentUser.getHomesteadUid() != null) {
                        // User belongs to a homestead.
                        setContentView(R.layout.activity_main);
                        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                        toolbar.setSubtitleTextColor(616161);
                        setSupportActionBar(toolbar);

                        FloatingActionButton fab = findViewById(R.id.fab);

                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                                startActivity(intent);
                            }
                        });

                        mAdapter = new HomeBoardPagerAdapter(getSupportFragmentManager());

                        // ViewPager and its adapters use support library fragments,
                        // so use getSupportFragmentManager.
                        mViewPager = findViewById(R.id.content_pager);

                        mViewPager.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        // User does not belong to a homestead
                        Log.d(TAG, "onSuccess: User does not belong to a homestead");

                        FirebaseDynamicLinks.getInstance()
                                .getDynamicLink(getIntent())
                                .addOnSuccessListener(MainActivity.this,
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

                                                    Intent createJoinHomesteadIntent = new Intent(MainActivity.this, HomesteadCreateJoinActivity.class);
                                                    createJoinHomesteadIntent.putExtra("homesteadInviteId", homesteadInviteId);
                                                    startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                } else {
                                                    Log.d(TAG, "onSuccess: Homestead invite id does not exist");
                                                    Intent createJoinHomesteadIntent = new Intent(MainActivity.this, HomesteadCreateJoinActivity.class);
                                                    startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                }
                                            }

                                        }).addOnFailureListener(MainActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.d(TAG, "getDynamicLink onFailure: " + e);
                                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
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

        super.onCreate(savedInstanceState);
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
                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                        FirebaseMessaging.getInstance()
                                                .subscribeToTopic(CurrentUser.getHomesteadUid() + HomesteadsContract.NOTIFICATIONS);
                                    }
                                });
                            } else {
                                Toast.makeText(MainActivity.this, "User does not belong to any homestead.", Toast.LENGTH_LONG).show();
                                FirebaseDynamicLinks.getInstance()
                                        .getDynamicLink(getIntent())
                                        .addOnSuccessListener(MainActivity.this,
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

                                                            Intent createJoinHomesteadIntent = new Intent(MainActivity.this, HomesteadCreateJoinActivity.class);
                                                            createJoinHomesteadIntent.putExtra("homesteadInviteId", homesteadInviteId);
                                                            startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                        } else {
                                                            Intent createJoinHomesteadIntent = new Intent(MainActivity.this, HomesteadCreateJoinActivity.class);
                                                            startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                        }
                                                    }

                                                }).addOnFailureListener(MainActivity.this, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "getDynamicLink onFailure: " + e);
                                        Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
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
                                    .addOnSuccessListener(MainActivity.this,
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

                                                        Intent createJoinHomesteadIntent = new Intent(MainActivity.this, HomesteadCreateJoinActivity.class);
                                                        createJoinHomesteadIntent.putExtra("homesteadInviteId", homesteadInviteId);
                                                        startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                    } else {
                                                        Intent createJoinHomesteadIntent = new Intent(MainActivity.this, HomesteadCreateJoinActivity.class);
                                                        startActivityForResult(createJoinHomesteadIntent, CREATE_HOMESTEAD_REQUEST);
                                                    }
                                                }

                                            }).addOnFailureListener(MainActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "getDynamicLink onFailure: " + e);
                                    Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
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
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                recreate();

                            }
                        });
                return true;
            case (R.id.menuInvite):
                String userId = CurrentUser.getUid();
                String homesteadId = CurrentUser.getHomesteadUid();
                Task<ShortDynamicLink> dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse("https://homesteadapp.com/?homesteadid=" + homesteadId + "&userid=" + userId))
                        .setDynamicLinkDomain("homesteadapp.page.link")
                        // Open links with this app on android
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                        // Open links with this app on iOS
                        .setIosParameters(new DynamicLink.IosParameters
                                .Builder("com.spauldhaliwal.homestead").build())
                        .buildShortDynamicLink().addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                            @Override
                            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                if (task.isSuccessful()) {
                                    // Short Link created
                                    Uri shortDynamicLinkUri = task.getResult().getShortLink();
                                    Uri flowchartLink = task.getResult().getPreviewLink();

                                    Log.d(TAG, "onOptionsItemSelected: inviteUri: " + shortDynamicLinkUri);

                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Invite URL");
                                    intent.putExtra(Intent.EXTRA_TEXT, shortDynamicLinkUri.toString());
                                    startActivity(Intent.createChooser(intent, "Invite URL"));

                                } else {
                                    // Error
                                    // ...
                                }

                            }
                        });

                return true;
            case (R.id.menuChatItem):

                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                return true;

            case (R.id.menuLeaveHomestead):
                new AlertDialog.Builder(this)
                        .setTitle("Leave Homestead")
                        .setMessage("Are you sure you want to leave this Homestead?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseResolver.leaveHomestead();

                                AuthUI.getInstance().signOut(MainActivity.this)
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
                                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                                recreate();

                                            }
                                        });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
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
