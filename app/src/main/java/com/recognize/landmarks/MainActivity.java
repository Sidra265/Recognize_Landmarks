package com.recognize.landmarks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;
import com.recognize.landmarks.activities.ResultActivity;
import com.recognize.landmarks.databinding.ActivityMainBinding;
import com.recognize.landmarks.helpers.MyHelper;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Context context = MainActivity.this;

    private ActivityMainBinding binding;

    public static Bitmap mBitmap;
    private FirebaseVisionCloudDetectorOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        options = new FirebaseVisionCloudDetectorOptions.Builder()
                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                        .setMaxResults(15)
                        .build();

        binding.select.setOnClickListener(v -> CropImage.startPickImageActivity(MainActivity.this));
        binding.search2.setOnClickListener(view1 -> {
            if (mBitmap != null){
                MyHelper.showDialog(context);

                detectLandmark(mBitmap);
            } else

                Toast.makeText(context, "Please choose an image first", Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri filePath = CropImage.getPickImageResultUri(this, data);
            CropImage.activity(filePath)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMultiTouchEnabled(true)
                    //REQUEST COMPRESS SIZE
                    .setRequestedSize(800, 800)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Log.d(TAG, result.getUri().toString());

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());
                    Log.d(TAG, "onActivityResult: " + mBitmap);

                    binding.imageView3.setImageBitmap(mBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void detectLandmark(Bitmap mBitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector(options);

        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
            @Override
            public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                MyHelper.dismissDialog();

                Intent intent = new Intent(context, ResultActivity.class);

                Log.d(TAG, "onSuccess: " + firebaseVisionCloudLandmarks);

                for (FirebaseVisionCloudLandmark landmark : firebaseVisionCloudLandmarks) {
                    String landmarkName = landmark.getLandmark();
                    float confidence = landmark.getConfidence();

                    intent.putExtra("name", landmarkName);
                    intent.putExtra("confidence", String.valueOf(confidence));

                    for (FirebaseVisionLatLng loc: landmark.getLocations()) {

                        intent.putExtra("lat", String.valueOf(loc.getLatitude()));
                        intent.putExtra("lng", String.valueOf(loc.getLongitude()));

                        startActivity(intent);

                        break;
                    }

                    break;
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                MyHelper.dismissDialog();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        });

    }
}