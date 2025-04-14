package com.example.hsai02;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hsai02.Activities.ChatActivity;
import com.example.hsai02.Activities.FileActivity;
import com.example.hsai02.Activities.FilesActivity;
import com.example.hsai02.Activities.MainActivity;
import com.example.hsai02.Activities.PhotoActivity;
import com.example.hsai02.Activities.PhotosActivity;

import java.util.List;

public class MasterActivity extends AppCompatActivity {

    private static final String TAG = "MasterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ActivityManager am = (ActivityManager) this .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        String Actvity_Name = taskInfo.get(0).topActivity.getClassName();
        int itemId = item.getItemId();
        if (itemId == R.id.menuText) {
            if (!Actvity_Name.equals("com.example.hsai02.Activities.MainActivity")) {
                Log.i(TAG,"Changing to MainActivity");
                Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        } else if (itemId == R.id.menuPhoto) {
            if (!Actvity_Name.equals("com.example.hsai02.Activities.PhotoActivity")) {
                Log.i(TAG,"Changing to PhotoActivity");
                Intent intent = new Intent(this.getApplicationContext(), PhotoActivity.class);
                startActivity(intent);
            }
        } else if (itemId == R.id.menuPhotos) {
            if (!Actvity_Name.equals("com.example.hsai02.Activities.PhotosActivity")) {
                Log.i(TAG,"Changing to PhotosActivity");
                Intent intent = new Intent(this.getApplicationContext(), PhotosActivity.class);
                startActivity(intent);
            }
        } else if (itemId == R.id.menuFile) {
            if (!Actvity_Name.equals("com.example.hsai02.Activities.FileActivity")) {
                Log.i(TAG,"Changing to FileActivity");
                Intent intent = new Intent(this.getApplicationContext(), FileActivity.class);
                startActivity(intent);
            }
        } else if (itemId == R.id.menuFiles) {
            if (!Actvity_Name.equals("com.example.hsai02.Activities.FilesActivity")) {
                Log.i(TAG,"Changing to FilesActivity");
                Intent intent = new Intent(this.getApplicationContext(), FilesActivity.class);
                startActivity(intent);
            }
        } else if (itemId == R.id.menuChat) {
            if (!Actvity_Name.equals("com.example.hsai02.Activities.ChatActivity")) {
                Log.i(TAG,"Changing to ChatActivity");
                Intent intent = new Intent(this.getApplicationContext(), ChatActivity.class);
                startActivity(intent);
            }
        } else if (itemId == R.id.menuExit) {
            finishAffinity();
        }
        return super.onOptionsItemSelected(item);
    }
}