package com.example.myapplication.ui.gallery;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.antitheft.R;

public class FullScreenImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        ImageView fullScreenImageView = findViewById(R.id.fullscreenImageView);
        String imageUrl = getIntent().getStringExtra("image_url");

        Glide.with(this).load(imageUrl).into(fullScreenImageView);
    }
}
