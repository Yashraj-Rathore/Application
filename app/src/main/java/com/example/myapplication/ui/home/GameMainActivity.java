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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamemainactivity);

        // Initialize Firebase database references
        cognitiveGameResultRef = FirebaseDatabase.getInstance().getReference("CognitiveGameResult");
        cognitiveGameEndRef = FirebaseDatabase.getInstance().getReference("CognitiveGameEnd");
        cognitiveGameResetRef = FirebaseDatabase.getInstance().getReference("CognitiveGameReset");

        // Listen for the cognitiveGameReset variable change
        cognitiveGameResetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean reset = dataSnapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(reset)) {
                    // Reset the cognitiveGameResult and cognitiveGameEnd to false
                    cognitiveGameResultRef.setValue(false);
                    cognitiveGameEndRef.setValue(false);
                    cognitiveGameResetRef.setValue(false); // Optionally reset the cognitiveGameReset too
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("GameMainActivity", "Failed to read cognitiveGameReset.", databaseError.toException());
            }
        });

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
            // Show a game over message or dialog
            new AlertDialog.Builder(GameMainActivity.this)
                    .setTitle(R.string.game_over)
                    .setMessage(getString(R.string.game_over_message) + " You've reached level " + currentLevel + ". Try again!")
                    .setPositiveButton(R.string.restart, (dialog, which) -> restartGame()) // Restart the game
                    .setNegativeButton(R.string.exit, (dialog, which) -> {
                        cognitiveGameEndRef.setValue(true);

//                        Intent intent = new Intent(this, com.example.myapplication.MainActivity.class); // Assuming MainActivity hosts HomeFragment
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                        startActivity(intent);
//
                        //startActivity(new Intent(GameMainActivity.this, MainActivity.class));
                        //finish();

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();


                    }) // Exit the game
                    .show();
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

                        //startActivity(new Intent(GameMainActivity.this, MainActivity.class));
                        //finish();


                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
