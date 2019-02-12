package com.varunsaini.android.ga_basictransitions;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReciever extends BroadcastReceiver {

    public static boolean isplaying = false ;
    public static Ringtone r;
    public int request_id, backgroundColor;
    private static String ringtoneString;
    private static Uri ringtoneUri;
    private static String mVibrate;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHandler db = new DatabaseHandler(context);
        SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase("Alarmm", MODE_PRIVATE, null);
        request_id = intent.getIntExtra("request_code", -1);
        backgroundColor = db.getGroupColorByTrimmedRequestId(Integer.parseInt(String.valueOf(request_id).substring(3)));

        String[] ringtoneVibrateString = db.getRingtoneUriVibrate(request_id);
        if (ringtoneVibrateString[0] != null) {
            ringtoneUri = Uri.parse(ringtoneVibrateString[0]);
        }


        Log.d("cx", "onReceive: " + request_id);
        WakeLocker.acquire(context);


        if (!isplaying) {
            if (ringtoneUri == null) {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (ringtoneUri == null) {
                    ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
            }
            r = RingtoneManager.getRingtone(context, ringtoneUri);
            r.play();
            r.setLooping(true);
            isplaying = true;
            Intent myIntent = new Intent(context, AlarmRingActivity.class);
            myIntent.putExtra("recieved_request_code",request_id);
            myIntent.putExtra("group_color_for_background",backgroundColor);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myIntent);
            Toast.makeText(context, "OnReceive alarm test", Toast.LENGTH_SHORT).show();
            isplaying = true;

        }else{
            int trimmed_request_id = Integer.parseInt(String.valueOf(request_id).substring(3));
            Toast.makeText(context, "Alarm missed at " + db.getAlarmTimeFromAlarmRequestId(trimmed_request_id), Toast.LENGTH_SHORT).show();
            AlarmManager alarmMgr1 = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent alarmIntent1 = PendingIntent.getBroadcast(context, request_id, intent, 0);
            alarmMgr1.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+ (7*24*60*60*1000), alarmIntent1);
            NotificationManager mNotificationManager;

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context, "notify_001");

            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.bigText("Alarm missed at " + db.getAlarmTimeFromAlarmRequestId(trimmed_request_id));
            bigText.setBigContentTitle("Missed Alarm");
            mBuilder.setSmallIcon(R.drawable.baseline_alarm_on_white_18dp);
            mBuilder.setContentTitle("Your Title");
            mBuilder.setContentText("Your text");
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            mBuilder.setStyle(bigText);

            mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = "YOUR_CHANNEL_ID";
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationManager.createNotificationChannel(channel);
                mBuilder.setChannelId(channelId);
            }

            mNotificationManager.notify(0, mBuilder.build());
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            Notification n  = new Notification.Builder(context)
                    .setContentTitle("New mail from " + "test@gmail.com")
                    .setContentText("Subject")
                    .setSmallIcon(R.drawable.baseline_alarm_on_white_18dp).build();

            notificationManager.notify(0, n);

        }
    }

}