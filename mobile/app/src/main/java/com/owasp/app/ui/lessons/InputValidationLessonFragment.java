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
    private static final String TAG = "DeepLinkLoader";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    
    // Hidden flag accessible via validation bypass
    private static final String FLAG = "KEY{1nput_V4l1d4t10n_Byp4ss3d}";
    private static final String ADMIN_URL = "https://admin.internal/dashboard";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInputValidationLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Quick link buttons
        binding.loadExampleButton.setOnClickListener(v -> 
            processDeepLink("https://example.com/welcome"));
        
        binding.loadTrustedButton.setOnClickListener(v -> 
            processDeepLink("https://trusted-site.com/home"));
        
        binding.loadOwaspButton.setOnClickListener(v -> 
            processDeepLink("https://owasp.org/about"));
        
        // Custom deep link
        binding.openLinkButton.setOnClickListener(v -> {
            String url = binding.urlInput.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a URL", Toast.LENGTH_SHORT).show();
                return;
            }
            processDeepLink(url);
        });

        // Setup expandable FAB
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        FloatingActionButton fabMarkComplete = requireActivity().findViewById(R.id.fab_mark_complete);

        if (fab != null) {
            fab.setOnClickListener(v -> toggleFabExpansion(fab, fabCommandRef, fabOwaspLink, fabMarkComplete));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink, fabMarkComplete);
            });
        }
        if (fabOwaspLink != null) {
            fabOwaspLink.setOnClickListener(v -> {
                openOwaspTop10Link();
                collapseFab(fab, fabCommandRef, fabOwaspLink, fabMarkComplete);
            });
        }
        if (fabMarkComplete != null) {
            fabMarkComplete.setOnClickListener(v -> {
                toggleCompleteStatus();
                updateMarkCompleteFabAppearance(fabMarkComplete);
                collapseFab(fab, fabCommandRef, fabOwaspLink, fabMarkComplete);
            });
        }

        updateMarkCompleteFabAppearance(fabMarkComplete);

        return root;
    }

    private void processDeepLink(String url) {
        Log.d(TAG, "Processing deep link: myapp://open?url=" + url);
        
        // VULNERABLE: Weak URL validation
        if (!isUrlAllowed(url)) {
            showError("Security Error", "URL blocked: " + url + "\n\nOnly example.com and trusted-site.com domains are allowed.");
            Log.w(TAG, "URL validation failed: " + url);
            return;
        }
        
        Log.i(TAG, "URL validation passed: " + url);
        loadContent(url);
    }
    
    /**
     * VULNERABILITY: Uses contains() instead of proper domain validation
     * Can be bypassed with: https://evil.com?ref=example.com
     * Or: https://example.com.evil.com
     * Or: https://evil.com#example.com
     */
    private boolean isUrlAllowed(String url) {
        // Weak validation - checks if trusted domain appears anywhere in URL
        return url.contains("example.com") || url.contains("trusted-site.com") || url.contains("owasp.org");
    }
    
    private void loadContent(String url) {
        String title;
        String body;
        int cardColor = getResources().getColor(R.color.card_bg);
        
        // Simulate loading different content based on URL
        if (url.equals("https://example.com/welcome")) {
            title = "Example.com - Welcome";
            body = "Welcome to Example.com!\n\nThis is safe, trusted content from an approved domain.";
            
        } else if (url.equals("https://trusted-site.com/home")) {
            title = "Trusted Site - Home";
            body = "Trusted Site Homepage\n\nYou are viewing content from an approved source.";
            
        } else if (url.equals("https://owasp.org/about")) {
            title = "OWASP.org - About";
            body = "About OWASP\n\nThe Open Web Application Security Project (OWASP) is a nonprofit foundation that works to improve the security of software.\n\nThis is approved security education content.";
            
        } else if (url.contains("admin.internal")) {
            // Hidden admin content - only accessible via validation bypass!
            title = "Admin Dashboard";
            body = "ACCESS GRANTED\n\n" +
                   "You successfully bypassed the URL validation!\n\n" +
                   "The validation only checks if 'example.com' or 'trusted-site.com' appears " +
                   "anywhere in the URL string, instead of properly validating the domain.\n\n" +
                   "This allowed you to access restricted admin.internal content.\n\n" +
                   "FLAG: " + FLAG;
            cardColor = getResources().getColor(android.R.color.holo_green_light);
            Log.i(TAG, "Admin content accessed via bypass!");
            
        } else {
            // Generic external content
            title = "External Content Loaded";
            body = "URL: " + url + "\n\n" +
                   "This URL passed validation and content was loaded.\n\n" +
                   "Try to find the hidden admin panel at admin.internal domain...";
        }
        
        binding.contentCard.setCardBackgroundColor(cardColor);
        binding.contentTitle.setText(title);
        binding.contentBody.setText(body);
        
        Toast.makeText(getContext(), "Content loaded successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void showError(String title, String message) {
        binding.contentCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        binding.contentTitle.setText(title);
        binding.contentBody.setText(message);
        
        Toast.makeText(getContext(), "URL validation failed", Toast.LENGTH_SHORT).show();
    }

    private void toggleFabExpansion(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2, FloatingActionButton fab3) {
        fabExpanded = !fabExpanded;
        if (fabExpanded) {
            if (fab1 != null) fab1.setVisibility(View.VISIBLE);
            if (fab2 != null) fab2.setVisibility(View.VISIBLE);
            if (fab3 != null) fab3.setVisibility(View.VISIBLE);
            if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            collapseFab(mainFab, fab1, fab2, fab3);
        }
    }

    private void collapseFab(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2, FloatingActionButton fab3) {
        fabExpanded = false;
        if (fab1 != null) fab1.setVisibility(View.GONE);
        if (fab2 != null) fab2.setVisibility(View.GONE);
        if (fab3 != null) fab3.setVisibility(View.GONE);
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
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Input/Output Validation")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
    
    private void toggleCompleteStatus() {
        boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.INPUT_VALIDATION_LESSON);
        String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private void updateMarkCompleteFabAppearance(FloatingActionButton fabMarkComplete) {
        if (fabMarkComplete == null) return;
        
        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.INPUT_VALIDATION_LESSON);
        
        if (isCompleted) {
            // Red - will mark as incomplete
            fabMarkComplete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.security_red)));
            fabMarkComplete.setContentDescription("Mark as Incomplete");
        } else {
            // Green - will mark as complete
            fabMarkComplete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.success_green)));
            fabMarkComplete.setContentDescription("Mark as Complete");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        FloatingActionButton fabMarkComplete = requireActivity().findViewById(R.id.fab_mark_complete);
        collapseFab(fab, fabCommandRef, fabOwaspLink, fabMarkComplete);
        binding = null;
    }
}
