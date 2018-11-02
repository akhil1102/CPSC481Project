package com.example.ak.project_481;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private CameraKitView cameraKitView;
    private Button photoButton;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraKitView = findViewById(R.id.camera);

        photoButton = findViewById(R.id.photoButton);
        photoButton.setOnClickListener(photoOnClickListener);
    }
    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private View.OnClickListener photoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            cameraKitView.captureImage(new CameraKitView.ImageCallback() {
//                @Override
//                public void onImage(CameraKitView cameraKitView, byte[] photo) {
//                    File savedPhoto = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
//                    try{
//                        FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
//                        outputStream.write(photo);
//                    } catch(IOException e){
//                        e.printStackTrace();
//                        Log.e("demo", "exception in photo callback");
//                    }
//                }
//            });
            cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                @Override
                public void onImage(CameraKitView cameraKitView, byte[] picture) {
                    Bitmap result = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                    getLabelsFromDevice(result);
                }
            });
        }
    };

    private void getLabelsFromDevice(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector();

        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                for (FirebaseVisionLabel label : firebaseVisionLabels) {
                    Log.d("label", label.getLabel());
                    Log.d("label", Float.toString(label.getConfidence()));
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
