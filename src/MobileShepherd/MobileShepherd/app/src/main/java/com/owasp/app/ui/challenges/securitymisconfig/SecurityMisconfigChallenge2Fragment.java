package com.owasp.app.ui.challenges.securitymisconfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.owasp.app.R;

public class SecurityMisconfigChallenge2Fragment extends Fragment {

    private static final String TAG = "BackupChallenge";
    private static final String PREFS_NAME = "BackupChallengePrefs";
    
    // Flag split into parts for obfuscation
    private static final String FLAG_PART1 = "OWASP{";
    private static final String FLAG_PART2 = "B4ckup_";
    private static final String FLAG_PART3 = "D4t4_";
    private static final String FLAG_PART4 = "3xtr4ct3d";
    private static final String FLAG_PART5 = "}";
    
    private TextInputEditText flagInput;
    private TextView resultText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_security_misconfig_challenge2, container, false);

        flagInput = root.findViewById(R.id.flag_input);
        resultText = root.findViewById(R.id.result_text);
        
        Button validateButton = root.findViewById(R.id.validate_button);
        validateButton.setOnClickListener(v -> validateFlag());

        // Store the flag in SharedPreferences (will be backed up)
        storeSecretData();

        return root;
    }

    private void storeSecretData() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Store some normal preferences
        editor.putString("username", "admin");
        editor.putString("theme", "dark");
        editor.putString("language", "en");
        
        // Store the flag - this will be in the backup!
        String fullFlag = FLAG_PART1 + FLAG_PART2 + FLAG_PART3 + FLAG_PART4 + FLAG_PART5;
        editor.putString("secret_flag", fullFlag);
        editor.putString("flag_hint", "Extract me with adb backup!");
        
        // Store individual parts as well for extra discovery
        editor.putString("config_key_1", FLAG_PART1);
        editor.putString("config_key_2", FLAG_PART2);
        editor.putString("config_key_3", FLAG_PART3);
        editor.putString("config_key_4", FLAG_PART4);
        editor.putString("config_key_5", FLAG_PART5);
        
        editor.apply();
        
        Log.d(TAG, "Secret data stored in SharedPreferences");
        Log.d(TAG, "Backup is enabled for this app - data can be extracted!");
        Log.d(TAG, "Hint: adb backup -f backup.ab -noapk com.owasp.app");
    }

    private void validateFlag() {
        String userInput = flagInput.getText().toString().trim();
        String correctFlag = FLAG_PART1 + FLAG_PART2 + FLAG_PART3 + FLAG_PART4 + FLAG_PART5;

        resultText.setVisibility(View.VISIBLE);

        if (userInput.equals(correctFlag)) {
            resultText.setText("✓ SUCCESS!\n\nFlag: " + correctFlag + "\n\nYou successfully extracted the backup and found the flag in SharedPreferences!");
            resultText.setTextColor(Color.parseColor("#388E3C"));
            resultText.setBackgroundColor(Color.parseColor("#E8F5E9"));
            Log.i(TAG, "Challenge completed! Flag validated successfully.");
        } else if (userInput.isEmpty()) {
            resultText.setText("⚠️ Please enter a flag");
            resultText.setTextColor(Color.parseColor("#F57C00"));
            resultText.setBackgroundColor(Color.parseColor("#FFF3E0"));
        } else {
            resultText.setText("✗ INCORRECT\n\nThat's not the right flag. Try extracting the app backup using ADB.");
            resultText.setTextColor(Color.parseColor("#D32F2F"));
            resultText.setBackgroundColor(Color.parseColor("#FFEBEE"));
            Log.d(TAG, "Incorrect flag attempt: " + userInput);
        }
    }
}
