package com.example.myapplication.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.antitheft.R;

public class ColorSelection extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_vision_selection);

        setupButton(R.id.btnNominal, "Nominal");
        setupButton(R.id.btnProtanopia, "Protanopia");
        setupButton(R.id.btnDeuteranopia, "Deuteranopia");
        setupButton(R.id.btnTritanopia, "Tritanopia");
    }

    private void setupButton(int buttonId, final String colorVisionType) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(view -> {
            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("ColorVisionType", colorVisionType);
            editor.apply();
            Log.d("GAME_PREFS", "Color Vision preference set to: " + colorVisionType); // Add this logging line
            Log.d("GAME_PREFS", "Color Vision preference set to: " + colorVisionType + " in ColorSelection");

            // Start InstructionsActivity with the color vision type
            Intent intent = new Intent(ColorSelection.this, InstructionsActivity.class);
            intent.putExtra("ColorVisionType", colorVisionType);
            startActivity(intent);
        });
    }

}
