package com.owasp.app.ui.challenges.supplychain;

import android.content.Intent;
import android.net.Uri;
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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentSupplyChainChallengeBinding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SupplyChainChallengeFragment extends Fragment {

    private FragmentSupplyChainChallengeBinding binding;
    private SupplyChainChallengeModel viewModel;
    private static final String TAG = "SupplyChainChallenge";
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;

    // Simulated vulnerable SDK with multiple supply chain issues
    private static final String VENDOR_SDK_VERSION = "VulnSDK-1.2.3";
    private static final String BACKDOOR_TOKEN = "bGVnYWN5X2JhY2tkb29yX3Rva2VuXzIwMjM="; // base64 encoded
    private boolean sdkInitialized = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSupplyChainChallengeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(SupplyChainChallengeModel.class);
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

        // Initialize vulnerable SDK
        initializeVulnerableSDK();

        // Setup UI
        binding.analyzeButton.setOnClickListener(v -> analyzeSDK());
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
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m8-code-tampering";
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
        
        introText.setText(R.string.supply_chain_intro);
        // vulnerabilitiesText.setText(R.string.supply_chain_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Supply Chain Security Challenge")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void initializeVulnerableSDK() {
        Log.d(TAG, "=== SDK Initialization ===");
        Log.d(TAG, "Loading " + VENDOR_SDK_VERSION + " from vendor ThirdPartyAnalytics Inc.");
        Log.d(TAG, "SDK build date: 2023-08-15 (OUTDATED - 16 months old)");
        Log.d(TAG, "Known CVEs: CVE-2023-45678, CVE-2024-12345");
        Log.w(TAG, "WARNING: This SDK version has unpatched security vulnerabilities");
        
        // Simulate SDK writing telemetry with sensitive data
        writeTelemetryData();
        
        // Simulate backdoor initialization
        String decodedToken = new String(Base64.decode(BACKDOOR_TOKEN, Base64.DEFAULT));
        Log.d(TAG, "SDK initialized with legacy authentication token");
        Log.v(TAG, "Debug mode enabled. Auth token: " + decodedToken);
        
        sdkInitialized = true;
        Log.d(TAG, "=== SDK Ready ===");
    }

    private void writeTelemetryData() {
        try {
            File telemetryFile = new File(requireContext().getFilesDir(), "sdk_telemetry.log");
            FileWriter writer = new FileWriter(telemetryFile, true);
            writer.write("SDK_VERSION=" + VENDOR_SDK_VERSION + "\n");
            writer.write("BACKDOOR_TOKEN=" + BACKDOOR_TOKEN + "\n");
            writer.write("API_ENDPOINT=https://analytics.vulnerable-vendor.com/collect\n");
            writer.write("TIMESTAMP=" + System.currentTimeMillis() + "\n");
            writer.close();
            
            Log.d(TAG, "Telemetry data written to: " + telemetryFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error writing telemetry: " + e.getMessage());
        }
    }

    private void analyzeSDK() {
        if (!sdkInitialized) {
            Toast.makeText(getContext(), "SDK not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show analysis section
        binding.analysisSection.setVisibility(View.VISIBLE);
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("📦 Dependency Analysis\n\n");
        analysis.append("SDK Name: ").append(VENDOR_SDK_VERSION).append("\n");
        analysis.append("Status: VULNERABLE\n");
        analysis.append("Last Updated: 16 months ago\n\n");
        analysis.append("Findings:\n");
        analysis.append("• Outdated dependency with known CVEs\n");
        analysis.append("• Hardcoded credentials detected\n");
        analysis.append("• Insecure data storage in telemetry logs\n");
        analysis.append("• Base64-encoded backdoor token found\n\n");
        analysis.append("Hint: Check logcat verbose logs and app files directory");
        
        binding.analysisResults.setText(analysis.toString());
        
        Log.d(TAG, "Analysis complete. Inspect logs and filesystem for more details.");
        Log.v(TAG, "Files directory: " + requireContext().getFilesDir().getAbsolutePath());
        
        Toast.makeText(getContext(), "Analysis complete! Check the results above and logcat", Toast.LENGTH_LONG).show();
    }

    private void submitFlag() {
        String enteredFlag = binding.flagInput.getText().toString().trim();

        if (enteredFlag.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a flag", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isValid = viewModel.validateFlag(enteredFlag);

        if (isValid) {
            progressTracker.markCompleted(FlagValidator.Module.SUPPLY_CHAIN_CHALLENGE);
            int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.SUPPLY_CHAIN_CHALLENGE);
            String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
            
            binding.flagValidationCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            binding.resultText.setText("✓ Correct Flag!\n\nYou successfully identified and exploited the supply chain vulnerability!");
            binding.resultText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Challenge Complete!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Challenge solved! Supply chain vulnerability successfully exploited.");
            
            new AlertDialog.Builder(requireContext())
                .setTitle("🎉 Success!")
                .setMessage("Congratulations! You exploited the supply chain vulnerability.\n\nFlag: " + enteredFlag + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
                .setPositiveButton("OK", null)
                .show();
            
            binding.submitFlagButton.setEnabled(false);
            binding.flagInput.setEnabled(false);
        } else {
            binding.flagValidationCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            binding.resultText.setText("✗ Incorrect Flag");
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
