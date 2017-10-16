package com.github.invghost.neostream;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.ArrayList;

public class CheckOnlineStatus extends IntentService {
    public CheckOnlineStatus() {
        super("CheckOnlineStatus");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!settings.getBoolean("enable_live_notifications", false))
            return;

        ArrayList<String> followedStreamers = UserData.getFollowing(getApplicationContext());
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
