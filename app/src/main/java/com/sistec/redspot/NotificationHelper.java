package com.sistec.redspot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String MY_CHANNEL_ID = "redSpotDangerWarning";
    private static final String MY_CHANNEL_NAME = "RedSpot Alert";
    private static final String MY_CHANNEL_DESC = "Give alert when you are in accident prone area";
    private static final int MY_NOTIFICATION_ID = 111;
    private static NotificationManagerCompat notificationManagerCompat;
    private static NotificationCompat.Builder mBuilder;
    private static TextToSpeech textToSpeech;

    public static void startSpeechWarning(final Context context, final String warningData){
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
                    Toast.makeText(context, "Initialization success.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
                textToSpeech.setSpeechRate(0.8f);
                textToSpeech.speak(warningData, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }



    public static void showNewNotification(Context context, String title, String desc, String extraDesc){
        NotificationManagerCompat notificationManagerCompat;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, MY_CHANNEL_ID);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 ,intent, 0);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.ic_notification);
        mBuilder.setColor(context.getResources().getColor(R.color.danger_highest));
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(desc);
        mBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(extraDesc + "\nClick this notification to know all danger area information."));
        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationManagerCompat = NotificationManagerCompat.from(context);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(MY_CHANNEL_ID, MY_CHANNEL_NAME, importance);
            channel.setDescription(MY_CHANNEL_DESC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManagerCompat.notify(MY_NOTIFICATION_ID, mBuilder.build());
        startSpeechWarning(context, "Warning. You are in accidental prone area. Please drive safe." );

    }
    public static void hideOldNotification(Context context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        if (textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

}
