package com.example.myapplication.ui.home;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.antitheft.R;
import com.example.antitheft.databinding.FragmentHomeBinding;
import com.example.myapplication.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private EditText etCodePin;
    private TextView tvCodePinMessage;
    private Button validateButton;
    private Button startGameButton;
    private DatabaseReference timeCurrentRef;
    private DatabaseReference codePinRef;
    private DatabaseReference StartGame;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCodePin = view.findViewById(R.id.etCodePin);
        tvCodePinMessage = view.findViewById(R.id.tvCodePinMessage);
        validateButton = view.findViewById(R.id.validateButton);
        startGameButton = view.findViewById(R.id.startGameButton);

        // Initially disable the start game button until the code is validated
        startGameButton.setEnabled(false);


        validateButton.setOnClickListener(v -> validateCode());

        timeCurrentRef = FirebaseDatabase.getInstance().getReference("timeCurrent");
        codePinRef = FirebaseDatabase.getInstance().getReference("codePin");

        checkTimeCurrentAndSetup();

        startGameButton.setOnClickListener(v -> {
            // Start GameMainActivity
            Intent intent = new Intent(getActivity(), ColorSelection.class);
            startActivity(intent);
        });

    }


    private void checkTimeCurrentAndSetup() {
        timeCurrentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean timeCurrent = dataSnapshot.getValue(Boolean.class);
                if (Boolean.FALSE.equals(timeCurrent)) {
                    // If `timeCurrent` is false, enable the start game button automatically
                    // and disable the code entry EditText and validate button.
                    startGameButton.setEnabled(true);
                    etCodePin.setEnabled(false); // Disable EditText for entering the code
                    validateButton.setEnabled(false); // Disable validate button
                    tvCodePinMessage.setText("No code required to start the game.");
                } else {
                    // If `timeCurrent` is true or null, keep everything enabled and wait for user to validate code
                    startGameButton.setEnabled(false); // Keep start game button disabled until code is validated
                    etCodePin.setEnabled(true); // Enable EditText for entering the code
                    validateButton.setEnabled(true); // Enable validate button
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("checkTimeCurrent", "Error checking timeCurrent", databaseError.toException());
            }
        });
    }


    private void validateCode() {
        final String userEnteredCode = etCodePin.getText().toString();
        if (userEnteredCode.length() == 6) {
            codePinRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Attempt to fetch the value as a Long first
                    Long codePinLong = dataSnapshot.getValue(Long.class);
                    String storedCodePin = null;

                    if (codePinLong != null) {
                        // Convert Long to String
                        storedCodePin = String.valueOf(codePinLong);
                    }

                    // Proceed with your validation logic using the converted String value
                    if (storedCodePin != null && storedCodePin.equals(userEnteredCode)) {
                        tvCodePinMessage.setText("Verified!"); // Clear any error messages
                        startGameButton.setEnabled(true);
                    } else {
                        tvCodePinMessage.setText("Incorrect code. Please try again.");
                        startGameButton.setEnabled(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle possible errors
                    Log.e("validateCode", "Error fetching codePin", databaseError.toException());
                }
            });
        } else {
            tvCodePinMessage.setText("Please enter a 6-digit code.");
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}