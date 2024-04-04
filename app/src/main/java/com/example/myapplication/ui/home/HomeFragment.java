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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
    private DatabaseReference codePinTrial;

    private DatabaseReference codePin_end;
    private DatabaseReference codePin_result;
    private String currentCodePin = "";

    private boolean isCodePinEndActive = false;
    String newCodePinString="";

    String lastKnownCodePin="";
    private Boolean timeCurrentStatus = null;
    private CountDownTimer countDownTimer;
    Boolean forceAuthorization=false;

    private CountDownTimer forceAuthorizationTimer;
    private boolean isForceAuthorizationActive = false;

    private boolean skipNextCodePinLogic = false;
    // Variable to track the last CodePin value processed
    private String lastProcessedCodePin = "";

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
        etCodePin.setVisibility(View.GONE);
        startGameButton.setEnabled(false);
        validateButton.setEnabled(false);
        etCodePin.setEnabled(false);


        timeCurrentRef = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("timeCurrent");
        codePinRef = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePin");
        codePin_end = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePin_end");
        codePin_result = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePin_result");
        codePin_result = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePin_result");
        ForceAuthorization=FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("ForceAuthorization");
        codePinTrial=FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePinTrial");

        listenToCodePinEnd();

//        // Continuously listen for new codePin
//        codePinRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Integer newCodePin = dataSnapshot.getValue(Integer.class);
//
//                if (newCodePin != null && isAdded()) { // Ensure the fragment is currently added to its activity
//                    codePinTrial.setValue(false);
//                    newCodePinString = String.valueOf(newCodePin);
//                    lastKnownCodePin = getLastKnownCodePin();
//
//                    if (!newCodePinString.equals(lastKnownCodePin)) {
//                        // New code pin detected, update UI and logic accordingly
//                        etCodePin.setEnabled(true);
//                        validateButton.setEnabled(true);
//                        etCodePin.setVisibility(View.VISIBLE);
//                        tvCodePinMessage.setText("New code detected. Please enter the code.");
//                        etCodePin.setHint("Enter 6-digit code");
//
//                        // Check if the timer is already running, if not, start a new timer
//                        if (!checkIfCodePinTimerIsActive()) {
//                            startCodePinTimer(30000); // Start a new timer for 30 seconds
//                        } else {
//                            long currentTime = System.currentTimeMillis();
//                            SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
//                            long endTime = prefs.getLong("codePinEndTime", 0);
//                            long durationLeft = endTime - currentTime;
//                            startCodePinTimer(durationLeft); // Continue the timer with the remaining duration
//                        }
//
//                    } else {
//                        // If the code pin hasn't changed and the timer is not active, reset UI elements
//                        if (!checkIfCodePinTimerIsActive()) {
//                            etCodePin.setVisibility(View.GONE);
//                            etCodePin.setHint("");
//                            tvCodePinMessage.setText("No new code pin detected.");
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e("codePinRefListener", "Error listening for codePin changes", databaseError.toException());
//            }
//        });
//
//
//        checkTimeCurrentAndSetup();
//        validateButton.setOnClickListener(v -> validateCode());
//
//        startGameButton.setOnClickListener(v -> {
//            // Start GameMainActivity
//            Intent intent = new Intent(getActivity(), ColorSelection.class);
//            startActivity(intent);
//        });


    }



    @Override
    public void onResume() {
        super.onResume();
//        // Clear the EditText and any messages
//        etCodePin.setEnabled(false);
//        etCodePin.setText("");
//        tvCodePinMessage.setText("");
//        // Reset the validation and game start state as necessary
//        validateButton.setEnabled(false);



        // Declare a global variable to keep track of ForceAuthorization status


        ForceAuthorization.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean forceAuthorization = dataSnapshot.getValue(Boolean.class);
                isForceAuthorizationActive = Boolean.TRUE.equals(forceAuthorization); // Update the global variable
                if (isForceAuthorizationActive) {
                    // Prioritize ForceAuthorization logic
                    startGameButton.setEnabled(true);
                    skipNextCodePinLogic = true;
                    if (checkIfTimerIsActive()) {
                        long currentTime = System.currentTimeMillis();
                        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                        long endTime = prefs.getLong("forceAuthorizationEndTime", 0);
                        long durationLeft = endTime - currentTime;
                        startSharedPreferencesTimer(durationLeft); // Continue the timer with the remaining duration
                    } else {
                        startSharedPreferencesTimer(30000); // Start a new timer for 30 seconds
                    }
                } else {
                    startGameButton.setEnabled(false);
                    etCodePin.setHint("");
                    tvCodePinMessage.setText("Please get Code or ForceAuthorize.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AuthorizationListener", "Error listening for Authorization", databaseError.toException());
            }
        });

        codePinRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isForceAuthorizationActive) {
                    // If ForceAuthorization is active, skip the CodePin logic
                    return;
                }

                Integer newCodePin = dataSnapshot.getValue(Integer.class);

                if (newCodePin== 0 && isAdded()) {

                    saveLastKnownCodePin(String.valueOf(newCodePin));

                    lastKnownCodePin = getLastKnownCodePin();
                    newCodePinString = String.valueOf(newCodePin);

                    Log.d("CodePinDebug", "LastKnownCodePin: " + lastKnownCodePin + ", NewCodePin: " + newCodePinString);
                }


                if (newCodePin != null &&  isAdded() && newCodePin != 0 ) { // Ensure the fragment is currently added to its activity
                    codePinTrial.setValue(false);
                    newCodePinString = String.valueOf(newCodePin);
                    lastKnownCodePin = getLastKnownCodePin();
                    Log.d("CodePinDebug", "LastKnownCodePin: " + lastKnownCodePin + ", NewCodePin: " + newCodePinString);
                    if (!newCodePinString.equals(lastKnownCodePin) && isAdded()) {
                        // New code pin detected, update UI and logic accordingly
                        etCodePin.setEnabled(true);
                        validateButton.setEnabled(true);
                        etCodePin.setVisibility(View.VISIBLE);
                        tvCodePinMessage.setText("New code detected. Please enter the code.");

                        etCodePin.setHint("Enter 6-digit code");

                            if (skipNextCodePinLogic) {
                                // Skip this round of CodePin logic due to recent ForceAuthorization
                                skipNextCodePinLogic = false; // Reset flag to not skip future CodePin changes
                                                               // Update last processed CodePin
                                return; // Skip further processing for this CodePin change
                            }


                        // Check if the timer is already running, if not, start a new timer
                        if (!checkIfCodePinTimerIsActive()) {
                            startCodePinTimer(30000); // Start a new timer for 30 seconds
                        } else {
                            long currentTime = System.currentTimeMillis();
                            SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                            long endTime = prefs.getLong("codePinEndTime", 0);
                            long durationLeft = endTime - currentTime;
                            startCodePinTimer(durationLeft); // Continue the timer with the remaining duration
                        }
                    } else {
                        // If the code pin hasn't changed and the timer is not active, reset UI elements
                        if (!checkIfCodePinTimerIsActive()) {
                            etCodePin.setVisibility(View.GONE);
                            etCodePin.setHint("");
                            tvCodePinMessage.setText("No new code pin detected.");
                        }
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



        // Add other UI reset logic here if needed
    }


    private void listenToCodePinEnd() {

        codePin_end.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean state = dataSnapshot.getValue(Boolean.class);
                if (state != null) {
                    isCodePinEndActive = state;
                    // Optionally, invoke any methods that depend on this state change

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("listenToCodePinEnd", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void startCodePinTimer(long duration) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        long endTime = System.currentTimeMillis() + duration;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("codePinEndTime", endTime);
        editor.apply();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                if (isAdded()) {
                    tvCodePinMessage.setText(String.format(Locale.getDefault(), "Enter the code within %d seconds", secondsLeft));
                }
            }

            public void onFinish() {
                //codePin_end.setValue(true);
                codePinRef.setValue(0);
                codePinTrial.setValue(false);
                Log.d("CodePinDebug", "isCodePinEndActive: " + isCodePinEndActive);

                if (isAdded()) {
                    etCodePin.setEnabled(false);
                    validateButton.setEnabled(false);
                    etCodePin.setVisibility(View.GONE);
                    tvCodePinMessage.setText("Time's up! Please wait for a new code.");
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit();
                    saveLastKnownCodePin(newCodePinString);
                    editor.remove("codePinEndTime").apply(); // Clear timer end time

                }
            }
        }.start();
    }


    private boolean checkIfCodePinTimerIsActive() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        long endTime = prefs.getLong("codePinEndTime", 0);
        return System.currentTimeMillis() < endTime;
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

    private void startSharedPreferencesTimer(long duration) {

        if (isAdded()) { // Check if the fragment is currently added to its activity
            long endTime = System.currentTimeMillis() + duration;
            SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("forceAuthorizationEndTime", endTime);
            editor.apply();
        }


        // Start a countdown based on the remaining time until "endTime"
        if (forceAuthorizationTimer != null) {
            forceAuthorizationTimer.cancel();
        }
        forceAuthorizationTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                // Update the message with the remaining time
                int secondsLeft = (int) (millisUntilFinished / 1000);

                if (isAdded()) { // Check if fragment is currently added to its activity
                    tvCodePinMessage.setText(String.format(Locale.getDefault(), "Bypassing Codepin due to Force Authorization. Click Start within %d seconds", secondsLeft));
                }
            }
            public void onFinish() {
                ForceAuthorization.setValue(false);
                codePinRef.setValue(0);
                skipNextCodePinLogic = false;
                if (isAdded()) { // Check if fragment is currently added to its activity
                    startGameButton.setEnabled(false);
                    tvCodePinMessage.setText("Force Authorization period has expired. Please get Code or ForceAuthorize again.");
                    etCodePin.setHint("");
                    // Safely access getActivity() as we've confirmed the fragment is added
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit();
                    editor.remove("forceAuthorizationEndTime");
                    editor.apply();
                }
            }
        }.start();
    }

    private boolean checkIfTimerIsActive() {
        if (isAdded()) { // Check if the fragment is currently added to its activity
            SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            long endTime = prefs.getLong("forceAuthorizationEndTime", 0);
            return System.currentTimeMillis() < endTime;
        } else {
            // Return a default value (e.g., false) if the fragment is not attached
            return false;
        }
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
        if (isAdded() && userEnteredCode.equals(newCodePinString)) {
            // Correct code entered
            tvCodePinMessage.setText("Code verified! Stay on this screen and Start Game");
            codePin_end.setValue(true);
            codePin_result.setValue(true);
            saveLastKnownCodePin(newCodePinString);

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
            FirebaseDatabase.getInstance("https://eng4k-capstone-server-main.firebaseio.com/").getReference("CognitiveGameResult").setValue(true);
            tvCodePinMessage.setText("Code verified!");
            //startGameButton.setEnabled(false); // Keep startGameButton disabled
            etCodePin.setEnabled(false); // Disable EditText
            validateButton.setEnabled(false); // Disable validate button

            Toast.makeText(getActivity(), "Code verified! Navigating Dashboard for servo control!", Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_HomeFragment_to_DashboardFragment);



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