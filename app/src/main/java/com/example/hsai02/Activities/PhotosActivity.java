package com.example.hsai02.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.hsai02.GeminiCallback;
import com.example.hsai02.GeminiManager;
import com.example.hsai02.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PhotosActivity extends AppCompatActivity {

    TextView tVCount, tVDetails;
    String currentPath, imagePath;
    Bitmap imageBitmap;
    GeminiManager geminiManager;
    int photosCount;
    ArrayList<Bitmap> photos;
    private final String TAG = "PhotosActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 102;
    private static final int REQUEST_FULL_IMAGE_CAPTURE = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        tVCount = findViewById(R.id.tVCount);
        tVDetails = findViewById(R.id.tVDetails);
        tVDetails.setMovementMethod(new ScrollingMovementMethod());

        geminiManager = GeminiManager.getInstance();

        photosCount = 0;
        photos = new ArrayList<>();

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    /**
     * onRequestPermissionsResult method
     * <p> Method triggered by other activities returning result of permissions request
     * </p>
     *
     * @param requestCode the request code triggered the activity
     * @param permissions the array of permissions granted
     * @param grantResults the array of permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * takeFull method
     * <p> Taking a full resolution photo by camera to upload to Firebase Storage
     * </p>
     *
     * @param view the view that triggered the method
     */
    public void addPhoto(View view) {
        // creating local temporary file to store the full resolution photo
        String filename = "tempfile";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imgFile = File.createTempFile(filename,".jpg",storageDir);
            imagePath = imgFile.getAbsolutePath();
            currentPath = imgFile.getParentFile().getAbsolutePath();
            Uri imageUri = FileProvider.getUriForFile(PhotosActivity.this,"com.example.hsai02.fileprovider",imgFile);
            Intent takePicIntent = new Intent();
            takePicIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            takePicIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,imageUri);
            if (takePicIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePicIntent, REQUEST_FULL_IMAGE_CAPTURE);
            }
        } catch (IOException e) {
            Toast.makeText(PhotosActivity.this,"Failed to create temporary file",Toast.LENGTH_LONG);
            throw new RuntimeException(e);
        }
    }


    /**
     * Prompting selected image file to Gemini
     * <p>
     *
     * @param requestCode   The call sign of the intent that requested the result
     * @param resultCode    A code that symbols the status of the result of the activity
     * @param data_back     The data returned
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data_back) {
        super.onActivityResult(requestCode, resultCode, data_back);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_FULL_IMAGE_CAPTURE) {
                imageBitmap = BitmapFactory.decodeFile(imagePath);
                if (imageBitmap != null) {
                    photosCount++;
                    photos.add(imageBitmap);
                    Toast.makeText(this, "Photo added to photos list", Toast.LENGTH_LONG).show();
                    tVCount.setText("Number of photos: " + photosCount);
                } else {
                    Toast.makeText(this, "Error saving image to list.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void photosPrompt(View view) {
        if (photosCount != 0) {
            ProgressDialog pD = new ProgressDialog(this);
            pD.setTitle("Sent Prompt");
            pD.setMessage("Waiting for response...");
            pD.setCancelable(false);
            pD.show();
            String dishIngredientsSchema = "{\n" +
                    "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                    "  \"title\": \"Dish Ingredients\",\n" +
                    "  \"description\": \"Schema for ingredients in a dish, designed for a dietary diary.\",\n" +
                    "  \"type\": \"array\",\n" +
                    "  \"items\": {\n" +
                    "    \"type\": \"object\",\n" +
                    "    \"properties\": {\n" +
                    "      \"name\": {\n" +
                    "        \"type\": \"string\",\n" +
                    "        \"description\": \"The name of the ingredient.\",\n" +
                    "        \"example\": \"Chicken Breast\"\n" +
                    "      },\n" +
                    "      \"quantity\": {\n" +
                    "        \"type\": \"number\",\n" +
                    "        \"description\": \"The quantity of the ingredient used.\",\n" +
                    "        \"minimum\": 0,\n" +
                    "        \"example\": 150\n" +
                    "      },\n" +
                    "      \"unit\": {\n" +
                    "        \"type\": \"string\",\n" +
                    "        \"description\": \"The unit of measurement for the quantity.\",\n" +
                    "        \"enum\": [\"g\", \"kg\", \"ml\", \"L\", \"piece\", \"cup\", \"tablespoon\", \"teaspoon\", \"oz\", \"lb\"],\n" +
                    "        \"example\": \"g\"\n" +
                    "      },\n" +
                    "      \"calories\": {\n" +
                    "        \"type\": \"number\",\n" +
                    "        \"description\": \"The caloric content of the ingredient, per the specified quantity.\",\n" +
                    "        \"minimum\": 0,\n" +
                    "        \"example\": 230\n" +
                    "      },\n" +
                    "      \"protein\": {\n" +
                    "        \"type\": \"number\",\n" +
                    "        \"description\": \"The protein content of the ingredient, per the specified quantity.\",\n" +
                    "        \"minimum\": 0,\n" +
                    "        \"example\": 30\n" +
                    "      },\n" +
                    "      \"carbohydrates\": {\n" +
                    "        \"type\": \"number\",\n" +
                    "        \"description\": \"The carbohydrate content of the ingredient, per the specified quantity.\",\n" +
                    "        \"minimum\": 0,\n" +
                    "        \"example\": 0\n" +
                    "      },\n" +
                    "      \"fat\": {\n" +
                    "        \"type\": \"number\",\n" +
                    "        \"description\": \"The fat content of the ingredient, per the specified quantity.\",\n" +
                    "        \"minimum\": 0,\n" +
                    "        \"example\": 10\n" +
                    "      },\n" +
                    "      \"fiber\": {\n" +
                    "        \"type\": \"number\",\n" +
                    "        \"description\": \"The fiber content of the ingredient, per the specified quantity.\",\n" +
                    "        \"minimum\": 0,\n" +
                    "        \"example\": 0\n" +
                    "      },\n" +
                    "      \"sugar\": {\n" +
                    "        \"type\": \"number\",\n" +
                    "        \"description\": \"The sugar content of the ingredient, per the specified quantity.\",\n" +
                    "        \"minimum\": 0,\n" +
                    "        \"example\": 0\n" +
                    "      },\n" +
                    "      \"sodium\": {\n" +
                    "        \"type\": \"number\",\n" +
                    "        \"description\": \"The sodium content of the ingredient, per the specified quantity.\",\n" +
                    "        \"minimum\": 0,\n" +
                    "        \"example\": 70\n" +
                    "      },\n" +
                    "      \"notes\": {\n" +
                    "        \"type\": \"string\",\n" +
                    "        \"description\": \"Optional notes about the ingredient, such as preparation methods or specific brands.\",\n" +
                    "        \"example\": \"Skinless, grilled\"\n" +
                    "      },\n" +
                    "      \"foodGroup\": {\n" +
                    "        \"type\": \"string\",\n" +
                    "        \"description\": \"The food group to which the ingredient belongs.\",\n" +
                    "        \"enum\": [\"Protein\", \"Vegetable\", \"Fruit\", \"Grain\", \"Dairy\", \"Fat\", \"Spice\", \"Other\"],\n" +
                    "        \"example\": \"Protein\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"required\": [\"name\", \"quantity\", \"unit\", \"calories\", \"protein\", \"carbohydrates\", \"fat\"]\n" +
                    "  }\n" +
                    "}";
            String prompt = "based on the photos write me the amount of carbs, proteins, fats and calories in the dish.\n" +
                    "If you can't find any food in the photos, please ask me to take a better photo."+
                    "return the data in the given schema:" + dishIngredientsSchema;
            geminiManager.sendTextWithPhotosPrompt(prompt, photos, new GeminiCallback() {
                @Override
                public void onSuccess(String result) {
                    pD.dismiss();
                    tVDetails.setText(result);
                    Log.i(TAG, "onActivityResult/ Success");
                }

                @Override
                public void onFailure(Throwable error) {
                    pD.dismiss();
                    tVDetails.setText("Error: " + error.getMessage());
                    Log.e(TAG, "onActivityResult/ Error: " + error.getMessage());
                }
            });
        } else {
            Toast.makeText(this, "No photos to send", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.menuPhotos) {
        } else if (id == R.id.menuText) {
            finish();
//        } else if (st.equals("Green")) {
//            RL.setBackgroundColor(Color.GREEN);
//        } else if (st.equals("Yellow")) {
//            RL.setBackgroundColor(Color.YELLOW);
//        } else if (st.equals("White")) {
//            RL.setBackgroundColor(Color.WHITE);
        }
        return super.onOptionsItemSelected(item);
    }

}