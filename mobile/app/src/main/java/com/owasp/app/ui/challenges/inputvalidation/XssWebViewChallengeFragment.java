package com.owasp.app.ui.challenges.inputvalidation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentXssChallengeBinding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class XssWebViewChallengeFragment extends Fragment {

    private FragmentXssChallengeBinding binding;
    private static final String TAG = "XssChallenge";
    private static final String HIDDEN_FLAG = "KEY{XSS_W3bV13w_Pwn3d_2024}";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXssChallengeBinding.inflate(inflater, container, false);
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

        setupWebView();
        
        binding.updateProfileButton.setOnClickListener(v -> updateProfile());
        
        // Initial profile render
        updateProfile();

        return root;
    }

    private void setupWebView() {
        WebView webView = binding.profileWebview;
        WebSettings settings = webView.getSettings();
        
        // VULNERABLE: JavaScript enabled for XSS
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        
        // Add JavaScript interface to allow flag extraction
        webView.addJavascriptInterface(new FlagStore(), "FlagStore");
        
        Log.d(TAG, "WebView configured with JavaScript enabled");
    }

    private void updateProfile() {
        String name = binding.nameInput.getText().toString();
        String bio = binding.bioInput.getText().toString();
        
        // VULNERABLE: No HTML encoding/sanitization
        // User input is directly embedded into HTML
        String html = generateProfileHtml(name, bio);
        
        binding.profileWebview.loadDataWithBaseURL(
            "https://profile.app",
            html,
            "text/html",
            "UTF-8",
            null
        );
        
        Log.d(TAG, "Profile updated: name=" + name);
    }

    private String generateProfileHtml(String name, String bio) {
        // VULNERABILITY: Direct string concatenation without encoding
        // This allows XSS injection through name or bio fields
        
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<style>" +
               "  body { font-family: Arial, sans-serif; padding: 20px; background: #f5f5f5; }" +
               "  .profile-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
               "  .name { font-size: 24px; font-weight: bold; color: #2196F3; margin-bottom: 10px; }" +
               "  .bio { font-size: 14px; color: #666; line-height: 1.6; }" +
               "  .flag-display { margin-top: 20px; padding: 15px; background: #4CAF50; color: white; border-radius: 4px; display: none; }" +
               "</style>" +
               "<script>" +
               "  // Flag stored in JavaScript context" +
               "  var secretFlag = '" + HIDDEN_FLAG + "';" +
               "  " +
               "  function revealFlag() {" +
               "    var flagDiv = document.getElementById('flagDisplay');" +
               "    if (flagDiv) {" +
               "      flagDiv.style.display = 'block';" +
               "      flagDiv.innerHTML = '<strong>FLAG CAPTURED!</strong><br>' + secretFlag;" +
               "    }" +
               "  }" +
               "</script>" +
               "</head>" +
               "<body>" +
               "<div class='profile-card'>" +
               "  <div class='name'>" + name + "</div>" +  // VULNERABLE - No escaping
               "  <div class='bio'>" + bio + "</div>" +     // VULNERABLE - No escaping
               "</div>" +
               "<div id='flagDisplay' class='flag-display'></div>" +
               "</body>" +
               "</html>";
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
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m7-client-code-quality";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void showVulnerabilityInfo() {
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
        
        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.XSS_CHALLENGE);
        String buttonText = isCompleted ? "Mark as Incomplete" : "Mark as Complete";
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Input Validation - WebView XSS")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .setNeutralButton(buttonText, (d, which) -> {
                    boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.XSS_CHALLENGE);
                    String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    // JavaScript interface for flag extraction (alternative method)
    public class FlagStore {
        @JavascriptInterface
        public String getFlag() {
            Log.d(TAG, "Flag accessed via JavaScript interface!");
            return HIDDEN_FLAG;
        }
        
        @JavascriptInterface
        public void showFlag(String flag) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), 
                    "FLAG EXTRACTED VIA XSS!\n\n" + flag, 
                    Toast.LENGTH_LONG).show();
                Log.d(TAG, "Flag extracted: " + flag);
            });
        }
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
