package it.uniba.dib.sms2324FF2.notifications;


import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.patient.PatientActivity;
import com.google.android.material.snackbar.Snackbar;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static boolean isAppInForeground = false;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getNotification()!=null){
            // Show the notification
            String notificationBody = remoteMessage.getNotification().getBody();
            String notificationTitle = remoteMessage.getNotification().getTitle();

            if (isAppInForeground) {
                showInAppNotification(notificationBody);
            } else {
                sendNotification(notificationTitle, notificationBody);
            }

        }
    }

    private void showInAppNotification(String body) {
        Activity foregroundActivity = AppLifecycleHandler.getForegroundActivity();
        if (foregroundActivity != null) {
            // Utilizza un CoordinatorLayout per garantire una corretta visualizzazione della Snackbar
            View rootView = foregroundActivity.getWindow().getDecorView().findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(rootView, body, 4000);

            // Imposta la gravità per far visualizzare la Snackbar nella parte superiore
            View snackbarView = snackbar.getView();
            CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.MATCH_PARENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.TOP;
            snackbarView.setLayoutParams(params);

            // Personalizzo snackbar
            View customView = snackbar.getView();
            TextView textView = customView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setMaxLines(5); // Imposta il numero massimo di linee desiderato
            textView.setTextColor(getColor(R.color.white));
            Typeface customTypeface = ResourcesCompat.getFont(getApplicationContext(), R.font.bubblegum_sans);
            textView.setTypeface(customTypeface);
            snackbar.setText(getString(R.string.Notification) + "\n" + body);

            // Mostra la Snackbar
            snackbar.show();
        }
    }





    private void sendNotification(String title, String body) {

            Intent intent = new Intent(this, PatientActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE);

            String channelId = "fcm_default_channel";
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_notifications)
                            .setContentTitle(title)
                            .setContentText(body)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
        NotificationChannel channel = new NotificationChannel(
                getString(R.string.default_notification_channel_id),
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH);

        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    public static class AppLifecycleHandler implements Application.ActivityLifecycleCallbacks {

        private static int numStarted = 0;
        private static Activity foregroundActivity;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (numStarted == 0) {
                // L'app è diventata visibile
                isAppInForeground = true;
                foregroundActivity = activity;
            }
            numStarted++;
        }

        @Override
        public void onActivityResumed(Activity activity) {
            foregroundActivity = activity;
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            numStarted--;
            if (numStarted == 0) {
                // L'app è stata nascosta
                isAppInForeground = false;
                foregroundActivity = null;
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        public static Activity getForegroundActivity() {
            return foregroundActivity;
        }
    }

}











