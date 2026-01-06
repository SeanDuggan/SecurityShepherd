package com.owasp.app.ui.challenges.securitymisconfig;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.owasp.app.R;

public class SecurityMisconfigChallenge3Fragment extends Fragment {

    // Flag split into parts for obfuscation
    private static final String FLAG_PART1 = "OWASP{";
    private static final String FLAG_PART2 = "3xp0rt3d_";
    private static final String FLAG_PART3 = "C0mp0n3nt_";
    private static final String FLAG_PART4 = "Pwn";
    private static final String FLAG_PART5 = "}";
    
    private TextInputEditText flagInput;
    private TextView resultText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_security_misconfig_challenge3, container, false);

        flagInput = root.findViewById(R.id.flag_input);
        resultText = root.findViewById(R.id.result_text);
        
        Button validateButton = root.findViewById(R.id.validate_button);
        validateButton.setOnClickListener(v -> validateFlag());

        return root;
    }

    private void validateFlag() {
        String userInput = flagInput.getText().toString().trim();
        String correctFlag = FLAG_PART1 + FLAG_PART2 + FLAG_PART3 + FLAG_PART4 + FLAG_PART5;

        resultText.setVisibility(View.VISIBLE);

        if (userInput.equals(correctFlag)) {
            resultText.setText("✓ SUCCESS!\n\nFlag: " + correctFlag + "\n\nYou successfully exploited the exported component!");
            resultText.setTextColor(Color.parseColor("#388E3C"));
            resultText.setBackgroundColor(Color.parseColor("#E8F5E9"));
        } else if (userInput.isEmpty()) {
            resultText.setText("⚠️ Please enter a flag");
            resultText.setTextColor(Color.parseColor("#F57C00"));
            resultText.setBackgroundColor(Color.parseColor("#FFF3E0"));
        } else {
            resultText.setText("✗ INCORRECT\n\nThat's not the right flag. Check the AndroidManifest.xml and launch the exported activity via ADB.");
            resultText.setTextColor(Color.parseColor("#D32F2F"));
            resultText.setBackgroundColor(Color.parseColor("#FFEBEE"));
        }
    }
}
