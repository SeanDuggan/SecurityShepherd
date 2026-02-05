package com.owasp.app.ui.challenges.securitymisconfig;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.owasp.app.R;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class SecurityMisconfigChallenge3Fragment extends Fragment {

    // Flag split into parts for obfuscation
    private static final String FLAG_PART1 = "OWASP{";
    private static final String FLAG_PART2 = "3xp0rt3d_";
    private static final String FLAG_PART3 = "C0mp0n3nt_";
    private static final String FLAG_PART4 = "Pwn";
    private static final String FLAG_PART5 = "}";
    
    private TextInputEditText flagInput;
    private TextView resultText;
    private ProgressTracker progressTracker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_security_misconfig_challenge3, container, false);

        progressTracker = new ProgressTracker(requireContext());

        // Setup FAB for vulnerability information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showVulnerabilityInfo());
        }

        flagInput = root.findViewById(R.id.flag_input);
        resultText = root.findViewById(R.id.result_text);
        
        Button validateButton = root.findViewById(R.id.validate_button);
        validateButton.setOnClickListener(v -> validateFlag());

        return root;
    }

    private void showVulnerabilityInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        TextView bestPracticesText = dialogView.findViewById(R.id.best_practices_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.security_misconfig_intro);
        vulnerabilitiesText.setText(R.string.security_misconfig_vulnerabilities);
        
        hintsSection.setVisibility(View.VISIBLE);
        hintsText.setText(R.string.security_misconfig_challenge3_description);
        
        bestPracticesSection.setVisibility(View.VISIBLE);
        bestPracticesText.setText(R.string.security_misconfig_best_practices);
        
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Security Misconfiguration - Exported Component")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void validateFlag() {
        String userInput = flagInput.getText().toString().trim();
        String correctFlag = FLAG_PART1 + FLAG_PART2 + FLAG_PART3 + FLAG_PART4 + FLAG_PART5;

        resultText.setVisibility(View.VISIBLE);

        if (userInput.equals(correctFlag)) {
            progressTracker.markCompleted(FlagValidator.Module.SECURITY_MISCONFIG_CHALLENGE_3);
            int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.SECURITY_MISCONFIG_CHALLENGE_3);
            String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
            
            resultText.setText("✓ SUCCESS!\n\nFlag: " + correctFlag + "\n\nYou successfully exploited the exported component!");
            resultText.setTextColor(Color.parseColor("#388E3C"));
            resultText.setBackgroundColor(Color.parseColor("#E8F5E9"));
            
            new AlertDialog.Builder(requireContext())
                .setTitle("🎉 Success!")
                .setMessage("Congratulations! You exploited the exported component.\n\nFlag: " + userInput + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
                .setPositiveButton("OK", null)
                .show();
        } else if (userInput.isEmpty()) {
            resultText.setText("Please enter a flag");
            resultText.setTextColor(Color.parseColor("#F57C00"));
            resultText.setBackgroundColor(Color.parseColor("#FFF3E0"));
        } else {
            resultText.setText("✗ INCORRECT\n\nThat's not the right flag. Check the AndroidManifest.xml and launch the exported activity via ADB.");
            resultText.setTextColor(Color.parseColor("#D32F2F"));
            resultText.setBackgroundColor(Color.parseColor("#FFEBEE"));
        }
    }
}
