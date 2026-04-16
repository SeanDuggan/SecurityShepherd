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

        return root;
    }

    private void sendInsecureRequest() {
        binding.sendHttpButton.setEnabled(false);
        binding.statusText.setText("Sending HTTP request...");
        
        new Thread(() -> {
            try {
                // Reconstruct flag at runtime
                String flag = decodeFlag();
                
                // Simulate HTTP request with plaintext credentials
                URL url = new URL("http://api.insecure-app.com/login");
                
                // Log the entire HTTP request (what a packet sniffer would see)
                Log.d(TAG, "═══════════════════════════════════════");
                Log.d(TAG, "HTTP REQUEST CAPTURED");
                Log.d(TAG, "═══════════════════════════════════════");
                Log.d(TAG, "Method: POST");
                Log.d(TAG, "URL: " + url.toString());
                Log.d(TAG, "Protocol: HTTP/1.1 (UNENCRYPTED)");
                Log.d(TAG, "");
                Log.d(TAG, "Headers:");
                Log.d(TAG, "  Content-Type: application/json");
                Log.d(TAG, "  User-Agent: InsecureApp/1.0");
                Log.d(TAG, "  X-API-Key: sk_live_4829fjksd92jfks");
                Log.d(TAG, "");
                Log.d(TAG, "Request Body (JSON):");
                Log.d(TAG, "{");
                Log.d(TAG, "  \"username\": \"admin\",");
                Log.d(TAG, "  \"password\": \"" + flag + "\",");
                Log.d(TAG, "  \"device_id\": \"android_12345\"");
                Log.d(TAG, "}");
                Log.d(TAG, "═══════════════════════════════════════");
                Log.d(TAG, "WARNING: Credentials sent in PLAINTEXT!");
                Log.d(TAG, "WARNING: Any attacker on the network can read this!");
                Log.d(TAG, "═══════════════════════════════════════");
                
                // Actually attempt the connection (will fail, but that's fine)
                try {
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("X-API-Key", "sk_live_4829fjksd92jfks");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(3000);
                    conn.setReadTimeout(3000);
                    
                    String jsonBody = "{\"username\":\"admin\",\"password\":\"" + flag + "\",\"device_id\":\"android_12345\"}";
                    
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                    
                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "Response Code: " + responseCode);
                    
                } catch (Exception e) {
                    Log.d(TAG, "Connection failed (expected): " + e.getMessage());
                    Log.d(TAG, "But the HTTP request was logged above!");
                }
                
                requireActivity().runOnUiThread(() -> {
                    binding.statusText.setText("✓ HTTP request sent!\n\nCheck logcat to see the plaintext traffic.");
                    binding.statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    binding.demoCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                    
                    binding.flagText.setText("Flag found in HTTP traffic!\n\nFlag: " + flag);
                    binding.flagText.setVisibility(View.VISIBLE);
                    
                    Toast.makeText(getContext(), "Request logged! Check logcat with tag: NetworkTraffic", Toast.LENGTH_LONG).show();
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
        
        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.INSECURE_COMM_LESSON);
        String buttonText = isCompleted ? "Mark as Incomplete" : "Mark as Complete";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Insecure Communication");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.setNeutralButton(buttonText, (d, which) -> {
            boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.INSECURE_COMM_LESSON);
            String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
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
