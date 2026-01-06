package com.owasp.app.ui.lessons;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentInputValidationLessonBinding;

public class InputValidationLessonFragment extends Fragment {

    private FragmentInputValidationLessonBinding binding;
    private static final String TAG = "InputValidation";
    
    // Hidden flag for successful bypass
    private static final String HIDDEN_FLAG = "FLAG{1nput_V4l1d4t10n_Byp4ss3d}";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInputValidationLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup test button
        binding.testUrlButton.setOnClickListener(v -> testUrlValidation());

        // Setup FAB for lesson info
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showDetailedInfo());
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

    private void showDetailedInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.input_validation_lesson_intro);
        vulnerabilitiesText.setText(R.string.input_validation_lesson_vulnerabilities);
        
        hintsSection.setVisibility(View.VISIBLE);
        hintsText.setText(R.string.input_validation_lesson_hint);
        
        bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Input/Output Validation")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
