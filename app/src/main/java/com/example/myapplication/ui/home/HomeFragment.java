package com.example.myapplication.ui.home;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private EditText etCodePin;
    private TextView tvCodePinMessage;
    private Button validateButton;
    private Button startGameButton;
    private DatabaseReference timeCurrentRef;
    private DatabaseReference codePinRef;
    private DatabaseReference ForceAuthorization;

    private DatabaseReference codePin_end;
    private DatabaseReference codePin_result;
    private String currentCodePin = "";
    private Boolean timeCurrentStatus = null;
    private CountDownTimer countDownTimer;
    Boolean forceAuthorization=false;




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
        validateButton.setEnabled(false);
        etCodePin.setEnabled(false);

        timeCurrentRef = FirebaseDatabase.getInstance().getReference("timeCurrent");
        codePinRef = FirebaseDatabase.getInstance().getReference("codePin");
        codePin_end = FirebaseDatabase.getInstance().getReference("codePin_end");
        codePin_result = FirebaseDatabase.getInstance().getReference("codePin_result");
        codePin_result = FirebaseDatabase.getInstance().getReference("codePin_result");
        ForceAuthorization=FirebaseDatabase.getInstance().getReference("ForceAuthorization");
        ForceAuthorization.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                forceAuthorization = dataSnapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(forceAuthorization)) {
                    startGameButton.setEnabled(true);
                    tvCodePinMessage.setText("Bypassing Codepin due to Force Authorization");

                }
                else {
                    startGameButton.setEnabled(false); // Or any default state you want
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AuthorizationListener", "Error listening for Authorization", databaseError.toException());
            }
        });





        // Continuously listen for new codePin
        codePinRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer newCodePin = dataSnapshot.getValue(Integer.class);

                if (newCodePin != null && isAdded()) { // Ensure the fragment is currently added to its activity
                    String newCodePinString = String.valueOf(newCodePin);
                    String lastKnownCodePin = getLastKnownCodePin();

                    if (!newCodePinString.equals(lastKnownCodePin)) {
                        saveLastKnownCodePin(newCodePinString);
                        // New code pin detected, update UI and logic accordingly
                        etCodePin.setEnabled(true);
                        validateButton.setEnabled(true);
                        tvCodePinMessage.setText("New code detected. Please enter the code.");
                        startTimer();

                    } else {
                        // No new code pin, keep the UI in its current state or make adjustments as needed
                        // This might involve disabling or enabling UI components based on other conditions
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("codePinRefListener", "Error listening for codePin changes", databaseError.toException());
            }
        });



        checkTimeCurrentAndSetup();
        validateButton.setOnClickListener(v -> validateCode());

        startGameButton.setOnClickListener(v -> {
            // Start GameMainActivity
            Intent intent = new Intent(getActivity(), ColorSelection.class);
            startActivity(intent);
        });



    }


    @Override
    public void onResume() {
        super.onResume();
        // Clear the EditText and any messages
        etCodePin.setEnabled(false);
        etCodePin.setText("");
        tvCodePinMessage.setText("");
        // Reset the validation and game start state as necessary
        validateButton.setEnabled(false);

        ForceAuthorization.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                forceAuthorization = dataSnapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(forceAuthorization)) {
                    startGameButton.setEnabled(true);
                    tvCodePinMessage.setText("Bypassing Codepin due to Force Authorization");

                }
                else {
                    startGameButton.setEnabled(false); // Or any default state you want
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AuthorizationListener", "Error listening for Authorization", databaseError.toException());
            }
        });


        // Add other UI reset logic here if needed
    }




    private void startTimer() {
        // Cancel the existing timer if it's already running
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // 1 minute countdown timer with 1 second tick intervals
        countDownTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                // Update the UI with the remaining time in a readable format
                int secondsLeft = (int) (millisUntilFinished / 1000);
                tvCodePinMessage.setText(String.format(Locale.getDefault(), "Stay on this screen and enter the code within %d seconds", secondsLeft));
            }

            public void onFinish() {
                // Disable EditText, validate button, and display message when timer finishes
                etCodePin.setEnabled(false);
                validateButton.setEnabled(false);
                tvCodePinMessage.setText("Time's up! Please wait for a new code.");

                // Optionally reset the current code pin to prevent validation attempts after expiration
                currentCodePin = "";
            }
        }.start();
    }



    private String getLastKnownCodePin() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("lastKnownCodePin", "");
    }

    private void saveLastKnownCodePin(String codePin) {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("lastKnownCodePin", codePin);
        editor.apply();
    }


// Additional methods to save and retrieve timer start time if implementing timer logic

    private void checkTimeCurrentAndSetup() {
        timeCurrentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean timeCurrent = dataSnapshot.getValue(Boolean.class);
                timeCurrentStatus = timeCurrent; // Update the latest status
                if (Boolean.FALSE.equals(timeCurrent)) {
                    // Keep startGameButton disabled as per requirement
                    //startGameButton.setEnabled(false);
                }
                // No else part needed here as we want to validate the code no matter what
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("checkTimeCurrent", "Error checking timeCurrent", databaseError.toException());
            }
        });
    }


    private void validateCode() {
        String userEnteredCode = etCodePin.getText().toString();
        if (isAdded() && userEnteredCode.equals(getLastKnownCodePin())) {
            // Correct code entered
            tvCodePinMessage.setText("Code verified! Stay on this screen and Start Game");
            codePin_end.setValue(true);
            codePin_result.setValue(true);


            // Cancel the timer since the code has been verified
            if (countDownTimer != null) {
                countDownTimer.cancel();
                // Optionally reset UI elements affected by the timer, if necessary
            }
            processAfterCodeVerification();
        } else {
            tvCodePinMessage.setText("Incorrect code. Please try again.");
            //startGameButton.setEnabled(false);
        }
    }



    private void processAfterCodeVerification() {
        if (Boolean.FALSE.equals(timeCurrentStatus)) {
            // Directly set CognitiveGameResult to true if timeCurrent is false
            FirebaseDatabase.getInstance().getReference("CognitiveGameResult").setValue(true);
            tvCodePinMessage.setText("Code verified! Navigate to Dashboard for servo control");
            //startGameButton.setEnabled(false); // Keep startGameButton disabled
            etCodePin.setEnabled(false); // Disable EditText
            validateButton.setEnabled(false); // Disable validate button
        } else if (Boolean.TRUE.equals(timeCurrentStatus)) {
            // Enable the startGameButton only if timeCurrent is true
            startGameButton.setEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel the countdown timer

        binding = null;
    }
}