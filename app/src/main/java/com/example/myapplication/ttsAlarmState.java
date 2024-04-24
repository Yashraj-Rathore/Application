package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.antitheft.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ttsAlarmState extends Service {    private DatabaseReference authRef;
    private DatabaseReference iffailRef;
    private MediaPlayer mediaPlayer;
    private boolean auth = true; // Assuming true by default
    private boolean iffail = true; // Assuming true by default

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://eng4k-capstone-server-main2.firebaseio.com/");
        authRef = database.getReference("Authorization");
        iffailRef = database.getReference("iffail");

        // Attach listeners
        attachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        // Listener for the Authorization node
        authRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    auth = dataSnapshot.getValue(Boolean.class);
                    checkConditionsAndPlay();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log the error or handle the cancelled event here
            }
        });

        // Listener for the iffail node
        iffailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    iffail = dataSnapshot.getValue(Boolean.class);
                    checkConditionsAndPlay();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log the error or handle the cancelled event here
            }
        });
    }

    private void checkConditionsAndPlay() {
        if (!auth && iffail) {
            playMP3();
        }
    }

    private void playMP3() {
        // Ensure we do not start multiple instances of MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.my_mp3_file); // Replace with your MP3 file in res/raw
        mediaPlayer.setOnCompletionListener(mp -> mp.release());
        mediaPlayer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        // Clean up listeners here if necessary
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}