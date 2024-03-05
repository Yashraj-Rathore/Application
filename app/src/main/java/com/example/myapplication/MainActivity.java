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

        

//        // Check if we have read permission
//        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
//
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
//            );
//        }

//        Switch alarmSwitch = findViewById(R.id.lockSwitch); // Ensure this ID exists
//        if (alarmSwitch != null) {
//            alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    // Handle switch change
//                    FirebaseDatabase database = FirebaseDatabase.getInstance();
//                    DatabaseReference myRef = database.getReference("alarm");
//                    if(isChecked) {
//                        myRef.setValue("ON");
//                    } else {
//                        myRef.setValue("OFF");
//                    }
//                }
//            });
//        } else {
//            // Log an error or throw an exception that the switch is not found
//            Log.e("MainActivity", "Switch not found");
//        }
//
//
//    }
//
//    private void updateDatabase(String status) {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("alarm");
//        myRef.setValue(status);
//    }
//
//    private boolean checkAndRequestPermissions() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (!Environment.isExternalStorageManager()) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
//                startActivityForResult(intent, PERMISSIONS_REQUEST_MANAGE_STORAGE);
//                return false;
//            }
//        } else {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                updateDatabase("ON");
//            } else {
//                Log.d("Permissions", "Read external storage permission was denied");
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PERMISSIONS_REQUEST_MANAGE_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (Environment.isExternalStorageManager()) {
//                updateDatabase("ON");
//            } else {
//                Log.d("Permissions", "Manage external storage permission was denied");
//            }
//        }
//    }
//}

//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Permission is not granted
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//        } else {
//            // Permission has already been granted
//            //uploadImage();
//        }
//
//        //checkAndRequestPermissions();
//
//    }


//    private void checkAndRequestPermissions() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            // On Android 11 and above, use the MANAGE_EXTERNAL_STORAGE permission
//            if (!Environment.isExternalStorageManager()) {
//                // Request for the permission
//                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//                intent.addCategory("android.intent.category.DEFAULT");
//                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
//                startActivityForResult(intent, PERMISSIONS_REQUEST_MANAGE_STORAGE);
//            } else {
//                // You have permission, proceed further
//                uploadImage();
//            }
//        } else {
//            // For Android 10 and below, use the READ_EXTERNAL_STORAGE permission
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//            } else {
//                // You have permission, proceed further
//                uploadImage();
//            }
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission was granted
//                    uploadImage();
//                } else {
//                    // Permission was denied
//                    Log.d("Permissions", "Read external storage permission was denied");
//                }
//                break;
        }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // Defer navigation until the NavController is ready
        // This is especially important when the app is just starting up
        findViewById(R.id.nav_host_fragment_activity_main).post(() -> {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            String navigateTo = intent.getStringExtra("navigateTo");
            if ("gallery".equals(navigateTo)) {
                // Use the global action to navigate to the GalleryFragment
                navController.navigate(R.id.navigation_gallery);
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





//    private void uploadImage() {
//        // Get the default Firebase Storage instance
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//
//        // Create a storage reference from our app
//        StorageReference storageRef = storage.getReference();
//
//        // Get the file reference
//        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "lock.jpg");
//
//        // Check if the file exists
//        if (!file.exists()) {
//            Log.e("Upload", "File does not exist at path: " + file.getPath());
//            return;
//        }
//
//        // Get the Uri for the file
//        Uri fileUri = Uri.fromFile(file);
//
//        // Create a reference to the file to upload
//        StorageReference riversRef = storageRef.child("images2/" + fileUri.getLastPathSegment());
//
//        // Initiate the upload
//        UploadTask uploadTask = riversRef.putFile(fileUri);
//
//        // Register observers to listen for when the upload is done or if it fails
//        uploadTask.addOnFailureListener(exception -> {
//            // Handle unsuccessful uploads
//            Log.e("Upload", "Upload failed: " + exception.getMessage(), exception);
//        }).addOnSuccessListener(taskSnapshot -> {
//            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
//            // Here you can also retrieve the download URL
//            Log.d("Upload", "Upload succeeded");
//            StorageReference ref = taskSnapshot.getMetadata().getReference();
//            if (ref != null) {
//                ref.getDownloadUrl().addOnSuccessListener(uri -> {
//                    String downloadUrl = uri.toString();
//                    Log.d("Download URL", "File uploaded to: " + downloadUrl);
//                });
//            }
//        });
//    }
//
//
//
//}


//    //Firebase Realtime Database code
//    FirebaseDatabase database = FirebaseDatabase.getInstance();
//    DatabaseReference myRef = database.getReference("alarm");
//    DatabaseReference myRef2 = database.getReference("motion");
//    DatabaseReference myRef3 = database.getReference("user_recognition");
//
//    // Write a message to the database
//        myRef.setValue("ON");
//        myRef2.setValue("OFF");
//        myRef3.setValue("OFF");
//
//    // Read from the database
//        myRef.addValueEventListener(new ValueEventListener() {
//        @Override
//        public void onDataChange (DataSnapshot dataSnapshot){
//            // This method is called once with the initial value and again
//            // whenever data at this location is updated.
//            String value = dataSnapshot.getValue(String.class);
//            Log.d("Database", "Value is: " + value);
//        }
//
//        @Override
//        public void onCancelled (DatabaseError error){
//            // Failed to read value
//            Log.w("Database", "Failed to read value.", error.toException());}
//    });
//
//        myRef2.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange (DataSnapshot dataSnapshot){
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
//                Log.d("Database", "Value is: " + value);}
//
//            @Override
//            public void onCancelled (DatabaseError error){
//                // Failed to read value
//                Log.w("Database", "Failed to read value.", error.toException());}
//        });
//        myRef3.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange (DataSnapshot dataSnapshot){
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
//                Log.d("Database", "Value is: " + value);}
//
//            @Override
//            public void onCancelled (DatabaseError error){
//                // Failed to read value
//                Log.w("Database", "Failed to read value.", error.toException());}
//        });
//
//
//    }
//}
