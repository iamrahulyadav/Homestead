package com.spauldhaliwal.homestead;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.spauldhaliwal.homestead.MainActivity;
import com.spauldhaliwal.homestead.R;

import java.util.Map;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

public class FirebaseMessagingNotificationService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessagingNotifi";

    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);
        Log.d("msg", "onMessageReceived: " + remoteMessage.getData().toString());


        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Map notification = remoteMessage.getData();

        String notificationType = notification.get("type").toString();
        String notificationSender = notification.get("sender").toString();

        String GROUP_KEY_CHAT = "com.spauldhaliwal.homestead.CHAT_NOTIFICATION";
        String GROUP_KEY_JOBS = "com.spauldhaliwal.homestead.JOB_NOTIFICATION";

        String channelId;

        if (notificationType.equals(MessagesContract.TYPE)) {
            channelId = "Chat Notification";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(notificationSender + ": ")
                    .setContentText(remoteMessage.getNotification().getBody())
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

        } else if (notificationType.equals(JobsContract.TYPE)) {

            channelId = "New Jobs Notification";
            String jobNotificationTitle = notification.get("jobTitle").toString();
            String jobNotificationDescription = notification.get("jobDescription").toString();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("New job added by " + notificationSender + ": " + jobNotificationTitle)
                    .setContentText(jobNotificationDescription)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
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

}
