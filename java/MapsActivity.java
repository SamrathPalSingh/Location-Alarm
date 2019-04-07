package com.example.android.locationalarm;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GnssStatus;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static android.view.View.GONE;
import static com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT;
import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;
import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mMap;
    private LatLng latlng;
    String CHANNEL_ID = "1";
    Boolean mapCheck;
    int locationInt;
    final int MY_PERMISSIONS_REQUEST_LOCATION = 101;
    LocationCallback mLocationCallback;
    CircleOptions circleOptions;
    Marker marker;
    FloatingActionButton fab2;
    final String geoStringRequestId = "TINTIN";
    final int RADIUS = 50;
    PendingIntent mGeofencePendingIntent;
    ArrayList<Geofence> mGeofenceList;
    final int REQUEST_CHECK_SETTINGS = 1;
    Marker curLoc;
    LocationRequest mLocationRequest;
    final String TAG = "In maps Activity:  ";
    private GeofencingClient mGeofencingClient;
    FloatingActionButton floatingActionButton;
    int neo;
    Boolean ns;
    Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ns = isNetworkAvailable();
        notificationChannel();
        locationPermission();
        neo = 0;
        if (ns) {
            if(locationInt == 1){
            fab2 = findViewById(R.id.fab2);
            TextView noNet = (TextView) findViewById(R.id.noNet);
            noNet.setVisibility(GONE);
            floatingActionButton = findViewById(R.id.fab);
            floatingActionButton.show();
            createLocationRequest();
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            task.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MapsActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                        }
                    }
                }
            });


            task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    mGeofencingClient = LocationServices.getGeofencingClient(MapsActivity.this);
                    mGeofenceList = new ArrayList<Geofence>();
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
                    mLocationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null) {
                                return;
                            }
                            for (Location location : locationResult.getLocations()) {
                                if (curLoc != null) curLoc.remove();
                                curLoc = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title("Current Location"));
                            }
                            if (neo == 0) {
                                mMap.moveCamera(newLatLngZoom(
                                        new LatLng(locationResult.getLastLocation().getLatitude(),
                                                locationResult.getLastLocation().getLongitude()), 16));
                                neo = 1;
                            }
                            RelativeLayout relativeLayout = findViewById(R.id.maps);
                            relativeLayout.setVisibility(View.VISIBLE);
                            ProgressBar pb = findViewById(R.id.progress);
                            pb.setVisibility(GONE);
                        }
                    };
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(MapsActivity.this);
                    }
                }
            });
    } else {
                TextView noNet =  findViewById(R.id.noNet);
                noNet.setText("Cannot access Location. Goto Settings > Apps > Location Alarm > Permissions to grant location permission");
                noNet.setVisibility(View.VISIBLE);
                ProgressBar pb = findViewById(R.id.progress);
                pb.setVisibility(GONE);

            }
        } else {
            TextView noNet = findViewById(R.id.noNet);
            noNet.setText("NO INTERNET CONNECTION");
            noNet.setVisibility(View.VISIBLE);
            ProgressBar pb = findViewById(R.id.progress);
            pb.setVisibility(GONE);

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mapCheck == null) {
            ns = isNetworkAvailable();
            locationPermission();
            neo = 0;
            if (ns) {
                if(locationInt == 1){
                fab2 = findViewById(R.id.fab2);
                TextView noNet = (TextView) findViewById(R.id.noNet);
                noNet.setVisibility(GONE);
                floatingActionButton = findViewById(R.id.fab);
                floatingActionButton.show();
                createLocationRequest();
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);
                SettingsClient client = LocationServices.getSettingsClient(this);
                Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
                task.addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ResolvableApiException) {
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(MapsActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sendEx) {
                            }
                        }
                    }
                });


                task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        mGeofencingClient = LocationServices.getGeofencingClient(MapsActivity.this);
                        mGeofenceList = new ArrayList<Geofence>();
                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        mLocationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                if (locationResult == null) {
                                    return;
                                }
                                for (Location location : locationResult.getLocations()) {
                                    if (curLoc != null) curLoc.remove();
                                    curLoc = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title("Current Location"));
                                }
                                if (neo == 0) {
                                    mMap.moveCamera(newLatLngZoom(
                                            new LatLng(locationResult.getLastLocation().getLatitude(),
                                                    locationResult.getLastLocation().getLongitude()), 16));
                                    neo = 1;
                                }
                                RelativeLayout relativeLayout = findViewById(R.id.maps);
                                relativeLayout.setVisibility(View.VISIBLE);
                                ProgressBar pb = findViewById(R.id.progress);
                                pb.setVisibility(GONE);
                            }
                        };
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        if (mapFragment != null) {
                            mapFragment.getMapAsync(MapsActivity.this);
                        }
                    }
                });
         }
         else{
                 TextView noNet =  findViewById(R.id.noNet);
                 noNet.setText("Cannot access Location. Goto Settings > Apps > Location Alarm > Permissions to grant location permission");
                 noNet.setVisibility(View.VISIBLE);
                 ProgressBar pb = findViewById(R.id.progress);
                 pb.setVisibility(GONE);
             }
            } else {
                TextView noNet = findViewById(R.id.noNet);
                noNet.setText("NO INTERNET CONNECTION");
                noNet.setVisibility(View.VISIBLE);
                ProgressBar pb = findViewById(R.id.progress);
                pb.setVisibility(GONE);
            }
        }
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
      /*  String startedFrom = getIntent().getStringExtra("started_from");
       if(startedFrom != null){
           if(startedFrom.equals("Destination")){
           Toast.makeText(this, "You have reached your destination", Toast.LENGTH_LONG).show();}
       }*/
        mapCheck = true;
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                latlng = new LatLng(point.latitude, point.longitude);
                if (circle!= null){
                circle.remove();
                }
                if (marker != null) {
                    marker.remove();
                }
                    circleOptions = new CircleOptions().center(new LatLng(point.latitude, point.longitude));
                    circleOptions.radius(RADIUS);
                    circleOptions.fillColor(R.color.area).strokeColor(Color.TRANSPARENT);
                    circle = mMap.addCircle(circleOptions);
                    marker = mMap.addMarker(new MarkerOptions().position(latlng).title("Alarm Location"));
                marker.showInfoWindow();
            }
        });
startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public void startTracking(View view) {
        if (marker != null){
            if (mGeofenceList.size() != 0) mGeofenceList.clear();
            mGeofenceList.add(new Geofence.Builder().setRequestId(geoStringRequestId).setCircularRegion(marker.getPosition().latitude, marker.getPosition().longitude, RADIUS).setExpirationDuration(NEVER_EXPIRE).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | GEOFENCE_TRANSITION_EXIT).build());
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent()).addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MapsActivity.this, "Alarm Started", Toast.LENGTH_SHORT).show();
                        floatingActionButton.hide();
                        fab2.show();
                        mMap.setOnMapClickListener(null);
                        }
                    }).addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MapsActivity.this, "Geofence not available", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Set the marker first", Toast.LENGTH_SHORT).show();
        }
    }
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void notificationChannel(){

            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = getString(R.string.channel_name);
                String description = getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                assert notificationManager != null;
                notificationManager.createNotificationChannel(channel);
            }

    }
    public void stopTracking(View view){
       marker.remove();
       circle.remove();
       fab2.hide();
       marker = null;
       floatingActionButton.show();
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MapsActivity.this, "Alarm Stopped", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() { //setting a marker on the map on clicking the map
            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public void onMapClick(LatLng point) {
                latlng = new LatLng(point.latitude, point.longitude);
                if (circle!= null){
                    circle.remove();
                }
                if (marker != null) {
                    marker.remove();
                }
                circleOptions = new CircleOptions().center(new LatLng(point.latitude, point.longitude));
                circleOptions.radius(RADIUS);
                circleOptions.fillColor(R.color.area).strokeColor(Color.TRANSPARENT);
                circle = mMap.addCircle(circleOptions);
                marker = mMap.addMarker(new MarkerOptions().position(latlng).title("Alarm Location"));
                marker.showInfoWindow();
            }
        });
    }
    private void locationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
               locationInt = 0;
        } else {locationInt = 1;}

    }
}

