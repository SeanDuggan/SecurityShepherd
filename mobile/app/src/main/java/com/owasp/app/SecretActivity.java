package com.owasp.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class SecretActivity extends Activity {

    private static final String TAG = "SecretActivity";
    private static final String UNLOCK_KEY = "unlock_code";
    private static final String UNLOCK_VALUE = "shepherd2023";
    
    // Flag parts
    private static final String FLAG_PART1 = "KEY{";
    private static final String FLAG_PART2 = "3xp0rt3d_";
    private static final String FLAG_PART3 = "C0mp0n3nt_";
    private static final String FLAG_PART4 = "Pwn";
    private static final String FLAG_PART5 = "}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView textView = new TextView(this);
        textView.setPadding(50, 50, 50, 50);
        textView.setTextSize(16);
        setContentView(textView);

        Log.w(TAG, "⚠️ SecretActivity launched! This activity is exported without permission checks!");
        
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(UNLOCK_KEY)) {
            String unlockCode = intent.getStringExtra(UNLOCK_KEY);
            
            Log.d(TAG, "Received unlock code: " + unlockCode);
            
            if (UNLOCK_VALUE.equals(unlockCode)) {
                String flag = FLAG_PART1 + FLAG_PART2 + FLAG_PART3 + FLAG_PART4 + FLAG_PART5;
                
                textView.setText("🎉 ACCESS GRANTED!\n\n" +
                        "You successfully exploited the exported component!\n\n" +
                        "Flag: " + flag + "\n\n" +
                        "Vulnerability: This activity is exported without proper permission checks, " +
                        "allowing any app or ADB command to launch it and access sensitive functionality.");
                
                Log.i(TAG, "✓ Flag revealed: " + flag);
                Toast.makeText(this, "Flag found! Check the activity screen.", Toast.LENGTH_LONG).show();
            } else {
                textView.setText("❌ ACCESS DENIED\n\nIncorrect unlock code.\n\n" +
                        "Hint: Check the challenge description for the correct key and value.");
                Log.d(TAG, "Incorrect unlock code provided");
            }
        } else {
            textView.setText("❌ UNAUTHORIZED ACCESS\n\n" +
                    "This activity requires an unlock code.\n\n" +
                    "Hint: Launch this activity via ADB with the correct intent extras.\n\n" +
                    "Example: adb shell am start -n com.owasp.app/.SecretActivity -e [key] [value]");
            
            Log.d(TAG, "Activity launched without unlock code");
        }
        
        Log.w(TAG, "Security Issue: This exported activity can be invoked by any app!");
    }
}
