package com.example.myapplication.ui.gps_display;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.antitheft.R;

import com.example.antitheft.databinding.ActivityMapBinding;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Declare a GoogleMap object at the class level
    private boolean isFirebaseInitialized = false; // Flag to check Firebase initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
            isFirebaseInitialized = true; // Set the flag to true after successful initialization
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }


        ActivityMapBinding binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap; // Initialize the GoogleMap object

        // Check if Firebase has been initialized
        if (!isFirebaseInitialized) {
            return; // If Firebase is not initialized, do not proceed
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/");
        DatabaseReference databaseReference = database.getReference("gpsData");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double latitude = dataSnapshot.child("Latitude").getValue(Double.class);
                    Double longitude = dataSnapshot.child("Longitude").getValue(Double.class);

                    if (latitude != null && longitude != null) {
                        LatLng newLocation = new LatLng(latitude, longitude); // No need to call doubleValue() explicitly
                        // Use newLocation for updating the map
                    } else {
                        // Handle the case where latitude or longitude or both are null
                        Log.w("MapsActivity", "Latitude or Longitude is null. Check your database structure or the data being set.");
                    }

                    String timestamp = dataSnapshot.child("timestamp").getValue(String.class); // Retrieve the timestamp

                    // Construct a snippet string that includes the timestamp
                    String snippet = "Updated at: " + timestamp;

                    // Update the map with the new coordinates
                    LatLng newLocation = new LatLng(latitude, longitude);
                    mMap.clear(); // Clear the old marker

                    mMap.addMarker(new MarkerOptions().position(newLocation).title("Vehicle's Last Known Location").snippet(snippet));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 12.0f));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
}
