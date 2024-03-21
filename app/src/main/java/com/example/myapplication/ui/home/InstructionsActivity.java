package com.example.myapplication.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.antitheft.R;

public class InstructionsActivity extends AppCompatActivity {

    private String colorVisionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        colorVisionType = getIntent().getStringExtra("ColorVisionType"); // Receive the color vision type

        Button readyButton = findViewById(R.id.readyButton);
        readyButton.setOnClickListener(view -> startGame());
    }

    private void startGame() {
        Intent intent = new Intent(InstructionsActivity.this, GameMainActivity.class);
        intent.putExtra("ColorVisionType", colorVisionType); // Pass the color vision type to MainActivity
        startActivity(intent);
    }




}
