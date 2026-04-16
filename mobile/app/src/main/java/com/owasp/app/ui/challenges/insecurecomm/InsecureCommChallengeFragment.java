package com.owasp.app.ui.challenges.insecurecomm;

import android.os.Bundle;
import android.util.Base64;
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
import com.owasp.app.databinding.FragmentInsecureCommChallengeBinding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class InsecureCommChallengeFragment extends Fragment {

    private FragmentInsecureCommChallengeBinding binding;
    private InsecureCommChallengeModel viewModel;
    private static final String TAG = "AppNetworkMonitor";
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;
    
    // Obfuscated flag components - split and encoded differently
    private static final byte[] ENC_PART1 = {75, 69, 89}; // KEY
    private static final byte[] ENC_PART2 = {123, 78, 51, 116}; // {N3t
    private static final byte[] ENC_PART3 = {119, 48, 114, 107}; // w0rk
    private static final byte[] ENC_PART4 = {95, 83, 110, 49}; // _Sn1
    private static final byte[] ENC_PART5 = {102, 102, 51, 100, 125}; // ff3d}

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInsecureCommChallengeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(InsecureCommChallengeModel.class);
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

        binding.startAppButton.setOnClickListener(v -> simulateAppTraffic());
        binding.submitFlagButton.setOnClickListener(v -> submitFlag());

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
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m5-insecure-communication";
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
        
        introText.setText(R.string.insecure_comm_intro);
        // vulnerabilitiesText.setText(R.string.insecure_comm_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.INSECURE_COMM_CHALLENGE);
        String buttonText = isCompleted ? "Mark as Incomplete" : "Mark as Complete";
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Insecure Communication Challenge")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .setNeutralButton(buttonText, (d, which) -> {
                    boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.INSECURE_COMM_CHALLENGE);
                    String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void simulateAppTraffic() {
        binding.startAppButton.setEnabled(false);
        binding.trafficStatus.setText("Monitoring network traffic...");
        
        new Thread(() -> {
            try {
                // Simulate multiple network requests with noise
                Thread.sleep(500);
                makeSecureAnalyticsRequest();
                
                Thread.sleep(800);
                makeInsecureApiRequest(); // This one contains the flag
                
                Thread.sleep(600);
                makeSecureImageRequest();
                
                Thread.sleep(700);
                makeInsecureMetricsRequest();
                
                Thread.sleep(500);
                makeSecureAuthRequest();
                
                requireActivity().runOnUiThread(() -> {
                    binding.trafficStatus.setText("✓ Network monitoring complete!\n\n5 requests captured. Analyze logcat to find insecure traffic.");
                    binding.trafficStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                    binding.submitSection.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Check logcat tag: AppNetworkMonitor", Toast.LENGTH_LONG).show();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
        }).start();
    }

    private void makeSecureAnalyticsRequest() {
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "[Request #1] Analytics Endpoint");
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "POST https://analytics.secure-api.com/events");
        Log.d(TAG, "Protocol: HTTPS/TLS 1.3 ✓ ENCRYPTED");
        Log.d(TAG, "Headers: [ENCRYPTED]");
        Log.d(TAG, "Body: [ENCRYPTED]");
        Log.d(TAG, "Status: ✓ Secure connection established");
        Log.d(TAG, "");
    }

    private void makeInsecureApiRequest() {
        String flag = reconstructFlag();
        
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "[Request #2] User Session Endpoint");
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "GET http://api.legacy-backend.com/user/session");
        Log.d(TAG, "Protocol: HTTP/1.1 WARNING: PLAINTEXT");
        Log.d(TAG, "");
        Log.d(TAG, "Headers:");
        Log.d(TAG, "  Host: api.legacy-backend.com");
        Log.d(TAG, "  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        Log.d(TAG, "  X-Session-Token: " + flag);
        Log.d(TAG, "  User-Agent: MobileApp/2.1.4");
        Log.d(TAG, "  Accept: application/json");
        Log.d(TAG, "");
        Log.d(TAG, "SECURITY WARNING: Sensitive token sent over HTTP!");
        Log.d(TAG, "");
        
        try {
            URL url = new URL("http://api.legacy-backend.com/user/session");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Session-Token", flag);
            conn.setRequestProperty("User-Agent", "MobileApp/2.1.4");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            
            try {
                conn.connect();
            } catch (Exception e) {
                Log.d(TAG, "Connection failed (expected)");
            }
        } catch (Exception e) {
            // Expected to fail
        }
    }

    private void makeSecureImageRequest() {
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "[Request #3] Image CDN");
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "GET https://cdn.secure-images.com/avatar/user123.jpg");
        Log.d(TAG, "Protocol: HTTPS/TLS 1.3 ✓ ENCRYPTED");
        Log.d(TAG, "Headers: [ENCRYPTED]");
        Log.d(TAG, "Status: ✓ Secure connection established");
        Log.d(TAG, "");
    }

    private void makeInsecureMetricsRequest() {
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "[Request #4] Metrics Endpoint");
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "POST http://metrics.old-service.com/collect");
        Log.d(TAG, "Protocol: HTTP/1.1 WARNING: PLAINTEXT");
        Log.d(TAG, "");
        Log.d(TAG, "Headers:");
        Log.d(TAG, "  Content-Type: application/json");
        Log.d(TAG, "Body:");
        Log.d(TAG, "  {\"event\":\"app_open\",\"user_id\":\"u_847263\"}");
        Log.d(TAG, "");
        Log.d(TAG, "WARNING: Non-sensitive data, but still unencrypted");
        Log.d(TAG, "");
    }

    private void makeSecureAuthRequest() {
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "[Request #5] Authentication Endpoint");
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "POST https://auth.secure-api.com/token/refresh");
        Log.d(TAG, "Protocol: HTTPS/TLS 1.3 ✓ ENCRYPTED");
        Log.d(TAG, "Headers: [ENCRYPTED]");
        Log.d(TAG, "Body: [ENCRYPTED]");
        Log.d(TAG, "Status: ✓ Secure connection established");
        Log.d(TAG, "");
        Log.d(TAG, "═══════════════════════════════════");
        Log.d(TAG, "Network capture complete");
        Log.d(TAG, "═══════════════════════════════════");
    }

    private String reconstructFlag() {
        byte[] combined = new byte[ENC_PART1.length + ENC_PART2.length + ENC_PART3.length + ENC_PART4.length + ENC_PART5.length];
        int pos = 0;
        
        System.arraycopy(ENC_PART1, 0, combined, pos, ENC_PART1.length);
        pos += ENC_PART1.length;
        
        System.arraycopy(ENC_PART2, 0, combined, pos, ENC_PART2.length);
        pos += ENC_PART2.length;
        
        System.arraycopy(ENC_PART3, 0, combined, pos, ENC_PART3.length);
        pos += ENC_PART3.length;
        
        System.arraycopy(ENC_PART4, 0, combined, pos, ENC_PART4.length);
        pos += ENC_PART4.length;
        
        System.arraycopy(ENC_PART5, 0, combined, pos, ENC_PART5.length);
        
        return new String(combined, StandardCharsets.UTF_8);
    }

    private void submitFlag() {
        String enteredFlag = binding.flagInput.getText().toString().trim();

        if (enteredFlag.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a flag", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isValid = viewModel.validateFlag(enteredFlag);

        if (isValid) {
            progressTracker.markCompleted(FlagValidator.Module.INSECURE_COMM_CHALLENGE);
            int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.INSECURE_COMM_CHALLENGE);
            String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
            
            binding.flagValidationCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            binding.resultText.setText("✓ Correct Flag!\n\nYou successfully intercepted the insecure HTTP traffic and found the session token!");
            binding.resultText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Challenge Complete!", Toast.LENGTH_LONG).show();
            
            new AlertDialog.Builder(requireContext())
                .setTitle("🎉 Success!")
                .setMessage("Congratulations! You intercepted insecure network traffic.\n\nFlag: " + enteredFlag + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
                .setPositiveButton("OK", null)
                .show();
            
            binding.submitFlagButton.setEnabled(false);
            binding.flagInput.setEnabled(false);
        } else {
            binding.flagValidationCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            binding.resultText.setText("✗ Incorrect Flag\n\nHint: Look for HTTP (not HTTPS) requests in the logs");
            binding.resultText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Incorrect flag. Keep analyzing!", Toast.LENGTH_SHORT).show();
            
            binding.flagInput.setText("");
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
