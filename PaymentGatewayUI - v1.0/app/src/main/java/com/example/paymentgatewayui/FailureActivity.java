package com.example.paymentgatewayui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FailureActivity extends AppCompatActivity {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable redirectRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.failure);

        Button retryButton = findViewById(R.id.tryAgainButton);

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRedirect();
                goToMain();
            }
        });

        redirectRunnable = this::goToMain;
        handler.postDelayed(redirectRunnable, 3000); // 3 seconds
    }

    private void goToMain() {
        Intent intent = new Intent(FailureActivity.this, MainActivity.class);
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
        cancelRedirect(); // Avoid memory leak
    }
}

