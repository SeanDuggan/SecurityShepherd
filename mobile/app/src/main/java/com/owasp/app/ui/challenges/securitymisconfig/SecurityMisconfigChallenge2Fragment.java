package com.owasp.app.ui.challenges.securitymisconfig;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

public class SecurityMisconfigChallenge2Fragment extends Fragment {

    private static final String TAG = "BackupChallenge";
    private static final String PREFS_NAME = "BackupChallengePrefs";
    
    // Flag split into parts for obfuscation
    private static final String FLAG_PART1 = "KEY{";
    private static final String FLAG_PART2 = "B4ckup_";
    private static final String FLAG_PART3 = "D4t4_";
    private static final String FLAG_PART4 = "3xtr4ct3d";
    private static final String FLAG_PART5 = "}";
    
    private TextInputEditText flagInput;
    private TextView resultText;
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_security_misconfig_challenge2, container, false);

        progressTracker = new ProgressTracker(requireContext());

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> toggleFabExpansion(fab, fabCommandRef, fabOwaspLink));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showVulnerabilityInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }
        if (fabOwaspLink != null) {
            fabOwaspLink.setOnClickListener(v -> {
                openOwaspTop10Link();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }

        flagInput = root.findViewById(R.id.flag_input);
        resultText = root.findViewById(R.id.result_text);
        
        Button validateButton = root.findViewById(R.id.validate_button);
        validateButton.setOnClickListener(v -> validateFlag());

        // Store the flag in SharedPreferences (will be backed up)
        storeSecretData();

        return root;
    }

    private void toggleFabExpansion(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2) {
        fabExpanded = !fabExpanded;
        if (fabExpanded) {
            if (fab1 != null) fab1.setVisibility(View.VISIBLE);
            if (fab2 != null) fab2.setVisibility(View.VISIBLE);
            if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            collapseFab(mainFab, fab1, fab2);
        }
    }

    private void collapseFab(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2) {
        fabExpanded = false;
        if (fab1 != null) fab1.setVisibility(View.GONE);
        if (fab2 != null) fab2.setVisibility(View.GONE);
        if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_help);
    }

    private void openOwaspTop10Link() {
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m10-extraneous-functionality";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void showVulnerabilityInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.security_misconfig_intro);
        // vulnerabilitiesText.setText(R.string.security_misconfig_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Security Misconfiguration - Backup")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
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
            progressTracker.markCompleted(FlagValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2);
            int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2);
            String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
            
            resultText.setText("✓ SUCCESS!\n\nFlag: " + correctFlag + "\n\nYou successfully extracted the backup and found the flag in SharedPreferences!");
            resultText.setTextColor(Color.parseColor("#388E3C"));
            resultText.setBackgroundColor(Color.parseColor("#E8F5E9"));
            Log.i(TAG, "Challenge completed! Flag validated successfully.");
            
            new AlertDialog.Builder(requireContext())
                .setTitle("🎉 Success!")
                .setMessage("Congratulations! You extracted data from the app backup.\n\nFlag: " + userInput + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
                .setPositiveButton("OK", null)
                .show();
        } else if (userInput.isEmpty()) {
            resultText.setText("Please enter a flag");
            resultText.setTextColor(Color.parseColor("#F57C00"));
            resultText.setBackgroundColor(Color.parseColor("#FFF3E0"));
        } else {
            resultText.setText("✗ INCORRECT\n\nThat's not the right flag. Try extracting the app backup using ADB.");
            resultText.setTextColor(Color.parseColor("#D32F2F"));
            resultText.setBackgroundColor(Color.parseColor("#FFEBEE"));
            Log.d(TAG, "Incorrect flag attempt: " + userInput);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
    }
}
