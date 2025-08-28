package com.example.paymentgatewayui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SuccessActivity extends AppCompatActivity {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable redirectRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        Button doneButton = findViewById(R.id.doneButton);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRedirect();
                navigateBackToMain();
            }
        });

        // Auto-redirect after 3 seconds
        redirectRunnable = this::navigateBackToMain;
        handler.postDelayed(redirectRunnable, 3000);
    }

    private void navigateBackToMain() {
        Intent intent = new Intent(SuccessActivity.this, TransactionTypeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void cancelRedirect() {
        if (redirectRunnable != null) {
            handler.removeCallbacks(redirectRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelRedirect(); // Prevent memory leak
    }
}
