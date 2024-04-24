package com.example.myapplication.ui.gallery;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.antitheft.R;
import com.example.antitheft.databinding.ActivityGalleryBinding;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.GridView;
import android.widget.Toast;


import com.example.myapplication.DeviceState;
import com.example.myapplication.DeviceStateChecker;
import com.example.myapplication.ui.dashboard.DashboardViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class galleryFragment extends Fragment {


    private GridView gridView;
    private ImageAdapter adapter;

    private ActivityGalleryBinding binding;
    private ArrayList<String> imagePaths;
    private TextView textViewProcessedResults2,textViewProcessedResults1;
    private DatabaseReference databaseRefML_End, databaseRefML2, databaseRefML_Update_Lock;

    private DatabaseReference codePinResult;
    private DatabaseReference galleryClearRef;
    private DatabaseReference codePin_end;
    private StorageReference textFileRef2;
    private Boolean previousML2Status = false;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        galleryViewModel galleryViewModel = new ViewModelProvider(this).get(galleryViewModel.class);

        binding = ActivityGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textGallery;

        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Grid and adapter setup
        gridView = binding.gridView; // Make sure to use the correct ID
        ArrayList<ImageItem> imageItems = new ArrayList<>(); // Use ImageItem instead of String
        adapter = new ImageAdapter(getActivity(), imageItems); // Initialize the adapter with ImageItem list
        gridView.setAdapter(adapter);
        galleryClearRef = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("galleryClear");

        databaseRefML_End = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("ML_end");
        databaseRefML2 = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("ML_2");
        databaseRefML_Update_Lock = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("ML_Update_Lock");
        textFileRef2 = FirebaseStorage.getInstance("gs://eng4k-capstone-server-712").getReference("face_recognition_status.txt");

        codePinResult = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePin_result");
        codePin_end = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePin_end");


        // Retrieve and display images
        retrieveAndDisplayImages();

        setupListeners();


        return root;


    }



    private void retrieveAndDisplayImages() {
        Context context = getContext();
        if (context == null) {
            // Context is not available, can't proceed
            return;
        }


        String bucketUrl = "gs://eng4k-capstone-server-712";
        FirebaseStorage storage = FirebaseStorage.getInstance(bucketUrl);
        StorageReference storageRef = storage.getReference();

        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<Task<Pair<String, Long>>> tasks = new ArrayList<>();

                    for (StorageReference itemRef : listResult.getItems()) {
                        // Only consider JPEG images
                        String itemName = itemRef.getName().toLowerCase();
                        if (itemName.endsWith(".jpg") || itemName.endsWith(".jpeg")) {
                            Task<Uri> downloadUrlTask = itemRef.getDownloadUrl();
                            Task<StorageMetadata> metadataTask = itemRef.getMetadata();

                            Task<Pair<String, Long>> combinedTask = Tasks.whenAllSuccess(downloadUrlTask, metadataTask)
                                    .continueWithTask(task -> {
                                        Uri downloadUrl = (Uri) task.getResult().get(0);
                                        StorageMetadata metadata = (StorageMetadata) task.getResult().get(1);
                                        Long creationTimeMillis = metadata.getCreationTimeMillis();
                                        return Tasks.forResult(new Pair<>(downloadUrl.toString(), creationTimeMillis));
                                    });
                            tasks.add(combinedTask);
                        }
                    }

                    // Once all tasks are complete, sort and update UI
                    Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(results -> {
                                ArrayList<ImageItem> imageItems = new ArrayList<>();
                                for (Pair<String, Long> pair : (List<Pair<String, Long>>) (List<?>) results) {
                                    String imageUrl = pair.first;
                                    String formattedTime = formatTime(pair.second);
                                    imageItems.add(new ImageItem(imageUrl, formattedTime));
                                }

                                // Sort by creation time in descending order
                                imageItems.sort((i1, i2) -> i2.getUploadTime().compareTo(i1.getUploadTime()));

                                // Run on UI thread
                                getActivity().runOnUiThread(() -> {
                                    adapter.setImageItems(imageItems);
                                    adapter.notifyDataSetChanged();
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("GalleryFragment", "Error fetching images", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("GalleryFragment", "Error listing images", e);
                });
    }


    private String formatTime(Long time) {
        // Format the time into a human-readable form
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        return dateFormat.format(new Date(time));
    }






    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btnClearGallery = view.findViewById(R.id.btnClearGallery);
        btnClearGallery.setOnClickListener(v -> {
            // Set galleryClear to true when the button is clicked
            galleryClearRef.setValue(true);
            Toast.makeText(getActivity(), "Clearing Images", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> galleryClearRef.setValue(false), 2000);
        });

       textViewProcessedResults2 = view.findViewById(R.id.textViewProcessedResults2);
        textViewProcessedResults1 = getView().findViewById(R.id.textViewProcessedResults);


    }



    private void setupListeners() {
        // Listen for changes in ML_2
//        databaseRefML2.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Boolean currentML2Status = snapshot.getValue(Boolean.class);
//                if (currentML2Status != null && !currentML2Status.equals(previousML2Status)) {
//                    // The value of ML_2 has changed
//                    // Check if it has changed to its opposite boolean value
//                    // Since we already checked for null and difference, it's certain it has changed
//
//                    // Perform your actions based on the change
//                    databaseRefML_End.setValue(false); // Force ML_End to false if ML_2 changes
//                    codePin_end.setValue(false);
//                    databaseRefML_Update_Lock.setValue(true); // Engage the update lock
//                    codePinResult.setValue(false);
//
//
//                    // Update the previousML2Status for future comparisons
//                    previousML2Status = currentML2Status;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        });

        // Listen for changes to ML_Update_Lock
        databaseRefML_Update_Lock.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean lockStatus = snapshot.getValue(Boolean.class);
                if (lockStatus != null && !lockStatus) {
                    downloadAndProcessTextFile();
                    downloadAndProcessFile(); // Only proceed if the lock is disengaged
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void downloadAndProcessTextFile() {
        Context context = getContext();
        if (context == null) {
            // Context is not available, can't proceed
            return;
        }
        File localFile = null;
        String bucketUrl = "gs://eng4k-capstone-server-712";
        FirebaseStorage storage = FirebaseStorage.getInstance(bucketUrl);


        // Reference to your text file in Firebase Storage
        StorageReference textFileRef = storage.getReference("processed_results.txt");


        try {
            localFile = File.createTempFile("processedResults", "txt", getContext().getCacheDir());
        } catch (IOException e) {
            // Handle IOException by showing a message to the user
            textViewProcessedResults1.setText("Unable to create local file.");
            e.printStackTrace();
            return;
        }

        File finalLocalFile = localFile;
        textFileRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    // Read text from file in a background thread
                    new Thread(() -> {
                        StringBuilder text = new StringBuilder();
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(finalLocalFile));
                            String line;

                            while ((line = br.readLine()) != null) {
                                text.append(line);
                                text.append('\n');
                            }
                            br.close();
                        } catch (IOException e) {
                            // Handle exceptions on the background thread
                            e.printStackTrace();
                        }

                        // Update the TextView on the main thread
                        String finalText = text.toString().trim(); // Trim to remove the last newline character
                        getActivity().runOnUiThread(() -> textViewProcessedResults1.setText(finalText));
                    }).start();
                })
                .addOnFailureListener(exception -> {
                    // Handle any errors in file download
                    textViewProcessedResults1.setText("Waiting for Images");
                    exception.printStackTrace();
                });


    }

    private void downloadAndProcessFile() {
        Context context = getContext();
        if (context == null) {
            // Context is not available, can't proceed
            return;
        }
        try {
            File localFile2 ;
            localFile2= File.createTempFile("faceRecognitionStatus", "txt", getContext().getCacheDir());
            textFileRef2.getFile(localFile2).addOnSuccessListener(taskSnapshot -> {
                processFileContent(localFile2);
            }).addOnFailureListener(exception -> {
                textViewProcessedResults2.setText("...");
            });
        } catch (IOException e) {
            textViewProcessedResults2.setText("Unable to create local file.");
        }
    }

    private void processFileContent(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String status = br.readLine();
            getActivity().runOnUiThread(() -> {
                if ("True".equals(status)) {
                    textViewProcessedResults2.setText("Verified! Wait for Code!");
                    updateDatabaseAuthorization(true);
                    // Set ML_End true directly here based on file content
                    databaseRefML_End.setValue(true);
                } else if ("False".equals(status)) {
                    textViewProcessedResults2.setText("Not Verified!");
                    updateDatabaseAuthorization(false);
                    databaseRefML_End.setValue(true);
                    // Depending on your logic, set ML_End to false here if necessary
                    // databaseRefML_End.setValue(false);
                }
                // Reset ML_Update_Lock to allow for new updates
                databaseRefML_Update_Lock.setValue(false);
            });
        } catch (IOException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() -> textViewProcessedResults2.setText("Error reading status."));
        }
    }

    private void updateDatabaseAuthorization(boolean isAuthorized) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("Authorization");
        databaseRef.setValue(isAuthorized);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



}
