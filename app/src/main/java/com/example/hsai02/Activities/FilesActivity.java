package com.example.hsai02.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hsai02.GeminiCallback;
import com.example.hsai02.GeminiManager;
import com.example.hsai02.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class FilesActivity extends AppCompatActivity {

    TextView tVCount, tVDetails;
    String currentPath, imagePath;
    Bitmap imageBitmap;
    GeminiManager geminiManager;
    int filesCount;
    ArrayList<File> files;
    ArrayList<Bitmap> photos;
    private final String TAG = "FilesActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 102;
    private static final int REQUEST_FULL_IMAGE_CAPTURE = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        tVCount = findViewById(R.id.tVCount);
        tVDetails = findViewById(R.id.tVDetails);
        tVDetails.setMovementMethod(new ScrollingMovementMethod());

        geminiManager = GeminiManager.getInstance();

        filesCount = 0;
        files = new ArrayList<>();
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
    public void addFile(View view) {
        // creating local temporary file to store the full resolution photo
        String filename = "tempfile";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imgFile = File.createTempFile(filename,".jpg",storageDir);
            imagePath = imgFile.getAbsolutePath();
            currentPath = imgFile.getParentFile().getAbsolutePath();
            Uri imageUri = FileProvider.getUriForFile(FilesActivity.this,"com.example.hsai02.fileprovider",imgFile);
            Intent takePicIntent = new Intent();
            takePicIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            takePicIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,imageUri);
            if (takePicIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePicIntent, REQUEST_FULL_IMAGE_CAPTURE);
            }
        } catch (IOException e) {
            Toast.makeText(FilesActivity.this,"Failed to create temporary file",Toast.LENGTH_LONG);
            throw new RuntimeException(e);
        }
    }

    /**
     * saveImageAsPdf method
     * <p> Saving the image as a PDF file
     * </p>
     *
     * @param bitmap the bitmap to save as a PDF
     * @return the file object of the saved PDF
     */
    private File saveImageAsPdf(Bitmap bitmap) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp + ".pdf";
        File file = new File(currentPath, fileName);;
        Log.i(TAG, "saveImageAsPdf/ File path: " + file.getAbsolutePath());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            Log.i(TAG, "saveImageAsPdf/ PDF saved to: " + file.getAbsolutePath());
            return file; // Return the File object
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "saveImageAsPdf/ Error saving PDF: " + e.getMessage());
            Toast.makeText(this, "Error saving PDF.", Toast.LENGTH_SHORT).show();
            return null; // Return null if there's an error
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
//                File pdfFile = saveImageAsPdf(imageBitmap);
//                if (pdfFile != null) {
//                    filesCount++;
//                    files.add(pdfFile);
//                    Toast.makeText(this, "File added to files list", Toast.LENGTH_LONG).show();
//                    tVCount.setText("Number of files: " + filesCount);
//                } else {
//                    Toast.makeText(this, "Error saving image as PDF.", Toast.LENGTH_LONG).show();
//                }
                if (imageBitmap != null) {
                    filesCount++;
                    photos.add(imageBitmap);
                    Toast.makeText(this, "Photo added to files list", Toast.LENGTH_LONG).show();
                    tVCount.setText("Number of photos: " + filesCount);
                } else {
                    Toast.makeText(this, "Error saving image as PDF.", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    public void filesPrompt(View view) {
        if (filesCount != 0) {
        ProgressDialog pD = new ProgressDialog(this);
        pD.setTitle("Sent Prompt");
        pD.setMessage("Waiting for response...");
        pD.setCancelable(false);
        pD.show();
        String prompt = "describe what is the item you get from the photos";
//        String prompt = "כתוב לי מהו הפרי או הירק שצולם ובנוסף תן לי מתכון אשר כולל אותו.\n" +
//                "אם אתה לא מוצא פרי או ירק בתמונה תן לי הנחיה לצלם את התמונה מחדש כך שהפרי או הירק יופיע בבירור בתמונה.";
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
            Toast.makeText(this, "No files to send", Toast.LENGTH_LONG).show();
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
        if (id == R.id.menuFiles) {
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