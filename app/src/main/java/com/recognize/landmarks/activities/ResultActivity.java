package com.recognize.landmarks.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.recognize.landmarks.MainActivity;
import com.recognize.landmarks.R;
import com.recognize.landmarks.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {
    private static final String TAG = "ResultActivity";
    private Context context = ResultActivity.this;

    private ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent = getIntent();

        binding.name.setText(intent.getStringExtra("name"));
        binding.confidence.setText(intent.getStringExtra("confidence"));
        binding.coordinates.setText(intent.getStringExtra("lat") + ", " + intent.getStringExtra("lng"));

        binding.resultImage.setImageBitmap(MainActivity.mBitmap);
    }
}
