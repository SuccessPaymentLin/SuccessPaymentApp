package com.example.paymentgatewayui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class ProcessingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.processing);

        // Simulate a 3-second processing delay
        new Handler().postDelayed(() -> {
            boolean isSuccess = true; // Change to false to simulate failure

            Intent resultIntent;
            if (isSuccess) {
                resultIntent = new Intent(this, SuccessActivity.class);
            } else {
                resultIntent = new Intent(this, FailureActivity.class);
            }

            resultIntent.putExtra("amount", getIntent().getStringExtra("amount"));
            startActivity(resultIntent);
            finish();
        }, 3000); // Delay = 3 seconds
    }
}