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

import org.json.JSONException;
import org.json.JSONObject;

public class SupplyChainLessonFragment extends Fragment {

    private FragmentSupplyChainLessonBinding binding;
    private static final String TAG = "SupplyChainLesson";
    
    // Simulated vulnerable third-party library with hardcoded API key
    private static final String THIRD_PARTY_API_KEY = "sk_live_vulnerable_key_12345";
    private static final String DEMO_FLAG = "OWASP{Vuln3r4bl3_D3p3nd3ncy}";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSupplyChainLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup FAB for detailed information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showDetailedInfo());
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

    private void showDetailedInfo() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(getString(R.string.supply_chain_intro));
        vulnerabilitiesText.setText(getString(R.string.supply_chain_vulnerabilities));
        
        hintsSection.setVisibility(View.VISIBLE);
        hintsText.setText(getString(R.string.supply_chain_lesson_hint));
        
        bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Supply Chain Security");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
