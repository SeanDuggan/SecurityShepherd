package com.owasp.app.ui.lessons.supplychain;

import android.os.Bundle;
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
import com.owasp.app.databinding.FragmentSupplyChainLessonBinding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;

public class SupplyChainLessonFragment extends Fragment {

    private FragmentSupplyChainLessonBinding binding;
    private static final String TAG = "SupplyChainLesson";
    
    // Simulated vulnerable third-party library with hardcoded API key
    private static final String THIRD_PARTY_API_KEY = "sk_live_vulnerable_key_12345";
    private static final String DEMO_FLAG = "KEY{Vuln3r4bl3_D3p3nd3ncy}";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSupplyChainLessonBinding.inflate(inflater, container, false);
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

        // Log initialization showing vulnerable dependency behavior
        Log.d(TAG, "Initializing third-party analytics library v2.3.1 (VULNERABLE)");
        Log.d(TAG, "WARNING: This version contains known security vulnerabilities CVE-2023-12345");
        
        // Setup demo section
        binding.checkDependencyButton.setOnClickListener(v -> checkDependency());

        return root;
    }

    private void checkDependency() {
        String enteredKey = binding.apiKeyInput.getText().toString().trim();

        if (enteredKey.isEmpty()) {
            Toast.makeText(getContext(), "Please enter an API key", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Validating API key from vulnerable dependency...");
        Log.d(TAG, "Comparing: " + enteredKey + " with stored key: " + THIRD_PARTY_API_KEY);

        if (enteredKey.equals(THIRD_PARTY_API_KEY)) {
            // Successful exploitation
            binding.demoCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            binding.flagText.setText("✓ Vulnerable Dependency Exploited!\n\nFlag: " + DEMO_FLAG);
            binding.flagText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Access Granted! You exploited the vulnerable third-party library!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "CRITICAL: Hardcoded credentials exposed! Flag: " + DEMO_FLAG);
            
            binding.checkDependencyButton.setEnabled(false);
            binding.apiKeyInput.setEnabled(false);
        } else {
            // Failed attempt
            binding.demoCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            binding.flagText.setVisibility(View.GONE);
            
            Toast.makeText(getContext(), "Invalid API Key", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "API key validation failed.");
            
            binding.apiKeyInput.setText("");
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
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m8-code-tampering";
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
        
        introText.setText(getString(R.string.supply_chain_intro));
        // vulnerabilitiesText.setText(getString(R.string.supply_chain_vulnerabilities));
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.SUPPLY_CHAIN_LESSON);
        String buttonText = isCompleted ? "Mark as Incomplete" : "Mark as Complete";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Supply Chain Security");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.setNeutralButton(buttonText, (d, which) -> {
            boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.SUPPLY_CHAIN_LESSON);
            String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
        builder.show();
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
