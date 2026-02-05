package com.owasp.app.ui.challenges.inputvalidation;

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

public class XssWebViewChallengeFragment extends Fragment {

    private FragmentXssChallengeBinding binding;
    private static final String TAG = "XssChallenge";
    private static final String HIDDEN_FLAG = "FLAG{XSS_W3bV13w_Pwn3d_2024}";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXssChallengeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup FAB to show vulnerability information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showVulnerabilityInfo());
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

    private void showVulnerabilityInfo() {
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
        hintsText.setText("XSS in WebView Challenge Hints:\n\n" +
            "• JavaScript is enabled in the WebView (vulnerable configuration)\n" +
            "• User input is directly embedded into HTML without sanitization\n" +
            "• The flag is stored in JavaScript variable 'secretFlag'\n" +
            "• A JavaScript interface 'FlagStore' is available\n\n" +
            "Attack Vectors:\n" +
            "1. Inject <script> tags in name or bio fields\n" +
            "2. Call revealFlag() function to display the flag\n" +
            "3. Use document.write() to extract JavaScript variables\n" +
            "4. Access FlagStore.getFlag() via JavaScript interface\n\n" +
            "Example Payloads:\n" +
            "• <script>revealFlag()</script>\n" +
            "• <script>alert(secretFlag)</script>\n" +
            "• <script>FlagStore.showFlag(secretFlag)</script>");
        
        bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Input Validation - WebView XSS")
                .setView(dialogView)
                .setPositiveButton("Close", null)
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
        binding = null;
    }
}
