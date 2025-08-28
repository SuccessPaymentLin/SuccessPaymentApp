package com.example.paymentgatewayui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONException;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AmountConfirmationActivity extends AppCompatActivity {

    TextView amountText;
    Button confirmButton, cancelButton;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount_confirmation);

        amountText = findViewById(R.id.amountText);
        confirmButton = findViewById(R.id.confirmButton);
        cancelButton = findViewById(R.id.cancelButton);

        String amount = getIntent().getStringExtra("amount");
        if (amount != null) {
            amountText.setText(String.format("€%.2f", Double.parseDouble(amount)));
        }

        confirmButton.setOnClickListener(v -> sendPayment(amount));
        cancelButton.setOnClickListener(v -> finish());
    }

    private void sendPayment(String amount) {
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");

        String json = String.format(
                "{" +
                        "\"amount\": %s," +
                        "\"description\": \"Android App Payment\"," +
                        "\"payment_method\": \"mobile_money\"," +
                        "\"phone_number\": \"670000000\"," +  // use test number from Bictorys
                        "\"provider\": \"MTN\"" +
                        "}", amount);

        RequestBody body = RequestBody.create(json, mediaType);

        Request request = new Request.Builder()
                .url("http://16.171.115.204:8080/charge")  // your backend
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                goToFailureScreen();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "null";
                Log.d("HTTP", "Response Code: " + response.code());
                Log.d("HTTP", "Response Body: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String checkoutUrl = json.getString("link");

                        new Handler(Looper.getMainLooper()).post(() -> {
                            Intent intent = new Intent(AmountConfirmationActivity.this, SuccessActivity.class);
                            intent.putExtra("checkoutUrl", checkoutUrl);
                            startActivity(intent);
                            finish();
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        goToFailureScreen();
                    }
                } else {
                    goToFailureScreen();
                }
            }
        });
    }

    private void goToSuccessScreen() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(AmountConfirmationActivity.this, SuccessActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void goToFailureScreen() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(AmountConfirmationActivity.this, FailureActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
