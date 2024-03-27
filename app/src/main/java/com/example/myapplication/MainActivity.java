package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;



import android.Manifest;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.antitheft.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.antitheft.databinding.ActivityMainBinding;
import com.google.common.net.InternetDomainName;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PERMISSIONS_REQUEST_MANAGE_STORAGE = 2;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    public static final String CHANNEL_ID = "new_image_notifications";

    private DatabaseReference cognitiveGameResetRef;

    private DatabaseReference cognitiveGameResultRef;
    private DatabaseReference cognitiveGameEndRef;
    private DatabaseReference databaseRefML_End, databaseRefML2, databaseRefML_Update_Lock;

    private DatabaseReference codePinResult;

    private DatabaseReference Authorization;

    private Boolean previousML2Status = false;

    private DatabaseReference codePin_end;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications,R.id.navigation_gallery)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);



        NavigationUI.setupWithNavController(binding.navView, navController);

        if (getIntent().hasExtra("navigateTo") && "dashboard".equals(getIntent().getStringExtra("navigateTo"))) {
            navController.navigate(R.id.navigation_dashboard); // Use the ID of your dashboard destination as defined in your nav_graph.xml
        }

        if (getIntent().hasExtra("navigateTo") && "Home".equals(getIntent().getStringExtra("navigateTo"))) {
            navController.navigate(R.id.action_dashboardFragment_to_HomeFragment); // Use the ID of your dashboard destination as defined in your nav_graph.xml
        }


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Set ForceAuthorization to false whenever the app starts or MCU sets it.
        databaseReference.child("ForceAuthorization").setValue(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("MainActivity", "ForceAuthorization set to false successfully.");
            } else {
                Log.e("MainActivity", "Failed to set ForceAuthorization.", task.getException());
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("allDevices")
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        // Handle failure
                    }
                    // Handle success
                });


        // Check and ask for the notification permission


        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission is granted. FCM SDK (and your app) can post notifications.
                    } else {
                        // Permission is denied. Inform the user that your app will not show notifications.
                    }
                });

        askNotificationPermission();

        createNotificationChannel();

        handleIntent(getIntent());

        cognitiveGameResetRef = FirebaseDatabase.getInstance().getReference("CognitiveGameReset");
        cognitiveGameResultRef = FirebaseDatabase.getInstance().getReference("CognitiveGameResult");
        cognitiveGameEndRef = FirebaseDatabase.getInstance().getReference("Cognitive_end");

        // Listen for the cognitiveGameReset variable change


        cognitiveGameResetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean reset = dataSnapshot.getValue(Boolean.class);
                // Only proceed if reset is requested
                if (Boolean.TRUE.equals(reset)) {
                    cognitiveGameEndRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Boolean gameEnd = dataSnapshot.getValue(Boolean.class);
                            // Only reset if cognitiveGameEnd is true
                            if (Boolean.TRUE.equals(gameEnd)) {
                                cognitiveGameResultRef.setValue(false);
                                cognitiveGameEndRef.setValue(false);


                                // Set cognitiveGameReset to false after 5 seconds
                                new Handler().postDelayed(() -> cognitiveGameResetRef.setValue(false), 5000);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("GameMainActivity", "Failed to read cognitiveGameEnd.", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("GameMainActivity", "Failed to read cognitiveGameReset.", databaseError.toException());
            }
        });

        cognitiveGameEndRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean reset = dataSnapshot.getValue(Boolean.class);
                // Only proceed if reset is requested
                if (Boolean.TRUE.equals(reset)) {
                    cognitiveGameResetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Boolean gameReset = dataSnapshot.getValue(Boolean.class);
                            // Only reset if cognitiveGameEnd is true
                            if (Boolean.TRUE.equals(gameReset)) {
                                cognitiveGameResultRef.setValue(false);
                                cognitiveGameEndRef.setValue(false);


                                // Set cognitiveGameReset to false after 5 seconds
                                new Handler().postDelayed(() -> cognitiveGameResetRef.setValue(false), 5000);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("GameMainActivity", "Failed to read cognitiveGameEnd.", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("GameMainActivity", "Failed to read cognitiveGameReset.", databaseError.toException());
            }
        });


        databaseRefML_End = FirebaseDatabase.getInstance().getReference("ML_end");
        databaseRefML2 = FirebaseDatabase.getInstance().getReference("ML_2");
        databaseRefML_Update_Lock = FirebaseDatabase.getInstance().getReference("ML_Update_Lock");
        Authorization=FirebaseDatabase.getInstance().getReference("Authorization");

        codePinResult = FirebaseDatabase.getInstance().getReference("codePin_result");
        codePin_end = FirebaseDatabase.getInstance().getReference("codePin_end");
        databaseRefML2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                Boolean currentML2Status = snapshot.getValue(Boolean.class);

                // Get SharedPreferences
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                // Read previousML2Status from SharedPreferences
                boolean previousML2Status = prefs.getBoolean("previousML2Status", false); // Default to false if not found

                if (currentML2Status != null && !currentML2Status.equals(previousML2Status)) {
                    // The value of ML_2 has changed
                    // Perform your actions based on the change
                    databaseRefML_End.setValue(false); // Force ML_End to false if ML_2 changes
                    codePin_end.setValue(false);
                    databaseRefML_Update_Lock.setValue(true); // Engage the update lock
                    codePinResult.setValue(false);
                    Authorization.setValue(false);

                    // Save the currentML2Status as the new previousML2Status in SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("previousML2Status", currentML2Status);
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", "Failed to read ML_2.", error.toException());
            }
        });




    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // Defer navigation until the NavController is ready
        findViewById(R.id.nav_host_fragment_activity_main).post(() -> {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            String navigateTo = intent.getStringExtra("navigateTo");
            String action = intent.getAction();

            if ("home".equals(navigateTo)) {
                // Navigate to the HomeFragment
                navController.navigate(R.id.action_global_navigation_home);
            } else if ("notifications".equals(navigateTo)) {
                // Navigate to the GalleryFragment
                navController.navigate(R.id.action_NotificationsFragment);
            }
              else if ("dashboard".equals(navigateTo)) {
                // Navigate to the GalleryFragment
                navController.navigate(R.id.action_HomeFragment_to_DashboardFragment);
            }

        });
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name); // User-visible name of the channel
            String description = getString(R.string.channel_description); // User-visible description of the channel
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Permission is already granted. FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show an educational UI to the user. This UI should explain why your app needs the
                // permission for posting notifications. After showing the UI, you can request the
                // permission again depending on the user's response.

                // Example dialog or UI interaction, then request permission based on user feedback:
                // requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Directly ask for the permission without rationale.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }


}




