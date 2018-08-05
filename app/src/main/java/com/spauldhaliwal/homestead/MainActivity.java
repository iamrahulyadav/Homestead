package com.spauldhaliwal.homestead;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.auth.AuthUI;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    SharedPreferences prefs = null;
    Vibrator vibe;

    // When requested, this adapter returns a HomeBoardFragment,
    // representing an object in the collection.
    ViewPager mViewPager;
    HomesteadBoardPagerAdapter mAdapter;
    Drawable userProfileDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity onCreate: starts");
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        prefs = getSharedPreferences("com.spauldhaliwal.homestead", MODE_PRIVATE);

        final FirebaseAuth auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitleTextColor(616161);
        setSupportActionBar(toolbar);


        final ActionBar actionBar = getActionBar();


        FloatingActionButton fab = findViewById(R.id.fab);
        Log.d(TAG, "onCreate: chatMenuId: " + R.id.menuChatItem);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                startActivity(intent);
            }
        });


        //Set overflow icon to user's profile image
        Glide.with(this)
                .load(CurrentUser.getProfileImage())
                .apply(RequestOptions.circleCropTransform().override(65, 65))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                                                @Nullable Transition<? super Drawable> transition) {
                        userProfileDrawable = resource;
                        toolbar.setOverflowIcon(userProfileDrawable);
                    }
                });

        mAdapter = new HomesteadBoardPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.content_pager);
        mViewPager.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        super.onCreate(savedInstanceState);
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
                String homesteadName = CurrentUser.getHomesteadName();
                String inviteTitle = "Join " + CurrentUser.getName() + "'s Homestead!";
                String inviteDescription = CurrentUser.getName() + " has invited you to join their homestead: " + CurrentUser.getHomesteadName() + ". Follow the link to accept their invitation!";
                Uri inviteImageUrl = Uri.parse(CurrentUser.getProfileImage());

                Task<ShortDynamicLink> dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse("https://homesteadapp.com/?homesteadid=" + homesteadId
                                + "&userid="
                                + userId))

                        .setDynamicLinkDomain("homesteadapp.page.link")
                        // Open links with this app on android
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                        // Open links with this app on iOS
                        .setIosParameters(new DynamicLink.IosParameters
                                .Builder("com.spauldhaliwal.homestead").build())
                        .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(inviteTitle)
                                .setDescription(inviteDescription)
                                .setImageUrl(inviteImageUrl)
                                .build())
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

    @Override
    protected void onResume() {
        super.onResume();
        ActivityState.setActivity(this);

        //Check if app is in first run
        if (prefs.getBoolean("firstRun", true)) {
            beginOnBoarding();
            prefs.edit().putBoolean("firstRun", false).apply();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ActivityState.clearActivity(this);
    }

    public void beginOnBoarding() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        Drawable homesteadIcon = getResources().getDrawable(R.drawable.homestead_launcher);
        Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        int centerX = size.x;

        int centerY = size.y;
        Log.d(TAG, "beginOnBoarding: centerX: " + centerX);
        Rect rect = new Rect(centerX, 0, 0, centerY);


        TapTargetSequence onBoardingSequence = new TapTargetSequence(this).targets(
                TapTarget.forView(findViewById(R.id.fab), "Tap here to create a new task.", "You can make it either public, and share it with your Homestead, or private.")
                        .outerCircleColor(R.color.colorAccent)
                        .tintTarget(false)
                        .cancelable(false)
                ,
                TapTarget.forToolbarMenuItem(toolbar, R.id.menuChatItem, "Tap here to access Homestead Chat.", "You'll be able to chat with the other members of your Homestead")
                        .outerCircleColor(R.color.colorAccent)
                        .tintTarget(false)
                        .cancelable(false)
                ,
                TapTarget.forToolbarOverflow(toolbar, "Tap here to access your menu.")
                        .outerCircleColor(R.color.colorAccent)
                        .tintTarget(false)
                        .cancelable(false)
                ,
                TapTarget.forBounds(rect, "Thank you for choosing Homestead.", "Tap here to begin.")
                        .icon(homesteadIcon)
                        .outerCircleColor(R.color.colorAccent)
                        .tintTarget(false)
                        .cancelable(false))
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {

                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {

                    }
                });

        onBoardingSequence.start();

    }
}
