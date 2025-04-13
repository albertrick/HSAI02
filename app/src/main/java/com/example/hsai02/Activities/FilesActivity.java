package com.example.hsai02.Activities;

import static com.example.hsai02.Prompts.FILE_PROMPT;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private TextView tVFileData, tVSummary;
    private GeminiManager geminiManager;
    private final String TAG = "FilesActivity";
    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 102;
    private static final int FILE_PICKER_REQUEST_CODE = 401;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        tVFileData = findViewById(R.id.tVFileData);
        tVSummary = findViewById(R.id.tVSummary);
        tVSummary.setMovementMethod(new ScrollingMovementMethod());

        geminiManager = GeminiManager.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * filePrompt method
     * <p> Method triggered by the user clicking the "Choose File" button
     * </p>
     *
     * @param view The view that was clicked.
     */
    public void filePrompt(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

    /**
     * Prompting selected file to Gemini
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
            if (requestCode == FILE_PICKER_REQUEST_CODE) {
                if (data_back != null) {
                    Uri fileUri = data_back.getData();
                    String mimeType = getContentResolver().getType(fileUri);
                    String path = fileUri.getPath();
                    String fileName = path.substring(1+path.lastIndexOf('/'));

                    tVFileData.setText("File chosen:\n" +
                            "NAME: " + fileName + "\n" +
                            "MIME Type: " + mimeType);

                    byte[] bytes = null;
                    try {
                        bytes = getBytes(fileUri);
                    } catch (Exception e) {
                        Log.i(TAG, "onActivityResult/ bytes: "+e.getMessage());
                        throw new RuntimeException(e);
                    }
                    if (bytes == null) {
                        tVSummary.setText("Error: File is empty");
                        return;
                    }
                    ProgressDialog pD = new ProgressDialog(FilesActivity.this);
                    pD.setTitle("Sent Prompt");
                    pD.setMessage("Waiting for response...");
                    pD.setCancelable(false);
                    pD.show();
                    String prompt = FILE_PROMPT;
                    geminiManager.sendTextWithFilePrompt(prompt, bytes, mimeType,
                            new GeminiCallback() {
                        @Override
                        public void onSuccess(String result) {
                            pD.dismiss();
                            tVSummary.setText(result);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            pD.dismiss();
                            tVSummary.setText("Error: " + error.getMessage());
                            Log.e(TAG, "onActivityResult/ Error: " + error.getMessage());
                        }
                    });
                }
            }
        }
    }

    /**
     * getBytes method
     * <p> Method to convert the file to bytes
     * </p>
     *
     * @param fileUri The uri of the file
     * @return The byte array of the file
     */
    private byte[] getBytes(Uri fileUri) throws IOException {
        InputStream iStream =   getContentResolver().openInputStream(fileUri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = iStream.read(buffer);
        while (len != -1) {
            byteBuffer.write(buffer, 0, len);
            len = iStream.read(buffer);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        if (id == R.id.menuFiles) {
        } else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}