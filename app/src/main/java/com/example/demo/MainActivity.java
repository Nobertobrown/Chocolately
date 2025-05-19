package com.example.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    public String name, email;

    // Google's API for location services
    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Geocoder.GeocodeListener geocodeListener;
    private static final String TAG = "MainActivity";
    public TextView greetings, tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address;
    public SwitchCompat sw_gps, sw_locations_updates;
    public Button logoutBtn;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locations_updates = findViewById(R.id.sw_locationsupdates);
        greetings = findViewById(R.id.intro);
        logoutBtn = findViewById(R.id.logoutBtn);

        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");

        //Setup location request
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, DEFAULT_UPDATE_INTERVAL * 1000)
                .setMinUpdateIntervalMillis(FAST_UPDATE_INTERVAL * 1000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    updateUIValues(location);
                }
            }
        };

        geocodeListener = new Geocoder.GeocodeListener() {
            @Override
            public void onGeocode(@NonNull List<Address> addresses) {
                // This method is called when geocoding is successful.
                // 'addresses' contains a list of Address objects.
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);

                    // Get the address details.
                    String addressString = address.getAddressLine(0); // Get the full address.

                    tv_address.setText(addressString);

                } else {
                    // Handle the case where no addresses were found.
                    tv_address.setText("No addresses found.");
                }
            }

            @Override
            public void onError(String errorMessage) {
                // This method is called if there's an error during geocoding.
                Log.e(TAG, "Error during geocoding: " + errorMessage);
            }
        };

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Credential Manager
        credentialManager = CredentialManager.create(this);

        logoutBtn.setOnClickListener(this::signOut);

        sw_gps.setOnClickListener(v -> {
            if (sw_gps.isChecked()) {
                locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY).build();
                tv_sensor.setText("Using GPS Sensors");
            } else {
                locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY).build();
                tv_sensor.setText("Using Cell Tower + Wifi");
            }
        });

        sw_locations_updates.setOnClickListener(v -> {
            if (sw_locations_updates.isChecked()) {
                tv_updates.setText("Location is being tracked");
                startLocationUpdates();
            } else {
                tv_updates.setText("Location is not being tracked");
                tv_lat.setText("Not tracking location");
                tv_lon.setText("Not tracking location");
                tv_speed.setText("Not tracking location");
                tv_altitude.setText("Not tracking location");
                tv_accuracy.setText("Not tracking location");
                tv_address.setText("Not tracking location");

                stopLocationUpdates();
            }
        });

        updateGPS();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (name != null) {
            greetings.setText(String.format("Hello %s", name));
        } else {
            greetings.setText(String.format("Hello %s", email));
        }
    }

    private void requestPermissions() {
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {

                            Boolean fineLocationGranted = null;
                            Boolean coarseLocationGranted = null;

                            fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);

                            if (Boolean.FALSE.equals(coarseLocationGranted) || Boolean.FALSE.equals(fineLocationGranted)) {
                                // No location access granted.
                                Toast.makeText(this, "This app requires location permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                );

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            updateUIValues(location);
                        }
                    });
        } else {
            requestPermissions();
        }
    }

    private void updateUIValues(Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if (location.hasAltitude()) {
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        } else {
            tv_altitude.setText("Not Available");
        }

        if (location.hasSpeed()) {
            tv_speed.setText(String.valueOf(location.getSpeed()));
        } else {
            tv_speed.setText("Not Available");
        }

        Geocoder geocoder = new Geocoder(this);

        try {
            geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1, geocodeListener);
        } catch (Exception e) {
            Log.e(TAG, "Error during geocoding: " + e.getLocalizedMessage());
            tv_address.setText("Not Available");
        }
    }

    private void signOut(View view) {
        // Firebase sign out
        mAuth.signOut();

        // When a user signs out, clear the current user credential state from all credential providers.
        ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();
        credentialManager.clearCredentialStateAsync(
                clearRequest,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(@NonNull Void result) {
                        updateUI();
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        Log.e(TAG, "Couldn't clear user credentials: " + e.getLocalizedMessage());
                    }
                });
    }

    private void updateUI() {
        Intent intent = new Intent(MainActivity.this, LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}