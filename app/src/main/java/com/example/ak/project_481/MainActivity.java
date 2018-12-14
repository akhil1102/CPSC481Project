package com.example.ak.project_481;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.translate.AmazonTranslateAsyncClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
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
    private ItemAdapter itemAdapter;
    private List<FirebaseVisionLabel> labels = new ArrayList<>();
    private GraphicOverlay mGraphicOverlay;
    private ImageView imageView;
    boolean doubleBackToExitPressedOnce = false;
    private DrawerLayout drawerLayout;
    private NestedScrollView nestedScrollView;
    private RecyclerView rvLabels;
    private NavigationView navigationView;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private List<String> originalTextList = new ArrayList<>();
    private String translatedJoined = "";
    private String originalJoined="";
    private CardView cardView;
    private TextView originaltv;
    private TextView translatedtv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraKitView = findViewById(R.id.camera);
        imageView = findViewById(R.id.imagePreview);
        mGraphicOverlay = findViewById(R.id.graphic_overlay);
        photoButton = findViewById(R.id.photoButton);
        photoButton.setOnClickListener(photoOnClickListener);
        nestedScrollView = findViewById(R.id.nestedSV);
        nestedScrollView.setVisibility(View.GONE);
        rvLabels = findViewById(R.id.rvLabels);
        rvLabels.setVisibility(View.GONE);
        itemAdapter = new ItemAdapter(MainActivity.this, labels);
        rvLabels.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        rvLabels.setAdapter(itemAdapter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        cardView = findViewById(R.id.cardview);
        cardView.setVisibility(View.GONE);
        originaltv = findViewById(R.id.originalString);
        translatedtv = findViewById(R.id.translatedString);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
    public void onBackPressed() {
        photoButton.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        nestedScrollView.setVisibility(View.GONE);
        cardView.setVisibility(View.GONE);
        cameraKitView.onResume();
        mGraphicOverlay.clear();
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        //Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
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
            cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                @Override
                public void onImage(CameraKitView cameraKitView, byte[] picture) {
                    Bitmap result = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                    Log.d("item id:", Integer.toString(getCheckedItem(navigationView)));
                    //depending on the mode selected the image is sent to that particular function
                    if (getCheckedItem(navigationView) == 0) {      //0 = id of Image Labelling in the side navigation drawer
                        getLabelsFromDevice(result);
                    } else if (getCheckedItem(navigationView) == 1) {   //1 = Text Recognition in the side navigation drawer
                        runTextRecognition(result);

                    }
                }
            });
        }
    };

    private int getCheckedItem(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isChecked()) {
                return i;
            }
        }

        return -1;
    }

    private void getLabelsFromDevice(final Bitmap bitmap) {
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
                photoButton.setVisibility(View.GONE);
                imageView.setImageBitmap(bitmap);
                cameraKitView.onPause();
                imageView.setVisibility(View.VISIBLE);
                nestedScrollView.setVisibility(View.VISIBLE);
                rvLabels.setVisibility(View.VISIBLE);
            }
        });
    }


    private void runTextRecognition(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                translatedtv.setText("");
                originaltv.setText("");
            }
        });
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        recognizer.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                //mTextButton.setEnabled(true);
                                processTextRecognitionResult(texts);
                                imageView.setImageBitmap(bitmap);
                                cameraKitView.onPause();
                                photoButton.setVisibility(View.GONE);
                            }

                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                e.printStackTrace();
                            }
                        });
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
            if (blocks.size() == 0) {
                Toast.makeText(MainActivity.this, "Text Not Recognized", Toast.LENGTH_SHORT).show();
                return;
            }
            originalJoined = "";
            translatedJoined = "";
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    Log.d("text: ", elements.get(k).getText());
                    originalTextList.add(elements.get(k).getText());
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);
                    originalJoined = originalJoined + " "+ elements.get(k).getText();
                }
            }
            getTranslatedText(originalJoined);
        }
    }

    private void getTranslatedText(String request) {
        AWSCredentials awsCredentials = new AWSCredentials() {
            @Override   //ACCESS KEY REQUIRED
            public String getAWSAccessKeyId() {
                return "<ACCESS KEY>";
            }

            @Override   //SECRET KEY REQUIRED
            public String getAWSSecretKey() {
                return "<SECRET KEY>";
            }
        };
        final AmazonTranslateAsyncClient translateAsyncClient = new AmazonTranslateAsyncClient(awsCredentials);
        final TranslateTextRequest translateTextRequest = new TranslateTextRequest()
                .withText(request)
                .withSourceLanguageCode("auto")
                .withTargetLanguageCode("en");
        translateAsyncClient.translateTextAsync(translateTextRequest, new AsyncHandler<TranslateTextRequest, TranslateTextResult>() {
            @Override
            public void onError(Exception e) {
                Log.e(LOG_TAG, "Error occurred in translating the text: " + e.getLocalizedMessage());
            }

            @Override
            public void onSuccess(TranslateTextRequest request, TranslateTextResult translateTextResult) {
                Log.d(LOG_TAG, "Original Text: " + request.getText());
                Log.d(LOG_TAG, "Translated Text: " + translateTextResult.getTranslatedText());
                translatedJoined = translateTextResult.getTranslatedText();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cameraKitView.onPause();
                        originaltv.setText("Original text: "+originalJoined);
                        translatedtv.setText("Translation: "+translatedJoined);
                        Log.d("translate string", translatedtv.getText().toString());
                        cardView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }



}
