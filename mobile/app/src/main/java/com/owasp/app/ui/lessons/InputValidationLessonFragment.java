package com.owasp.app.ui.lessons;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentInputValidationLessonBinding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class InputValidationLessonFragment extends Fragment {

    private FragmentInputValidationLessonBinding binding;
    private static final String TAG = "InputValidation";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    
    // Hidden flag for successful bypass
    private static final String HIDDEN_FLAG = "KEY{1nput_V4l1d4t10n_Byp4ss3d}";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInputValidationLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Setup test button
        binding.testUrlButton.setOnClickListener(v -> testUrlValidation());

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> toggleFabExpansion(fab, fabCommandRef, fabOwaspLink));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }
        if (fabOwaspLink != null) {
            fabOwaspLink.setOnClickListener(v -> {
                openOwaspTop10Link();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }

        return root;
    }

    private void testUrlValidation() {
        String url = binding.urlInput.getText().toString().trim();
        
        if (url.isEmpty()) {
            binding.validationResult.setText("Error: URL cannot be empty");
            return;
        }

        // VULNERABLE: Weak URL validation that can be bypassed
        String result = validateUrl(url);
        binding.validationResult.setText(result);
        
        Log.d(TAG, "URL validation result: " + result);
    }

    private String validateUrl(String url) {
        // Simulate deep link processing: myapp://open?url=<USER_URL>
        Log.d(TAG, "Processing URL: " + url);
        
        // VULNERABILITY: Simple contains() check can be bypassed
        // Example bypass: https://evil.com?redirect=example.com
        // Example bypass: https://example.com.evil.com
        // Example bypass: https://evil.com#example.com
        
        if (url.contains("example.com") || url.contains("trusted-site.com")) {
            // Check if it's actually a bypass attempt
            if (!url.startsWith("https://example.com") && 
                !url.startsWith("https://trusted-site.com") &&
                !url.startsWith("http://example.com") && 
                !url.startsWith("http://trusted-site.com")) {
                
                // Successful bypass!
                return "BYPASS DETECTED!\n\n" +
                       "URL validated: " + url + "\n\n" +
                       "This is a validation bypass vulnerability!\n" +
                       "The validation only checks if the trusted domain appears anywhere in the URL.\n\n" +
                       "Your bypass worked! Here's the flag:\n\n" +
                       HIDDEN_FLAG + "\n\n" +
                       "Proper validation should use startsWith() or parse the URL host.";
            }
            
            return "URL Validation: PASSED ✓\n\n" +
                   "URL: " + url + "\n\n" +
                   "This URL is from a trusted domain and will be loaded.\n\n" +
                   "Try to bypass the validation by including the trusted domain in a malicious URL!";
        } else {
            return "URL Validation: BLOCKED ✗\n\n" +
                   "URL: " + url + "\n\n" +
                   "This URL is not from a trusted domain.\n\n" +
                   "Allowed domains:\n" +
                   "• example.com\n" +
                   "• trusted-site.com\n\n" +
                   "Hint: The validation checks if the domain appears ANYWHERE in the URL...";
        }
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
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m4-insecure-authentication";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void showDetailedInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.input_validation_lesson_intro);
        // vulnerabilitiesText.setText(R.string.input_validation_lesson_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.INPUT_VALIDATION_LESSON);
        String buttonText = isCompleted ? "Mark as Incomplete" : "Mark as Complete";
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Input/Output Validation")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .setNeutralButton(buttonText, (d, which) -> {
                    boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.INPUT_VALIDATION_LESSON);
                    String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                })
                .show();
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
