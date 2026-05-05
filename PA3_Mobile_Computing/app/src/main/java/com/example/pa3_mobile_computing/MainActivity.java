package com.example.pa3_mobile_computing;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private TextView txtStatus;
    private TextView txtCurrentLocation;
    private TextView txtSavedLocations;

    private Button btnStart;
    private Button btnStop;
    private Button btnClear;

    private FusedLocationProviderClient fusedLocationClient;

    private final BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (LocationService.ACTION_LOCATION_UPDATED.equals(intent.getAction())) {

                double latitude = intent.getDoubleExtra(
                        LocationService.EXTRA_LATITUDE,
                        0.0
                );

                double longitude = intent.getDoubleExtra(
                        LocationService.EXTRA_LONGITUDE,
                        0.0
                );

                String locationText =
                        "Current Location:\nLatitude: " + latitude +
                                "\nLongitude: " + longitude;

                txtCurrentLocation.setText(locationText);

                loadSavedData();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txtStatus);
        txtCurrentLocation = findViewById(R.id.txtCurrentLocation);
        txtSavedLocations = findViewById(R.id.txtSavedLocations);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnClear = findViewById(R.id.btnClear);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadSavedData();

        btnStart.setOnClickListener(view -> startTracking());

        btnStop.setOnClickListener(view -> stopTracking());

        btnClear.setOnClickListener(view -> clearSavedLocations());
    }

    private void startTracking() {

        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

        Intent serviceIntent = new Intent(this, LocationService.class);
        startService(serviceIntent);

        txtStatus.setText("Status: Tracking Active");

        Toast.makeText(
                this,
                "Location tracking started",
                Toast.LENGTH_SHORT
        ).show();

        getCurrentLocation();
    }

    private void stopTracking() {

        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);

        txtStatus.setText("Status: Tracking Stopped");

        Toast.makeText(
                this,
                "Location tracking stopped",
                Toast.LENGTH_SHORT
        ).show();

        loadSavedData();
    }

    private void getCurrentLocation() {

        if (!hasLocationPermission()) {
            txtCurrentLocation.setText("Current Location: Permission denied");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {

                    if (location != null) {
                        displayCurrentLocation(location);
                    } else {
                        txtCurrentLocation.setText(
                                "Current Location: Unavailable right now"
                        );
                    }

                    loadSavedData();
                })
                .addOnFailureListener(e -> txtCurrentLocation.setText(
                        "Current Location: Error getting location"
                ));
    }

    private void displayCurrentLocation(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        String locationText =
                "Current Location:\nLatitude: " + latitude +
                        "\nLongitude: " + longitude;

        txtCurrentLocation.setText(locationText);
    }

    private boolean hasLocationPermission() {

        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    private void loadSavedData() {

        SharedPreferences preferences = getSharedPreferences(
                LocationService.PREF_NAME,
                MODE_PRIVATE
        );

        boolean isTracking = preferences.getBoolean(
                LocationService.KEY_TRACKING_STATUS,
                false
        );

        String locationLog = preferences.getString(
                LocationService.KEY_LOCATION_LOG,
                ""
        );

        if (isTracking) {
            txtStatus.setText("Status: Tracking Active");
        } else {
            txtStatus.setText("Status: Tracking Stopped");
        }

        if (locationLog.isEmpty()) {
            txtSavedLocations.setText("No saved locations yet.");
        } else {
            txtSavedLocations.setText(locationLog);
        }
    }

    private void clearSavedLocations() {

        SharedPreferences preferences = getSharedPreferences(
                LocationService.PREF_NAME,
                MODE_PRIVATE
        );

        preferences.edit()
                .putString(LocationService.KEY_LOCATION_LOG, "")
                .apply();

        txtSavedLocations.setText("No saved locations yet.");

        Toast.makeText(
                this,
                "Saved locations cleared",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(
                LocationService.ACTION_LOCATION_UPDATED
        );

        ContextCompat.registerReceiver(
                this,
                locationUpdateReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        loadSavedData();

        if (hasLocationPermission()) {
            getCurrentLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            unregisterReceiver(locationUpdateReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(
                        this,
                        "Location permission granted",
                        Toast.LENGTH_SHORT
                ).show();

                startTracking();

            } else {

                txtStatus.setText("Status: Permission Denied");

                txtCurrentLocation.setText(
                        "Current Location: Permission denied"
                );

                Toast.makeText(
                        this,
                        "Location permission denied. Tracking cannot start.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}