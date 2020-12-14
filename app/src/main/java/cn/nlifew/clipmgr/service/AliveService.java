package cn.nlifew.clipmgr.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.ui.about.AboutActivity;

public class AliveService extends Service {
    private static final String TAG = "AliveService";

    private static final int NOTIFICATION_ID = 10;
    private static final String CHANNEL_ID = "package_rules_provider";

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        Notification.Builder builder = makeBuilder(this);
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    private Notification.Builder makeBuilder(Context context) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(NOTIFICATION_SERVICE);
        if (nm == null) {
            return null;
        }

        final Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = makeChannel(context);
            nm.createNotificationChannel(channel);
            builder = new Notification.Builder(context, CHANNEL_ID);
        }
        else {
            builder = new Notification.Builder(context);
        }

        Resources res = context.getResources();

        builder.setContentTitle(res.getText(R.string.alive_notification_title));
        builder.setContentText(res.getText(R.string.alive_notification_content));
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher_round);

        PendingIntent pi = PendingIntent.getActivity(this,
                10, new Intent(this, AboutActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        return builder;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static NotificationChannel makeChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                context.getString(R.string.alive_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setDescription(context.getString(R.string.alive_channel_description));
        return channel;
    }
}