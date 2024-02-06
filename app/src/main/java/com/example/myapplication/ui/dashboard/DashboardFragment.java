package com.example.myapplication.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.antitheft.R;
import com.example.antitheft.databinding.FragmentDashboardBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PERMISSIONS_REQUEST_MANAGE_STORAGE = 2;
    private Switch alarmSwitch;

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

        // Initialize your Switch here and set its listener
        alarmSwitch = view.findViewById(R.id.lockSwitch);
        if (alarmSwitch != null) {
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