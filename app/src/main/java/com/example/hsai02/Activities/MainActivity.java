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
import com.example.hsai02.MasterActivity;
import com.example.hsai02.R;

public class MainActivity extends MasterActivity {
    private EditText eTLanguage, eTWords;
    private TextView tVResult;
    private GeminiManager geminiManager;
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

    /**
     * This method is called when the user clicks the "Send Prompt" button.
     * It retrieves the language and words from the EditText fields, constructs a prompt,
     * and sends it to the Gemini AI model.
     *
     * @param view The view that was clicked.
     */
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
            geminiManager.sendTextPrompt(prompt,
                    new GeminiCallback() {
                @Override
                public void onSuccess(String result) {
                    pD.dismiss();
                    tVResult.setText(result);
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

}