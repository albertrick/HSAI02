package com.example.hsai02.Activities;

import static com.example.hsai02.Prompts.CHAT_FIRST_PROMPT;
import static com.example.hsai02.Prompts.SYSTEM_PROMPT;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hsai02.GeminiCallback;
import com.example.hsai02.GeminiChatManager;
import com.example.hsai02.R;

public class ChatActivity extends AppCompatActivity {

    private TextView tVGameArea;
    private EditText eTUserInput;
    private GeminiChatManager chatManager;
    private final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        eTUserInput = findViewById(R.id.eTUserInput);
        tVGameArea = findViewById(R.id.tVGameArea);
        tVGameArea.setMovementMethod(new ScrollingMovementMethod());

        chatManager = GeminiChatManager.getInstance(SYSTEM_PROMPT);

        getFirstQuestion();
    }

    /**
     * This method is called when the user clicks the "Send" button.
     * It retrieves the user input, hides the keyboard, and processes the input.
     *
     * @param view The view that was clicked.
     */
    public void sendUserInput(View view) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        String userInput = eTUserInput.getText().toString();
        if (userInput.isEmpty()) {
            Toast.makeText(this, "Please enter your answer", Toast.LENGTH_LONG).show();
            return;
        } else {
            SpannableString spanString = new SpannableString("\n" + "תשובתך:" + "\n" + userInput + "\n");
            spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
            spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
            tVGameArea.append(spanString);
            eTUserInput.setText("");
            processUserInput(userInput);
        }
    }

    /**
     * This method retrieves the first question from the chat manager and displays it in the TextView.
     */
    private void getFirstQuestion() {
        String prompt = CHAT_FIRST_PROMPT;
        ProgressDialog pD = new ProgressDialog(this);
        pD.setTitle("Sent Prompt");
        pD.setMessage("Waiting for response...");
        pD.setCancelable(false);
        pD.show();
        chatManager.sendChatMessage(prompt, new GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                pD.dismiss();
                tVGameArea.append(result + "\n");
            }

            @Override
            public void onFailure(Throwable error) {
                pD.dismiss();
                tVGameArea.append("שגיאה: " + error.getMessage() + "\n");
                Log.e(TAG, "textPrompt/ Error: " + error.getMessage());
            }
        });
    }

    /**
     * This method processes the user input by sending it to the chat manager and displaying the response.
     *
     * @param userInput The user input to be processed.
     */
    private void processUserInput(String userInput) {
        ProgressDialog pD = new ProgressDialog(this);
        pD.setTitle("Sent Prompt");
        pD.setMessage("Waiting for response...");
        pD.setCancelable(false);
        pD.show();
        chatManager.sendChatMessage(userInput, new GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                pD.dismiss();
                tVGameArea.append("\n" + result + "\n");
            }

            @Override
            public void onFailure(Throwable error) {
                pD.dismiss();
                tVGameArea.append("\nשגיאה: " + error.getMessage() + "\n");
                Log.e(TAG, "textPrompt/ Error: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuChat) {
        } else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}