package com.example.hsai02;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText eTLanguage, eTWords;
    TextView tVResult;
    GeminiManager geminiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eTLanguage = findViewById(R.id.eTLanguage);
        eTWords = findViewById(R.id.eTWords);
        tVResult = findViewById(R.id.tVResult);

        geminiManager = GeminiManager.getInstance();

    }


    public void textPrompt(View view) {
        String language = eTLanguage.getText().toString();
        String words = eTWords.getText().toString();
        if (language.isEmpty() || words.isEmpty()) {
            tVResult.setText("Please enter both language and words.");
        } else {
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
                }

                @Override
                public void onFailure(Throwable error) {
                    pD.dismiss();
                    tVResult.setText("Error: " + error.getMessage());
                }
            });
        }

    }
}