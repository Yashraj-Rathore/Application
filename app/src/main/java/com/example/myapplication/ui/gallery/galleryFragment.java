package com.example.myapplication.ui.gallery;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        // Retrieve and display images
        retrieveAndDisplayImages();

        return root;


    }

    private void retrieveAndDisplayImages() {
        String bucketUrl = "gs://app-proj4000.appspot.com";
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
        String bucketUrl = "gs://app-proj4000.appspot.com";
        FirebaseStorage storage = FirebaseStorage.getInstance(bucketUrl);

// Initialize your TextView
        TextView textViewProcessedResults = view.findViewById(R.id.textViewProcessedResults);

// Reference to your text file in Firebase Storage
        StorageReference textFileRef = storage.getReference("proccessed_results.txt");

// Create a local file to store the download
        File localFile = null;
        try {
            localFile = File.createTempFile("processedResults", "txt", getContext().getCacheDir());
        } catch (IOException e) {
            // Handle IOException by showing a message to the user
            textViewProcessedResults.setText("Unable to create local file.");
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
                        String finalText = text.toString();
                        getActivity().runOnUiThread(() -> textViewProcessedResults.setText(finalText));
                    }).start();
                })
                .addOnFailureListener(exception -> {
                    // Handle any errors in file download
                    textViewProcessedResults.setText("Failed to download results.");
                    exception.printStackTrace();
                });


        // Reference to your 'images/' directory in Firebase Storage

//        StorageReference storageRef = storage.getReference(); // Now points to your non-default bucket
//
//        storageRef.listAll()
//                .addOnSuccessListener(listResult -> {
//                    // Initialize a new list to hold the download URLs
//                    ArrayList<String> newImagePaths = new ArrayList<>();
//
//                    // Track the number of async operations we need to wait for
//                    AtomicInteger itemCount = new AtomicInteger();
//
//                    for (StorageReference itemRef : listResult.getItems()) {
//                        // Only consider JPEG images
//                        if (itemRef.getName().toLowerCase().endsWith(".jpg") || itemRef.getName().toLowerCase().endsWith(".jpeg")) {
//                            itemCount.incrementAndGet(); // Increment count for JPEG files
//                            // For each JPEG item, get the download URL
//                            itemRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                                // "uri" is the download URL for the image
//                                newImagePaths.add(uri.toString()); // Add URI to your list as a String
//                                if (newImagePaths.size() == itemCount.get()) {
//                                    // Once all URIs are fetched, update your adapter
//                                    adapter.setImageUrls(newImagePaths);
//                                    adapter.notifyDataSetChanged();
//                                }
//                            }).addOnFailureListener(e -> {
//                                // Handle any errors in getting download URLs
//                                if (itemCount.decrementAndGet() == 0) {
//                                    // Optionally, update UI or notify users that some images might not load
//                                    adapter.setImageUrls(newImagePaths);
//                                    adapter.notifyDataSetChanged();
//                                }
//                            });
//                        }
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    // Handle any errors here, such as failing to list the items
//                });
//    }
//
//

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



}
