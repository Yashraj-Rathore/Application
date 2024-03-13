package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PERMISSIONS_REQUEST_MANAGE_STORAGE = 2;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    public static final String CHANNEL_ID = "new_image_notifications";
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
            } else if ("gallery".equals(navigateTo)) {
                // Navigate to the GalleryFragment
                navController.navigate(R.id.action_dashboardFragment_to_galleryFragment);
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




