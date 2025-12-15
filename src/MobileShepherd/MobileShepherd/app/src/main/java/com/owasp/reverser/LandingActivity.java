package com.owasp.reverser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Button enterAppButton = findViewById(R.id.enter_app_button);
        enterAppButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
