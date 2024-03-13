package com.example.myapplication.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.antitheft.R;
import com.example.antitheft.databinding.FragmentDashboardBinding;
import com.example.myapplication.ui.gallery.ImageAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PERMISSIONS_REQUEST_MANAGE_STORAGE = 2;
    private Switch alarmSwitch;
    private ArrayList<Uri> imageUrls = new  ArrayList<>();
    private ImageAdapter imageAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;



    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Button displayImagesButton = view.findViewById(R.id.btnDisplayImages);

        displayImagesButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_dashboardFragment_to_galleryFragment);
        });

// Initialize Firebase Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize your button
       Button btnForceAuthenticate = view.findViewById(R.id.btnForceAuthenticate);


        // Listen for changes in the database
        databaseReference.child("ForceAuthorization").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean forceAuthorization = snapshot.getValue(Boolean.class);
                if (forceAuthorization != null && forceAuthorization) {
                    // Disable button if ForceAuthorization is true
                    btnForceAuthenticate.setEnabled(false);
                    btnForceAuthenticate.setBackgroundColor(Color.GRAY); // Optional: change button color
                } else {
                    // Enable button if ForceAuthorization is false or null
                    btnForceAuthenticate.setEnabled(true);
                    btnForceAuthenticate.setBackgroundColor(Color.parseColor("#FF6200EE")); // Optional: reset button color
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Set the button click listener
        btnForceAuthenticate.setOnClickListener(buttonview -> {
            // Update the value in the database
            databaseReference.child("ForceAuthorization").setValue(true);
        });





        // Initialize your Switch here and set its listener
        alarmSwitch = view.findViewById(R.id.lockSwitch);
        if (alarmSwitch != null) {
            // Read the current state from the database and update the switch state
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("alarm");
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String status = dataSnapshot.getValue(String.class);
                        boolean isOn = "ON".equals(status);
                        alarmSwitch.setChecked(isOn); // This will update the switch state without triggering the listener

                        // Remove this listener after retrieving the initial state to avoid unnecessary database reads
                        myRef.removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("DashboardFragment", "Failed to read value.", databaseError.toException());
                }
            });

            // Set the listener for the switch
            alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Handle switch change
                updateDatabase(isChecked ? "ON" : "OFF");
            });
        }
    }




    private void updateDatabase(String status) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("alarm");
        myRef.setValue(status);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}