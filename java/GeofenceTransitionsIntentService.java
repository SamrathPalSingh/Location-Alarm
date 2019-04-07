package com.example.android.locationalarm;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GeofenceTransitionsIntentService extends IntentService {
final String TAG = "In Intent Service";
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }
    String CHANNEL_ID = "1";
    int notificationId = 11;
    MediaPlayer audio;
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            int errorMessage = (geofencingEvent.getErrorCode() );
            Log.e(TAG, "Error Code" + errorMessage);
            return;
    }
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

      audio = MediaPlayer.create(this, R.raw.alrm);
            audio.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    audio.stop();
                    }
                });
            Intent clickIntent = new Intent(this, MapsActivity.class);
            clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            clickIntent.putExtra("Reached", "Destination");
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, clickIntent, 0);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.iconapp)
                    .setContentTitle("Location Alarm")
                    .setContentText("You have reached your destination")
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(notificationId, mBuilder.build());
            audio.start();
            Toast.makeText(this, "You have reached your destination", Toast.LENGTH_LONG).show();
        }
   }
}
