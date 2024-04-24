package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.antitheft.R;
import com.example.myapplication.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.util.Date;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Prioritize data payload.

        if (remoteMessage.getData().size() > 0) {
            // You can use different keys to identify the type of notification.
            String type = remoteMessage.getData().get("type");
            if ("imageUpload".equals(type)) {
                String title = remoteMessage.getData().get("title");
                String messageBody = remoteMessage.getData().get("message");
                sendNotificationImage(title, messageBody);
            } else if ("codePin".equals(type)) {
                String title = remoteMessage.getData().get("title");
                String messageBody = remoteMessage.getData().get("message");
                sendNotificationCodePin(title, messageBody);
            } else if ("iffail".equals(type)) {
                String title = remoteMessage.getData().get("title");
                String messageBody = remoteMessage.getData().get("message");
                sendNotificationiffail(title, messageBody);

            } else if ("enrollInit".equals(type)) {
                String title = remoteMessage.getData().get("title");
                String messageBody = remoteMessage.getData().get("message");
                sendNotificationenrollInit(title, messageBody);
            }
            // Check if message contains a notification payload and handle it as a fallback.
            if (remoteMessage.getNotification() != null) {
                sendNotificationImage(remoteMessage.getNotification().getTitle(),
                        remoteMessage.getNotification().getBody());
            }
        }
    }

    private void sendNotificationImage(String title, String messageBody) {

        SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Create a unique key for each notification based on current time
        String notifKey = "notif_" + System.currentTimeMillis();
        editor.putString(notifKey + "_title", title);
        editor.putString(notifKey + "_message", messageBody);
        editor.putString(notifKey + "_timestamp", DateFormat.getDateTimeInstance().format(new Date()));
        editor.apply();

        // Create an explicit intent for your MainActivity
        Intent intent = new Intent(this, MainActivity.class);

        // Include some extra data to indicate where to navigate
        intent.putExtra("navigateTo", "gallery");

        // Ensure the back stack is managed correctly
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = MainActivity.CHANNEL_ID;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_bell)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
    private void sendNotificationenrollInit(String title, String messageBody) {

        SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Create a unique key for each notification based on current time
        String notifKey = "notif_" + System.currentTimeMillis();
        editor.putString(notifKey + "_title", title);
        editor.putString(notifKey + "_message", messageBody);
        editor.putString(notifKey + "_timestamp", DateFormat.getDateTimeInstance().format(new Date()));
        editor.apply();

        // Create an explicit intent for your MainActivity
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("navigateTo", "notifications");

        // Ensure the back stack is managed correctly
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = MainActivity.CHANNEL_ID;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_bell)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
    private void sendNotificationCodePin(String title, String messageBody) {
        SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Create a unique key for each notification based on current time
        String notifKey = "codePin_" + System.currentTimeMillis();
        editor.putString(notifKey + "_title", title);
        editor.putString(notifKey + "_message", messageBody);
        // Save a timestamp along with the notification
        editor.putString(notifKey + "_timestamp", DateFormat.getDateTimeInstance().format(new Date()));
        editor.apply();

        // Create an explicit intent for your MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigateTo", "notifications");

        // Directs to the HomeFragment where they can enter the pin
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = MainActivity.CHANNEL_ID;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("New Pin Code")
                .setContentText("A new pin code has been set.")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the notification channel if it does not exist
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Code Pin Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Notify the user
        notificationManager.notify(0, notificationBuilder.build()); // Use a different ID than for image notifications
    }


    private void sendNotificationiffail(String title, String messageBody) {

        SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Create a unique key for each notification based on current time
        String notifKey = "notif_" + System.currentTimeMillis();
        editor.putString(notifKey + "_title", title);
        editor.putString(notifKey + "_message", messageBody);
        editor.putString(notifKey + "_timestamp", DateFormat.getDateTimeInstance().format(new Date()));
        editor.apply();


        // Create an explicit intent for your MainActivity
        Intent intent = new Intent(this, MainActivity.class);

        // Include some extra data to indicate where to navigate
        intent.putExtra("navigateTo", "dashboard");

        // Ensure the back stack is managed correctly
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = MainActivity.CHANNEL_ID;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_bell)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }


}

