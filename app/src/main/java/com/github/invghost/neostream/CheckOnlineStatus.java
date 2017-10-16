package com.github.invghost.neostream;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Set;

public class CheckOnlineStatus extends IntentService {
    public CheckOnlineStatus() {
        super("CheckOnlineStatus");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences settings = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        if (!settings.getBoolean("enable_live_notifications", false))
            return;

        Set<String> followedStreamers = settings.getStringSet("followed", null);
        if(followedStreamers != null) {
            for (String streamer : followedStreamers) {
                if(TwitchAPI.IsOnline(streamer) && TwitchAPI.IsStreamUnique(getApplicationContext(), streamer)) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.ic_search_black_24dp)
                                    .setContentTitle(streamer + " is now live!")
                                    .setContentText(TwitchAPI.GetStatus(streamer));

                    Intent resultIntent = new Intent(this, MainActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

                    mNotificationManager.notify(1, mBuilder.build());
                }
            }
        }
    }
}
