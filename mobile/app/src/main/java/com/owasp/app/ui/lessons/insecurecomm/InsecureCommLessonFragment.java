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
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentInsecureCommLessonBinding;
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
    
    // Obfuscated flag components - direct string parts
    private static final String PART1 = "OWASP";
    private static final String PART2 = "{H1TTP";
    private static final String PART3 = "_Insec";
    private static final String PART4 = "ure_F1";
    private static final String PART5 = "nd}";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInsecureCommLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Setup FAB expansion
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

        // Set initial FAB appearance based on completion status
        updateMarkCompleteFabAppearance(fabMarkComplete);

        binding.sendHttpButton.setOnClickListener(v -> sendInsecureRequest());

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
        // Reconstruct flag from obfuscated parts at runtime
        return PART1 + PART2 + PART3 + PART4 + PART5;
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
    
    private void toggleCompleteStatus() {
        boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.INSECURE_COMM_LESSON);
        String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private void updateMarkCompleteFabAppearance(FloatingActionButton fabMarkComplete) {
        if (fabMarkComplete == null) return;
        
        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.INSECURE_COMM_LESSON);
        
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
        
        // Hide mini FABs when leaving fragment
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        FloatingActionButton fabMarkComplete = requireActivity().findViewById(R.id.fab_mark_complete);
        collapseFab(fab, fabCommandRef, fabOwaspLink, fabMarkComplete);
        
        binding = null;
    }
}
