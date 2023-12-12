package com.stockholmiot.proxyguide.ui.map;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.stockholmiot.proxyguide.R;

import java.util.Random;

public class NotificationHelper extends ContextWrapper {

    private static final String TAG = "NotificationHelper";
    private static final int YOUR_PERMISSION_REQUEST_CODE = 2;
    private Context context;
    public NotificationHelper(Context base) {
        super(base);
        context = base;
        createChannels();
    }

    private String CHANNEL_NAME = "High priority channel";
    private String CHANNEL_ID = "com.example.notifications_01";
    private String id = "com.stockholmiot.proxyguide";

    private void createChannels() {


        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel(id);
            if (channel == null) {
                channel = new NotificationChannel(id, "Channel Title", NotificationManager.IMPORTANCE_HIGH);
                // Config notification channel
                channel.setDescription("Channel description");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 1000, 200, 340});
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void sendHighPriorityNotification(String title, String body, Class activityName) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the missing permission
            //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, YOUR_PERMISSION_REQUEST_CODE);
            return;
        }

        Intent notificationIntent = new Intent(this, activityName);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.drawable.img_app_logo)
                .setStyle(new NotificationCompat.BigPictureStyle())
                .setContentTitle("Title")
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{100, 1000, 200, 340})
                .setAutoCancel(false) // True: touch on notification menu dismissed, but swipe to dismiss
                .setTicker("Notification");

        builder.setContentIntent(contentIntent);
        NotificationManagerCompat m = NotificationManagerCompat.from(getApplicationContext());

        m.notify(1, builder.build());

    }

    private int generateRandomNotificationId() {
        // Use Random to generate a random notification ID
        return new Random().nextInt(Integer.MAX_VALUE);
    }

}
