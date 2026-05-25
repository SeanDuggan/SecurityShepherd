package com.owasp.app.ui.lessons.insecurecomm;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentInsecureCommLessonBinding;
import com.owasp.app.utils.FlagProvider;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import android.content.Intent;
import android.net.Uri;

public class InsecureCommLessonFragment extends Fragment {

    private FragmentInsecureCommLessonBinding binding;
    private static final String TAG = "NetworkTraffic";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    private String currentFlag = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInsecureCommLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());
        FlagProvider.getFlag(requireContext(), FlagValidator.Module.INSECURE_COMM_LESSON,
                flagValue -> currentFlag = flagValue);

        // Setup FAB expansion
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

        binding.sendHttpButton.setOnClickListener(v -> sendInsecureRequest());

        // Wire up flag submission
        MaterialButton btnSubmit = root.findViewById(R.id.btn_submit_flag);
        TextInputEditText editFlag = root.findViewById(R.id.edit_flag);
        TextView resultText = root.findViewById(R.id.text_flag_result);
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> checkFlag(editFlag, resultText));
        }

        return root;
    }

    private void sendInsecureRequest() {
        binding.sendHttpButton.setEnabled(false);
        binding.statusText.setText("Sending HTTP request...");
        
        new Thread(() -> {
            try {
                // Reconstruct flag at runtime
                String apiKey = decodeFlag();
                
                // Send actual HTTP request - flag is the API key in headers
                URL url = new URL("http://api.mobile-analytics.test/v1/events");
                
                // Log basic info (but NOT the API key - they must intercept traffic)
                Log.d(TAG, "Sending HTTP POST to: " + url.toString());
                Log.d(TAG, "Protocol: HTTP (unencrypted)");
                
                // Actually send the connection
                try {
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("User-Agent", "MobileApp/2.1.0");
                    conn.setRequestProperty("X-API-Key", apiKey);  // FLAG IS HERE
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    
                    // JSON body with analytics event data
                    String jsonBody = "{\"event_type\":\"user_login\",\"user_id\":\"12345\",\"timestamp\":\"2026-05-04T10:30:00Z\",\"device\":\"Android\"}";
                    
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                    
                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "Server response: " + responseCode);
                    
                } catch (Exception e) {
                    // Connection will fail (.test domain doesn't resolve), but request was sent to proxy
                    Log.d(TAG, "Connection failed: " + e.getMessage());
                    Log.d(TAG, "HTTP request was transmitted (intercept it to see the API key)");
                }
                
                requireActivity().runOnUiThread(() -> {
                    String requestPreview = "✓ Analytics event transmitted!\n\n" +
                            "Request sent to: http://api.mobile-analytics.test/v1/events\n\n" +
                            "HTTP Request Structure:\n" +
                            "POST /v1/events HTTP/1.1\n" +
                            "Content-Type: application/json\n" +
                            "User-Agent: MobileApp/2.1.0\n" +
                            "X-API-Key: ████████████████\n\n" +
                            "Body:\n" +
                            "{\"event_type\":\"user_login\",...}\n\n" +
                            "The API key was sent in plaintext!\n" +
                            "Use a proxy tool to intercept and capture it.";
                    
                    binding.statusText.setText(requestPreview);
                    binding.statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    binding.demoCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                    
                    Toast.makeText(getContext(), "Event sent over HTTP! Intercept to capture the API key.", Toast.LENGTH_LONG).show();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    binding.statusText.setText("Error: " + e.getMessage());
                    binding.sendHttpButton.setEnabled(true);
                });
            }
        }).start();
    }

    private String decodeFlag() {
        return currentFlag;
    }

    private void checkFlag(TextInputEditText editFlag, TextView resultText) {
        if (editFlag == null || resultText == null) return;
        String entered = editFlag.getText() != null ? editFlag.getText().toString().trim() : "";
        if (entered.isEmpty()) {
            resultText.setVisibility(View.VISIBLE);
            resultText.setText("Please enter the intercepted API key.");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark));
            return;
        }
        resultText.setVisibility(View.VISIBLE);
        if (entered.equals(currentFlag)) {
            resultText.setText("✓ Flag captured!");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green));
            if (!progressTracker.isCompleted(FlagValidator.Module.INSECURE_COMM_LESSON)) {
                progressTracker.markCompleted(FlagValidator.Module.INSECURE_COMM_LESSON);
                FlagValidator.validateFlag(requireContext(), FlagValidator.Module.INSECURE_COMM_LESSON,
                        currentFlag, correct -> Log.d(TAG, "Server submission: " + correct));
            }
        } else {
            resultText.setText("✗ Incorrect — keep intercepting.");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.security_red));
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
        // M5: Insecure Communication
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m5-insecure-communication";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void showDetailedInfo() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(getString(R.string.insecure_comm_intro));
        // vulnerabilitiesText.setText(getString(R.string.insecure_comm_vulnerabilities));
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Insecure Communication");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Hide mini FABs when leaving fragment
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
        
        binding = null;
    }
}
