package com.spauldhaliwal.homestead;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

public class FirebaseMessagingNotificationService extends FirebaseMessagingService {

    //TODO Implement read recepits. Remove old notifications from database.

    private static final String TAG = "FirebaseMessagingNotifi";

    Map notificationData;

    String notificationType;
    String notificationSender;
    String notificationSenderUid;
    String notificationBody;

    String GROUP_KEY_CHAT = "com.spauldhaliwal.homestead.CHAT_NOTIFICATION";
    String GROUP_KEY_JOBS = "com.spauldhaliwal.homestead.JOB_NOTIFICATION";

    String channelId;

    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);
        Log.d("msg", "onMessageReceived: " + remoteMessage.getData().toString() + CurrentUser.getUid());

        notificationData = remoteMessage.getData();
        Log.d(TAG, "onMessageReceived: NotificationData: " + notificationData);

        notificationType = notificationData.get("type").toString();
        notificationSender = notificationData.get("sender").toString();
        notificationSenderUid = notificationData.get("sender_uid").toString();
        notificationBody = notificationData.get("body").toString();

        String homesteadMessageId = HomesteadsContract.HOMESTEAD_MESSAGES_ID;

        String channelId;
        if (!notificationSenderUid.equals(CurrentUser.getUid())
                && !notificationSenderUid.equals(homesteadMessageId)) {
            //only send in-app notifications if the senderUid and currentUserUid are not the same

            if (notificationType.equals(MessagesContract.TYPE)) {

                if (//Check to see if current activity is Chat Activity. No need to send notifications if true.
                        ActivityState.getCurrentActivity() != null &&
                                !ActivityState.getCurrentActivity()
                                        .getClass()
                                        .getSimpleName()
                                        .equals(ChatActivity.class.getSimpleName())) {

                    sendChatNotification();
                } else if (ActivityState.getCurrentActivity() == null) {
                    sendChatNotification();
                }

            } else if (notificationType.equals(JobsContract.TYPE)) {
                if (!notificationSenderUid.equals(CurrentUser.getUid())) {
                    Log.d(TAG, "onMessageReceived: notificationSenderUid: " + notificationSenderUid);
                    //only send in-app notifications if the senderUid and currentUserUid are not the same
                    sendJobNotification();
                }
            }
        }
    }

    public void sendChatNotification() {
        channelId = "Chat Notification";

        Intent intent = new Intent(this, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.chat_notification)
                .setContentTitle(notificationSender + ": ")
                .setContentText(notificationBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setGroup(GROUP_KEY_CHAT)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    public void sendJobNotification() {
        channelId = "New Jobs Notification";
        Log.d(TAG, "sendJobNotification: notificationData: " + notificationData);
        String jobNotificationTitle = notificationData.get("jobTitle").toString();
        String jobNotificationDescription = notificationData.get("jobDescription").toString();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.base_notification)
                .setContentTitle("New job added by " + notificationSender + ": " + jobNotificationTitle)
                .setContentText(jobNotificationDescription)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setGroup(GROUP_KEY_JOBS)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

}
