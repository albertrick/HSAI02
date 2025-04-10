package com.example.hsai02.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hsai02.GeminiCallback;
import com.example.hsai02.GeminiManager;
import com.example.hsai02.R;

public class MainActivity extends AppCompatActivity {
    EditText eTLanguage, eTWords;
    TextView tVResult;
    GeminiManager geminiManager;
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eTLanguage = findViewById(R.id.eTLanguage);
        eTWords = findViewById(R.id.eTWords);
        tVResult = findViewById(R.id.tVResult);
        tVResult.setMovementMethod(new ScrollingMovementMethod());

        geminiManager = GeminiManager.getInstance();

    }


    public void textPrompt(View view) {
        String language = eTLanguage.getText().toString();
        String words = eTWords.getText().toString();
        if (language.isEmpty() || words.isEmpty()) {
            tVResult.setText("Please enter both language and words.");
        } else {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            String prompt = "Create a story in " + language + " with " + words +
                    " words. return only the name of the story and the story.";
            ProgressDialog pD = new ProgressDialog(this);
            pD.setTitle("Sent Prompt");
            pD.setMessage("Waiting for response...");
            pD.setCancelable(false);
            pD.show();
            geminiManager.sendTextPrompt(prompt, new GeminiCallback() {
                @Override
                public void onSuccess(String result) {
                    pD.dismiss();
                    tVResult.setText(result);
                    Log.i(TAG, "textPrompt/ Success");
                }

                @Override
                public void onFailure(Throwable error) {
                    pD.dismiss();
                    tVResult.setText("Failed prompting Gemini");
                    Log.e(TAG, "textPrompt/ Error: " + error.getMessage());
                }
            });
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
        if (id == R.id.menuPhoto) {
            intent = new Intent(this, PhotoActivity.class);
            startActivity(intent);
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