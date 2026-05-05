package com.example.pa3_mobile_computing;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationService extends Service {

    public static final String PREF_NAME = "LocationLoggerPrefs";
    public static final String KEY_LOCATION_LOG = "location_log";
    public static final String KEY_TRACKING_STATUS = "tracking_status";

    public static final String ACTION_LOCATION_UPDATED =
            "com.example.pa3_mobile_computing.LOCATION_UPDATED";

    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        saveLocation(location);
                        sendLocationBroadcast(location);
                    }
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        saveTrackingStatus(true);
        startLocationUpdates();

        return START_STICKY;
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            stopSelf();
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000
        )
                .setMinUpdateIntervalMillis(3000)
                .build();

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
        );
    }

    private void saveLocation(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        String timeStamp = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        ).format(new Date());

        String newEntry =
                timeStamp +
                        "\nLatitude: " + latitude +
                        "\nLongitude: " + longitude +
                        "\n\n";

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String oldLog = preferences.getString(KEY_LOCATION_LOG, "");

        String updatedLog = newEntry + oldLog;

        preferences.edit()
                .putString(KEY_LOCATION_LOG, updatedLog)
                .apply();
    }

    private void sendLocationBroadcast(Location location) {

        Intent intent = new Intent(ACTION_LOCATION_UPDATED);

        intent.putExtra(EXTRA_LATITUDE, location.getLatitude());
        intent.putExtra(EXTRA_LONGITUDE, location.getLongitude());

        sendBroadcast(intent);
    }

    private void saveTrackingStatus(boolean isTracking) {

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        preferences.edit()
                .putBoolean(KEY_TRACKING_STATUS, isTracking)
                .apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        saveTrackingStatus(false);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}