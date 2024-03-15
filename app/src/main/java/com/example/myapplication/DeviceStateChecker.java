package com.example.myapplication;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DeviceStateChecker {

    private DeviceStateListener listener;

    public interface DeviceStateListener {
        void onStateChanged(DeviceState state);
    }

    public void setDeviceStateListener(DeviceStateListener listener) {
        this.listener = listener;
    }

    public void checkDeviceState() {
        DatabaseReference stateRef = FirebaseDatabase.getInstance().getReference("deviceState");

        stateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer stateCode = dataSnapshot.getValue(Integer.class);
                    DeviceState currentState = DeviceState.fromStateCode(stateCode != null ? stateCode : 0);
                    if(listener != null) {
                        listener.onStateChanged(currentState);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
}
