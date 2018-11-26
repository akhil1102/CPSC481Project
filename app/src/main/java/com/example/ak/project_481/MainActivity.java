package com.example.ak.project_481;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private CameraKitView cameraKitView;
    private FloatingActionButton photoButton;
    private FrameLayout framePreview;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<FirebaseVisionLabel> labels = new ArrayList<>();
    BottomSheetBehavior sheetBehavior;
    private GraphicOverlay mGraphicOverlay;
    private ImageView imageView;
    boolean doubleBackToExitPressedOnce = false;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraKitView = findViewById(R.id.camera);
        imageView = findViewById(R.id.imagePreview);
        //framePreview = findViewById(R.id.framePreview);
        mGraphicOverlay = findViewById(R.id.graphic_overlay);
        photoButton = findViewById(R.id.photoButton);
        photoButton.setOnClickListener(photoOnClickListener);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);

        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                Toast.makeText(MainActivity.this, "My Account",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        //ImageButton btnRetry = findViewById(R.id.btnRetry);
//        btnRetry.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(cameraKitView.getVisibility()==View.VISIBLE){
//                    showPreview();
//                }else{
//                    hidePreview();
//                }
//            }
//        });
        //sheetBehavior.setPeekHeight(224);
//        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View view, int i) {
//
//            }
//
//            @Override
//            public void onSlide(@NonNull View view, float v) {
//
//            }
//        });
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
    public void onBackPressed(){
        imageView.setVisibility(View.GONE);
        cameraKitView.onResume();
        mGraphicOverlay.clear();
        if(doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        //Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
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
                    //getLabelsFromDevice(result);
                    runTextRecognition(result);
                }
            });
//            showBottomSheetDialog();
        }
    };

    private void getLabelsFromDevice(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector();

        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                labels.clear();
                labels.addAll(firebaseVisionLabels);
                itemAdapter.notifyDataSetChanged();
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView = findViewById(R.id.rvLabels);
                itemAdapter = new ItemAdapter(MainActivity.this, labels );
                recyclerView.setAdapter(itemAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }
    public void showBottomSheetDialog() {
        //View view = getLayoutInflater().inflate(R.layout.fragment_bottom_sheet, null);
        BottomSheetLabelFragment labels = new BottomSheetLabelFragment();
        labels.show(getSupportFragmentManager(), "");
    }

    private void runTextRecognition(final Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        //mTextButton.setEnabled(false);
        recognizer.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                //mTextButton.setEnabled(true);
                                processTextRecognitionResult(texts);
                                imageView.setImageBitmap(bitmap);
                                cameraKitView.onPause();
                            }

                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
//                                mTextButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }
        private void processTextRecognitionResult(FirebaseVisionText texts) {
            List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
//            if (blocks.size() == 0) {
//                showToast("No text found");
//                return;
//            }
            //mGraphicOverlay.clear();
            for (int i = 0; i < blocks.size(); i++) {
                List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
                for (int j = 0; j < lines.size(); j++) {
                    List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                    for (int k = 0; k < elements.size(); k++) {
                        Log.d("text: ",elements.get(k).getText());
                        GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                        mGraphicOverlay.add(textGraphic);
                    }
                }
            }


        }

    }
