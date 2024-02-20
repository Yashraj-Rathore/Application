package com.example.myapplication.ui.gallery;


import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class galleryFragment extends Fragment {


    private GridView gridView;
    private ImageAdapter adapter;

    private ActivityGalleryBinding binding;
    private ArrayList<String> imagePaths;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(galleryViewModel.class);

        binding = ActivityGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final TextView textView = binding.textGallery;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


        gridView = binding.gridView; // Make sure to use the correct ID
        imagePaths = new ArrayList<>();
        adapter = new ImageAdapter(getActivity(), imagePaths);
        gridView.setAdapter(adapter);

        return root;



    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // Reference to your 'images/' directory in Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/");
        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    // Initialize a new list to hold the download URLs
                    ArrayList<String> newImagePaths = new ArrayList<>();

                    // Track the number of async operations we need to wait for
                    AtomicInteger itemCount = new AtomicInteger(listResult.getItems().size());

                    for (StorageReference itemRef : listResult.getItems()) {
                        // For each item, get the download URL
                        itemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // "uri" is the download URL for the image
                            imagePaths.add(uri.toString()); // Add URI to your list as a String
                            if (imagePaths.size() == listResult.getItems().size()) {
                                // Once all URIs are fetched, update your adapter
                                adapter.setImageUrls(imagePaths);
                                adapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(e -> {
                            // Handle any errors in getting download URLs
                            // Also decrement the count
                            if (itemCount.decrementAndGet() == 0) {
                                // Optionally, update UI or notify users that some images might not load
                                adapter.setImageUrls(newImagePaths);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors here, such as failing to list the items
                });
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



}
