package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.antitheft.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

    private DatabaseReference codePin;
    private DatabaseReference Authorization;

    private Boolean previousML2Status = false;

    private DatabaseReference CodePin;

    private DatabaseReference codePin_end;

    private TextView textViewProcessedResults2,textViewProcessedResults1;



    private StorageReference textFileRef2;

    private boolean isActivityCreated = false;
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

        textViewProcessedResults2 = findViewById(R.id.textViewProcessedResults2);
        textViewProcessedResults1 = findViewById(R.id.textViewProcessedResults);

        if (getIntent().hasExtra("navigateTo") && "dashboard".equals(getIntent().getStringExtra("navigateTo"))) {
            navController.navigate(R.id.action_HomeFragment_to_DashboardFragment); // Use the ID of your dashboard destination as defined in your nav_graph.xml
        }

        if (getIntent().hasExtra("navigateTo") && "Home".equals(getIntent().getStringExtra("navigateTo"))) {
            navController.navigate(R.id.action_dashboardFragment_to_HomeFragment); // Use the ID of your dashboard destination as defined in your nav_graph.xml
        }

        startDatabaseListenerService();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference();

        // Set ForceAuthorization to false whenever the app starts or MCU sets it.
        databaseReference.child("ForceAuthorization").setValue(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("MainActivity", "ForceAuthorization set to false successfully.");
            } else {
                Log.e("MainActivity", "Failed to set ForceAuthorization.", task.getException());
            }
        });

//        databaseReference.child("codePin").setValue(0).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Log.d("MainActivity", "ForceAuthorization set to false successfully.");
//            } else {
//                Log.e("MainActivity", "Failed to set ForceAuthorization.", task.getException());
//            }
//        });



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
        cognitiveGameResetRef = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("CognitiveGameReset");
        cognitiveGameResultRef = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("CognitiveGameResult");
        cognitiveGameEndRef = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("Cognitive_end");
        CodePin=FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePin");

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


        databaseRefML_End = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("ML_end");
        databaseRefML2 = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("ML_2");
        databaseRefML_Update_Lock = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("ML_Update_Lock");
        Authorization=FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("Authorization");

        codePinResult = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePin_result");
        codePin_end = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePin_end");
        codePin = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePin");

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
                    //codePin.setValue(0);
                    // Save the currentML2Status as the new previousML2Status in SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove("currentCodePin");
                    editor.remove("lastKnownCodePin");
                    editor.putBoolean("previousML2Status", currentML2Status);
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", "Failed to read ML_2.", error.toException());
            }
        });


        //reset function

        DatabaseReference resetAllRef = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("resetAll");

        resetAllRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean resetAll = dataSnapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(resetAll)) {
                    // Perform the reset actions
                    databaseRefML_End.setValue(false);
                    codePin_end.setValue(false);
                    databaseRefML_Update_Lock.setValue(true);
                    codePinResult.setValue(false);
                    Authorization.setValue(false);

                    // Reset the resetAll node to false after 5 seconds
                    new Handler().postDelayed(() -> resetAllRef.setValue(false), 5000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("MainActivity", "Failed to read resetAll.", databaseError.toException());
            }
        });



        //gallery

//        textFileRef2 = FirebaseStorage.getInstance("gs://eng4k-capstone-server-712").getReference("face_recognition_status.txt");
//
//        setupListeners();
//        isActivityCreated = true;


    }

//
//    private void setupListeners() {
//
//
//        // Listen for changes to ML_Update_Lock
//        databaseRefML_Update_Lock.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Boolean lockStatus = snapshot.getValue(Boolean.class);
//                if (lockStatus != null && !lockStatus) {
//                    if (isActivityCreated) {
//                        downloadAndProcessTextFile();
//                        downloadAndProcessFile();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        });
//    }
//
//    private void downloadAndProcessTextFile() {
//        if (!isActivityCreated) {
//            Log.d("MainActivity", "Activity not created, skipping file processing");
//            return; // Guard clause to prevent execution if activity is not created
//        }
//
//        File localFile = null;
//        String bucketUrl = "gs://eng4k-capstone-server-712";
//        FirebaseStorage storage = FirebaseStorage.getInstance(bucketUrl);
//        StorageReference textFileRef = storage.getReference("processed_results.txt");
//
//        try {
//            localFile = File.createTempFile("processedResults", "txt", getCacheDir());
//        } catch (IOException e) {
//            if (textViewProcessedResults1 != null) {
//                textViewProcessedResults1.setText("Unable to create local file.");
//            }
//            Log.e("MainActivity", "File creation failed", e);
//            return;
//        }
//
//        File finalLocalFile = localFile;
//        textFileRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
//            new Thread(() -> {
//                StringBuilder text = new StringBuilder();
//                try (BufferedReader br = new BufferedReader(new FileReader(finalLocalFile))) {
//                    String line;
//                    while ((line = br.readLine()) != null) {
//                        text.append(line).append('\n');
//                    }
//                } catch (IOException e) {
//                    Log.e("MainActivity", "Error reading file", e);
//                }
//
//                String finalText = text.toString().trim();
//                runOnUiThread(() -> {
//                    if (isActivityCreated) {
//                        if (textViewProcessedResults1 != null) {
//                            textViewProcessedResults1.setText(finalText);
//                        }
//                    } else {
//                        Log.d("MainActivity", "Activity not active, skipping UI update");
//                    }
//                });
//            }).start();
//        }).addOnFailureListener(exception -> {
//            if (textViewProcessedResults1 != null) {
//                textViewProcessedResults1.setText("Waiting for Images");
//            }
//            Log.e("MainActivity", "Download failed", exception);
//        });
//    }
//    private void downloadAndProcessFile() {
//        if (!isActivityCreated) {
//            Log.d("MainActivity", "Activity not created, skipping file download");
//            return;
//        }
//
//        File localFile;
//        try {
//            localFile = File.createTempFile("faceRecognitionStatus", "txt", getCacheDir());
//        } catch (IOException e) {
//            Log.e("MainActivity", "File creation failed", e);
//            runOnUiThread(() -> {
//                if (textViewProcessedResults2 != null) {
//                    textViewProcessedResults2.setText("Unable to create local file.");
//                }
//            });
//            return;
//        }
//
//        textFileRef2.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
//            processFileContent(localFile);
//        }).addOnFailureListener(exception -> {
//            Log.e("MainActivity", "File download failed", exception);
//            runOnUiThread(() -> {
//                if (textViewProcessedResults2 != null) {
//                    textViewProcessedResults2.setText("Failed to load file.");
//                }
//            });
//        });
//    }
//
//    private void processFileContent(File file) {
//        if (!file.exists()) {
//            Log.e("MainActivity", "File does not exist");
//            runOnUiThread(() -> {
//                if (textViewProcessedResults2 != null) {
//                    textViewProcessedResults2.setText("File not found.");
//                }
//            });
//            return;
//        }
//
//        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//            String status = br.readLine();
//            runOnUiThread(() -> {
//                if (isActivityCreated) {
//                    if (textViewProcessedResults2 != null) {
//                        textViewProcessedResults2.setText("True".equals(status) ? "Verified! Wait for Code!" : "Not Verified!");
//                    }
//                    updateDatabaseAuthorization("True".equals(status));
//                    databaseRefML_End.setValue(true);
//                    databaseRefML_Update_Lock.setValue(false);
//                } else {
//                    Log.d("MainActivity", "Activity not active, skipping UI update");
//                }
//            });
//        } catch (IOException e) {
//            Log.e("MainActivity", "Error reading file", e);
//            runOnUiThread(() -> {
//                if (textViewProcessedResults2 != null) {
//                    textViewProcessedResults2.setText("Error reading status.");
//                }
//            });
//        }
//    }
//    private void updateDatabaseAuthorization(boolean isAuthorized) {
//        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("Authorization");
//        databaseRef.setValue(isAuthorized);
//    }





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
            else if ("gallery".equals(navigateTo)) {
                // Navigate to the GalleryFragment
                navController.navigate(R.id.action_global_navigation_gallery);
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
    private void startDatabaseListenerService() {
        Intent serviceIntent = new Intent(this, ttsAlarmState.class);
        startService(serviceIntent);
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




