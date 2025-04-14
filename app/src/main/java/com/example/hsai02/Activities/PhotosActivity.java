package com.example.hsai02.Activities;

import static com.example.hsai02.Prompts.INGREDIENTS_SCHEMA;
import static com.example.hsai02.Prompts.PHOTOS_PROMPT;

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
import com.example.hsai02.MasterActivity;
import com.example.hsai02.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PhotosActivity extends MasterActivity {

    private TextView tVCount, tVDetails;
    private String currentPath, imagePath;
    private Bitmap imageBitmap;
    private GeminiManager geminiManager;
    private int photosCount;
    private ArrayList<Bitmap> photos;
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
                Toast.makeText(this, "External storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * This method is called when the user clicks the "Add Photo" button.
     * It opens the camera to take a photo and saves it to a temporary file.
     *
     * @param view The view that was clicked.
     */
    public void addPhoto(View view) {
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
            Log.e(TAG, "addPhoto/ Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Add selected image file to image list
     * <p>
     *
     * @param requestCode   The call sign of the intent that requested the result
     * @param resultCode    A code that symbols the status of the result of the activity
     * @param data_back     The data returned
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data_back) {
        super.onActivityResult(requestCode, resultCode, data_back);
        if ((resultCode == Activity.RESULT_OK)
                && (requestCode == REQUEST_FULL_IMAGE_CAPTURE)) {
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

    /**
     * Prompting the image list to Gemini
     * <p>
     *
     * @param view The view that was clicked.
     */
    public void photosPrompt(View view) {
        if (photosCount != 0) {
            ProgressDialog pD = new ProgressDialog(this);
            pD.setTitle("Sent Prompt");
            pD.setMessage("Waiting for response...");
            pD.setCancelable(false);
            pD.show();
            String prompt = PHOTOS_PROMPT;
            geminiManager.sendTextWithPhotosPrompt(prompt, photos,
                    new GeminiCallback() {
                @Override
                public void onSuccess(String result) {
                    pD.dismiss();
                    tVDetails.setText(result);
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

}