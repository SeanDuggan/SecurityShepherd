package com.owasp.app.ui.challenges.poorauth;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentPoorAuthChallengeBinding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Random;

public class PoorAuthChallengeFragment extends Fragment {

    private FragmentPoorAuthChallengeBinding binding;
    private PoorAuthChallengeModel viewModel;
    private static String tempPassword;
    private boolean passwordReset = false;
    private static final String TAG = "PoorAuthChallenge";
    private static final String USERNAME = "Jack";
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(PoorAuthChallengeModel.class);

        binding = FragmentPoorAuthChallengeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
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

        // Write insecure logs revealing security question answers
        writeInsecureLogs();

        // Setup forgot password button
        binding.forgotPasswordButton.setOnClickListener(v -> showPasswordResetDialog());

        // Setup login button
        binding.loginButton.setOnClickListener(v -> handleLogin());

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
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m3-insecure-authentication-authorization";
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
        
        introText.setText(R.string.poor_auth_intro);
        // vulnerabilitiesText.setText(R.string.poor_auth_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        
        additionalSection.setVisibility(View.GONE);
        
        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.POOR_AUTH_CHALLENGE);
        String buttonText = isCompleted ? "Mark as Incomplete" : "Mark as Complete";
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Poor Authentication Challenge")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .setNeutralButton(buttonText, (d, which) -> {
                    boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.POOR_AUTH_CHALLENGE);
                    String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void writeInsecureLogs() {
        // Log sensitive information that reveals security answers
        Log.d(TAG, "My name is Jack Meade, I'm here to kick ass and drink gravy!");
        Log.d(TAG, "Today I had chicken again! I love Chicken! #deliciousChicken");
        Log.d(TAG, "The house is flooded... uh oh");
        Log.d(TAG, "Misplaced my phone again, found it in the microwave.");
        Log.d(TAG, "My mother just married again! Goodbye Mrs. Meade hello Mrs Jenkins!");
        
        // Write to world-readable files (intentionally insecure)
        writeWorldReadableLog("My name is Jack Meade, I'm here to kick ass and drink gravy!");
        writeWorldReadableLog("Today I had chicken again! I love Chicken! #deliciousChicken #whyDoIDoThis");
        writeWorldReadableLog("My mother just married again! Goodbye Mrs. Meade hello Mrs Jenkins!");
        
        Toast.makeText(getContext(), "Hint: Check logcat for interesting information!", Toast.LENGTH_LONG).show();
    }

    private void writeWorldReadableLog(String content) {
        Date date = new Date();
        Random rand = new Random(5);
        String filename = "PoorAuthLog" + rand.nextInt(100);
        String EOL = System.getProperty("line.separator");
        BufferedWriter writer = null;
        
        try {
            writer = new BufferedWriter(
                new OutputStreamWriter(
                    requireContext().openFileOutput(filename, Context.MODE_PRIVATE)
                )
            );
            writer.write(content + EOL);
            writer.write(date.toString() + EOL);
        } catch (Exception e) {
            Log.e(TAG, "Error writing log", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing writer", e);
                }
            }
        }
    }

    private void showPasswordResetDialog() {
        // Show password reset section
        binding.loginSection.setVisibility(View.GONE);
        binding.resetSection.setVisibility(View.VISIBLE);

        binding.resetButton.setOnClickListener(v -> handlePasswordReset());
        binding.cancelButton.setOnClickListener(v -> {
            binding.loginSection.setVisibility(View.VISIBLE);
            binding.resetSection.setVisibility(View.GONE);
            binding.question1Input.setText("");
            binding.question2Input.setText("");
        });
    }

    private void handlePasswordReset() {
        String answer1 = binding.question1Input.getText().toString().trim();
        String answer2 = binding.question2Input.getText().toString().trim();

        if (answer1.isEmpty() || answer2.isEmpty()) {
            Toast.makeText(getContext(), "Empty Fields Detected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check security answers (intentionally weak questions)
        if (answer1.equalsIgnoreCase("Chicken") && answer2.equalsIgnoreCase("Meade")) {
            // Generate weak temporary password
            tempPassword = generateWeakTempPassword(6);
            passwordReset = true;

            Log.d(TAG, "Password reset successful! Temp password: " + tempPassword);
            
            binding.tempPasswordText.setText("Your temporary password is: " + tempPassword);
            binding.tempPasswordText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Password Reset! Use the temporary password to login.", Toast.LENGTH_LONG).show();

            // Return to login screen after delay
            binding.getRoot().postDelayed(() -> {
                binding.loginSection.setVisibility(View.VISIBLE);
                binding.resetSection.setVisibility(View.GONE);
                binding.question1Input.setText("");
                binding.question2Input.setText("");
            }, 3000);
        } else {
            Toast.makeText(getContext(), "Invalid answers. Check the logs for hints!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Invalid password reset attempt. Answers: " + answer1 + ", " + answer2);
        }
    }

    private void handleLogin() {
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Empty Fields Detected.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Login attempt - Username: " + username + ", Password: " + password);
        Log.d(TAG, "Password reset status: " + passwordReset + ", Temp password: " + tempPassword);

        if (!passwordReset) {
            Toast.makeText(getContext(), "Your account has been locked! Use password reset.", Toast.LENGTH_LONG).show();
            return;
        }

        if (username.equals(USERNAME) && password.equals(tempPassword)) {
            // Successful login - show flag
            String flagText = "KEY{P00r_Auth_W34k_Qu3st10ns}";
            
            binding.loginSection.setVisibility(View.GONE);
            binding.successSection.setVisibility(View.VISIBLE);
            binding.flagText.setText("Congratulations! Here's your flag:\n\n" + flagText);
            
            Toast.makeText(getContext(), "Logged in successfully!", Toast.LENGTH_LONG).show();
            
            // Show flag validation
            binding.validateFlagButton.setOnClickListener(v -> validateFlag());
        } else {
            Toast.makeText(getContext(), "Invalid Credentials!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Login failed. Expected: " + USERNAME + "/" + tempPassword);
            binding.passwordInput.setText("");
        }
    }

    private void validateFlag() {
        String enteredFlag = binding.flagInput.getText().toString().trim();
        boolean isValid = viewModel.validateFlag(enteredFlag);

        if (isValid) {
            progressTracker.markCompleted(FlagValidator.Module.POOR_AUTH_CHALLENGE);
            int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.POOR_AUTH_CHALLENGE);
            String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
            
            Toast.makeText(getContext(), "Flag validated successfully! Challenge complete!", Toast.LENGTH_LONG).show();
            binding.flagValidationCard.setCardBackgroundColor(
                getResources().getColor(android.R.color.holo_green_light)
            );
            
            new AlertDialog.Builder(requireContext())
                .setTitle("🎉 Success!")
                .setMessage("Congratulations! You exploited weak authentication.\n\nFlag: " + enteredFlag + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
                .setPositiveButton("OK", null)
                .show();
        } else {
            Toast.makeText(getContext(), "Incorrect flag!", Toast.LENGTH_SHORT).show();
            binding.flagValidationCard.setCardBackgroundColor(
                getResources().getColor(android.R.color.holo_red_light)
            );
            binding.flagInput.setText("");
        }
    }

    private String generateWeakTempPassword(int length) {
        // Intentionally weak: only numeric, predictable
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
        binding = null;
    }
}
