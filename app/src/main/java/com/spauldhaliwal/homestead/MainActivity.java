package com.spauldhaliwal.homestead;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
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
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    Vibrator vibe;

    // When requested, this adapter returns a HomeBoardFragment,
    // representing an object in the collection.
    ViewPager mViewPager;
    HomeBoardPagerAdapter mAdapter;
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity onCreate: starts");
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        final FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            // User is already signed in
            Log.d(TAG, "onCreate: already signed in");

            CurrentUser.buildUser(new CurrentUser.OnGetDataListener() {
                @Override
                public void onSuccess() {
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

                            ref.child(user.getUid())
                                    .child(UsersContract.TOKEN_ID)
                                    .setValue(FirebaseInstanceId.getInstance().getToken());

                            CurrentUser.buildUser(new CurrentUser.OnGetDataListener() {
                                @Override
                                public void onSuccess() {
                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                        } else {
                            // User doesn't have an account. Creating new one.
                            Log.d(TAG, "onDataChange: creating user.");
                            UserModel userModel = new UserModel(user.getUid(),
                                    FirebaseInstanceId.getInstance().getToken(),
                                    user.getDisplayName(),
                                    user.getEmail(),
                                    user.getPhotoUrl().toString(),
                                    "-L8_5eJ7_jNW2GsPQlxK",
                                    "node",
                                    "node");
                            Log.d(TAG, "onDataChange: creating user.." + userModel.toString());
                            ref.child(user.getUid()).setValue(userModel);

                            CurrentUser.buildUser(new CurrentUser.OnGetDataListener() {
                                @Override
                                public void onSuccess() {
                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(intent);

                                    finish();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }


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
