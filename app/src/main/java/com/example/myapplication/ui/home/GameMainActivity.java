package com.example.myapplication.ui.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.myapplication.LoginActivity;
import com.example.myapplication.MainActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.example.antitheft.R;

public class GameMainActivity extends AppCompatActivity implements GameView.GameEventListener {

    private int currentLevel = 1;
    private TextView timerTextView;
    private TextView promptTextView;
    private TextView levelTextView;
    private GameView gameView;
    // Time limits for levels in milliseconds

    private DatabaseReference cognitiveGameResultRef;
    private DatabaseReference cognitiveGameEndRef;
    private DatabaseReference cognitiveGameResetRef;
    private DatabaseReference Authorization;
    private DatabaseReference codePinTrial;
    private DatabaseReference ForceAuthorization;
    private int restartCounter = 0; // Counter for tracking restarts
    private final int maxRestarts = 2; // Maximum number of allowed restarts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamemainactivity);

        // Initialize Firebase database references
        cognitiveGameResultRef = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("CognitiveGameResult");
        cognitiveGameEndRef = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("CognitiveGame_end");
        ForceAuthorization=FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("ForceAuthorization");
        Authorization=FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("Authorization");
        codePinTrial=FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/").getReference("codePinTrial");



        // Initialize the TextViews
        timerTextView = findViewById(R.id.timerTextView);
        promptTextView = findViewById(R.id.promptTextView);
        levelTextView = findViewById(R.id.levelTextView);

        // Create and set up the GameView
        gameView = new GameView(this, this);
        FrameLayout gameViewContainer = findViewById(R.id.gameViewContainer);
        gameViewContainer.addView(gameView);

        // Update the initial level display and start the first level
        levelTextView.setText(getString(R.string.level_default, currentLevel));
        startLevel(currentLevel);
        SharedPreferences sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        Log.d("MAIN_ACTIVITY", "MainActivity onCreate");

    }

    @Override
    public void onNewRound(String shapeName, String colorName) {
        runOnUiThread(() -> promptTextView.setText(getString(R.string.tap_shape_prompt, shapeName, colorName)));
    }

    @Override
    public void onTimerTick(long secondsLeft) {
        // Update the countdown timer
        runOnUiThread(() -> timerTextView.setText(getString(R.string.time_left, secondsLeft)));
    }





    @Override
    public void onGameOver() {
        runOnUiThread(() -> {
            // If the restartCounter is less than maxRestarts, allow the game to restart
            if (restartCounter < maxRestarts) {
                new AlertDialog.Builder(GameMainActivity.this)
                        .setTitle(R.string.game_over)
                        .setMessage(getString(R.string.game_over_message) + " You've reached level " + currentLevel + ". Try again!")
                        .setPositiveButton(R.string.restart, (dialog, which) -> {
                            restartCounter++; // Increment the restart counter
                            restartGame(); // Restart the game
                        })
                        .setNegativeButton(R.string.exit, (dialog, which) -> {

                            cognitiveGameResultRef.setValue(false);
                            cognitiveGameEndRef.setValue(true);

                            Toast.makeText(this, "Navigating back to Home Screen!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.putExtra("navigateTo", "Home"); // "dashboard" is an example key
                            startActivity(intent);
                            finish();

                        }) // Exit the game
                        .show();
            } else {
                // If maxRestarts has been reached, exit the game automatically

                new AlertDialog.Builder(GameMainActivity.this)
                        .setTitle(R.string.game_over)
                        .setMessage("Maximum restarts reached. Exiting game.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            cognitiveGameResultRef.setValue(false);
                            cognitiveGameEndRef.setValue(true);
                            codePinTrial.setValue(true);

                            Toast.makeText(this, "Navigating back to Home Screen!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.putExtra("navigateTo", "Home"); // "dashboard" is an example key
                            startActivity(intent);
                            finish();

                        })
                        .show();

            }
        });

    }

    @Override
    public void onGameSuccess() {
        runOnUiThread(() -> {
            // Update UI elements using resource strings
            timerTextView.setText(getString(R.string.timer_default)); // Use a default or empty timer text
            levelTextView.setText(getString(R.string.game_success_title)); // Display success message

            new AlertDialog.Builder(GameMainActivity.this)
                    .setTitle(getString(R.string.game_success_title))
                    .setMessage(getString(R.string.game_success_message))
                    .setPositiveButton("OK", (dialog, which) -> {
                        cognitiveGameResultRef.setValue(true);
                        cognitiveGameEndRef.setValue(true);
                        ForceAuthorization.setValue(false);
                        Authorization.setValue(true);

                        //startActivity(new Intent(GameMainActivity.this, MainActivity.class));
                        //finish();


//                        Intent intent = new Intent(this, MainActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                        startActivity(intent);
//                        finish();


                        Toast.makeText(this, "Navigating to Dashboard for servo Control.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("navigateTo", "dashboard"); // "dashboard" is an example key
                        startActivity(intent);
                        finish();



                    })
                    .show();
        });
    }

    private void startLevel(int level) {
        // Set the level and time limit for the GameView based on the current level
        gameView.setLevel(level);

    }

    private void restartGame() {
        // Reset game state variables in the database
        cognitiveGameResultRef.setValue(false);
        cognitiveGameEndRef.setValue(false);

        currentLevel = 1; // Reset to level 1
        levelTextView.setText(getString(R.string.level_default, currentLevel));


        startLevel(currentLevel); // Start the first level again

    }

    @Override
    public void onNextLevel(int newLevel) {
        runOnUiThread(() -> {
            currentLevel = newLevel; // Update the currentLevel variable
            levelTextView.setText(getString(R.string.level_default, newLevel)); // Update the level display
            startLevel(newLevel); // Configure the game view for the new level
        });
    }








}
