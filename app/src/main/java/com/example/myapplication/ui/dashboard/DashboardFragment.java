package com.example.myapplication.ui.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.antitheft.R;
import com.example.antitheft.databinding.FragmentDashboardBinding;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.ui.gallery.ImageAdapter;
import com.example.myapplication.ui.gps_display.MapsActivity;
import com.example.myapplication.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private Switch alarmSwitch;
    private Switch servoSwitch;

    private Button btnAuthorities;

    private ArrayList<Uri> imageUrls = new  ArrayList<>();

    private FirebaseAuth mAuth;

    private DatabaseReference databaseReference2;
    private TextView LockAlert;
    private ImageView LockAlertImage;
    private TextView Lock;
    private ImageView LockImage;
    private DatabaseReference iffailRef;
    private DatabaseReference iffailResetRef;

    private DatabaseReference ML_end;
    private DatabaseReference Authorization;
    private Button btnResetAlarm;

    boolean currentMLEndState = false;
    boolean currentAuthorizationState = false;

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

       LockAlert= view.findViewById(R.id.lockStatus2Text);
        LockAlertImage=view.findViewById(R.id.lockImage2);
        Lock= view.findViewById(R.id.lockStatusText);
        LockImage=view.findViewById(R.id.lockImage);

        LockAlert.setVisibility(View.GONE);
        LockAlertImage.setVisibility(View.GONE);
        Lock.setVisibility(View.VISIBLE);
        LockImage.setVisibility(View.VISIBLE);


        Button locationButton = view.findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MapsActivity
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
            }
        });


        Button displayImagesButton = view.findViewById(R.id.btnDisplayImages);

        displayImagesButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_dashboardFragment_to_galleryFragment);
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Button btnForceAuthenticate = view.findViewById(R.id.btnForceAuthenticate);
        alarmSwitch = view.findViewById(R.id.lockSwitch);
        servoSwitch = view.findViewById(R.id.ServoSwitch);

// Listen to ForceAuthorization changes and adjust UI & database accordingly
        databaseReference.child("ForceAuthorization").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean forceAuthorization = snapshot.getValue(Boolean.class);
                if (forceAuthorization != null) {
                    btnForceAuthenticate.setEnabled(!forceAuthorization);
                    // Set LockHandler to true if forceAuthorization is true, otherwise false

                    //databaseReference.child("LockHandler").setValue(forceAuthorization);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


        btnForceAuthenticate.setOnClickListener(v -> {
            // Prompt the user to enter their password
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Password Verification");

            // Set up the input field
            EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String password = input.getText().toString();
                verifyPasswordAndUpdateAuthorization(password);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });



        // not sure about this
        databaseReference.child("CognitiveGameResult").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean cognitiveGameResult = snapshot.getValue(Boolean.class);

                // If CognitiveGameResult is true, set LockHandler to true
                if (Boolean.TRUE.equals(cognitiveGameResult)) {
                    databaseReference.child("LockHandler").setValue(true);
                } else {
                    // If CognitiveGameResult is not true, check ForceAuthorization before setting LockHandler
                    databaseReference.child("ForceAuthorization").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Boolean forceAuthorization = task.getResult().getValue(Boolean.class);
                            // If either CognitiveGameResult or ForceAuthorization is true, set LockHandler to true
                            databaseReference.child("LockHandler").setValue(Boolean.TRUE.equals(forceAuthorization));
                        } else {
                            Log.e("DatabaseError", "Error getting ForceAuthorization value", task.getException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("CognitiveGameResultListener", "Failed to read CognitiveGameResult.", error.toException());
            }
        });




        databaseReference.child("LockHandler").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean lockHandler = snapshot.getValue(Boolean.class);
                // Enable/disable alarm and servo switches based on LockHandler value
                boolean isEnabled = lockHandler != null && lockHandler;

                // alarmSwitch.setEnabled(isEnabled);
                servoSwitch.setEnabled(isEnabled);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("LockHandlerListener", "Failed to read LockHandler state.", error.toException());
            }
        });

        // Assuming alarmSwitch and servoSwitch are already initialized as shown in your code snippet


        // Set a listener for the servo switch to update the database when toggled
        servoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only update the database if LockHandler allows it
            databaseReference.child("LockHandler").get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Boolean lockHandler = task.getResult().getValue(Boolean.class);
                    if(Boolean.TRUE.equals(lockHandler)) {
                        databaseReference.child("servoControl").setValue(isChecked);
                    }
                }
            });
        });



 //Add a ValueEventListener to listen to changes in Authorization
        databaseReference.child("Authorization").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot authorizationSnapshot) {
                Boolean authorization = authorizationSnapshot.getValue(Boolean.class);

                // Add a ValueEventListener to listen to changes in ML_end
                databaseReference.child("ML_end").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot mlEndSnapshot) {
                        Boolean mlEnd = mlEndSnapshot.getValue(Boolean.class);

                        // Check if both Authorization and ML_end are false
                        boolean isAuthorized = authorization != null && !authorization;
                        boolean isMLEnd = mlEnd != null && mlEnd;


                        if (isAuthorized && isMLEnd) {
                            databaseReference.child("iffail").setValue(true);
                            // Assuming you have a Button with the id btnAuthorities in your layout
                            Button btnAuthorities = view.findViewById(R.id.btnAuthorities);
                            btnAuthorities.setEnabled(true); // Enable button
                            alarmSwitch.setEnabled(true); // Enable switch
                        } else {
                            databaseReference.child("iffail").setValue(false);
                            Button btnAuthorities = view.findViewById(R.id.btnAuthorities);
                            btnAuthorities.setEnabled(false); // Disable button if any condition is not met
                            alarmSwitch.setEnabled(false); // Disable switch
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("ML_endListener", "Failed to read ML_end state.", databaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("AuthorizationListener", "Failed to read Authorization state.", databaseError.toException());
            }
        });



        alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only update the database if Authorization allows it
            databaseReference.child("iffail").get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Boolean Authorization = task.getResult().getValue(Boolean.class);
                    if(Boolean.TRUE.equals(Authorization)) {
                        databaseReference.child("alarm").setValue(isChecked);
                    }
                }
            });
        });

// Reflect the actual states of alarm and servo switches from the database
        updateSwitchStateFromDatabase("alarm", alarmSwitch);
        updateSwitchStateFromDatabase("servoControl", servoSwitch);



        btnAuthorities = view.findViewById(R.id.btnAuthorities);

        // Set up a click listener for the button
        btnAuthorities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Define the message to send
                String emergencyMessage = "The authorities have been notified. Please step out of the vehicle!";

                // Send the message to the Realtime Database
                sendEmergencyMessage(emergencyMessage);
            }
        });

        databaseReference2 = FirebaseDatabase.getInstance().getReference();


        databaseReference.child("iffail").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean iffailResult = snapshot.getValue(Boolean.class);

                // If iffailResult is true, set lockimage and text to true
                if (Boolean.TRUE.equals(iffailResult)) {

                    LockAlert.setVisibility(View.VISIBLE);
                    LockAlertImage.setVisibility(View.VISIBLE);

                    Lock.setVisibility(View.GONE);
                    LockImage.setVisibility(View.GONE);


                } else {

                    LockAlert.setVisibility(View.GONE);
                    LockAlertImage.setVisibility(View.GONE);

                    Lock.setVisibility(View.VISIBLE);
                    LockImage.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("iffailListener", "Failed to read iffailResult.", error.toException());
            }
        });



        btnResetAlarm = view.findViewById(R.id.btnResetAlarm);

        iffailRef = FirebaseDatabase.getInstance().getReference("iffail");
        iffailResetRef = FirebaseDatabase.getInstance().getReference("iffailReset");
        ML_end = FirebaseDatabase.getInstance().getReference("ML_end");
       Authorization = FirebaseDatabase.getInstance().getReference("Authorization");



        listenForDatabaseChanges();

        iffailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean iffail = dataSnapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(iffail)) {
                    btnResetAlarm.setVisibility(View.VISIBLE);
                } else {
                    btnResetAlarm.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("iffailListener", "Error listening for iffail changes", databaseError.toException());
            }
        });

        checkConditionsAndUpdateUI();

    }

    private void listenForDatabaseChanges() {
        // Listen for changes to ML_end
        ML_end.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Assuming ML_end is a boolean value
                Boolean value = dataSnapshot.getValue(Boolean.class);
                if (value != null) {
                    currentMLEndState = value;
                    // Call any method that needs to know ML_end's value

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DatabaseError", "loadPost:onCancelled", databaseError.toException());
            }
        });

        // Listen for changes to Authorization
        Authorization.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Assuming Authorization is a boolean value
                Boolean value = dataSnapshot.getValue(Boolean.class);
                if (value != null) {
                    currentAuthorizationState = value;
                    // Call any method that needs to know Authorization's value

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DatabaseError", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }


    private void checkConditionsAndUpdateUI() {
        btnResetAlarm.setOnClickListener(v -> {
            // Check conditions before resetting
            // Fetch current state of Authorization

            if ( currentMLEndState && !currentAuthorizationState) {
                // Acknowledge the alarm
                iffailResetRef.setValue(true);

                // Reset iffail
                iffailRef.setValue(false);
                Authorization.setValue(true);

                // Optionally reset Authorization and ML_end if needed
                // mlEndRef.setValue(false);

                // Provide feedback to the user
                Toast.makeText(getActivity(), "Alarm reset successfully.", Toast.LENGTH_SHORT).show();

                // Automatically reset iffailReset after a delay (e.g., 5 seconds)
                new Handler().postDelayed(() -> iffailResetRef.setValue(false), 5000);
            } else {
                // Conditions not met, provide feedback
                Toast.makeText(getActivity(), "Cannot reset alarm. Conditions not met.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyPasswordAndUpdateAuthorization(String password) {
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && password != null && !password.isEmpty()) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Password is correct, toggle ForceAuthorization state
                            databaseReference.child("ForceAuthorization").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Boolean forceAuthorization = snapshot.getValue(Boolean.class);
                                    // Toggle ForceAuthorization and set LockHandler accordingly
                                    boolean newForceAuthorization = forceAuthorization == null || !forceAuthorization;
                                    databaseReference.child("ForceAuthorization").setValue(newForceAuthorization);
                                    //databaseReference.child("LockHandler").setValue(newForceAuthorization);
                                    Toast.makeText(getActivity(), "Navigating to Home screen for game.", Toast.LENGTH_SHORT).show();
                                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);
                                    navController.navigate(R.id.action_dashboardFragment_to_HomeFragment);


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        } else {
                            // Password is incorrect
                            Toast.makeText(getActivity(), "Incorrect password. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



    private void sendEmergencyMessage(String message) {
        // You might want to use a specific path where you need to store the message
        databaseReference2.child("emergencyMessage").setValue(message)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Message sent.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to send message to the database.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateSwitchStateFromDatabase(String key, Switch switchControl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean state = dataSnapshot.getValue(Boolean.class);
                switchControl.setChecked(state != null && state);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(key + "SwitchListener", "Failed to read state.", databaseError.toException());
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}