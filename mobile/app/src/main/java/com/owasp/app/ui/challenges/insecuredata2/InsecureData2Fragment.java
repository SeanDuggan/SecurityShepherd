package com.owasp.app.ui.challenges.insecuredata2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentInsecureData2Binding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

import java.io.File;

public class InsecureData2Fragment extends Fragment {

    private FragmentInsecureData2Binding binding;
    private InsecureData2Model viewModel;
    private static final String PREFS_NAME = "UserCredentials";
    private ProgressTracker progressTracker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(InsecureData2Model.class);

        binding = FragmentInsecureData2Binding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Setup FAB for vulnerability information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showVulnerabilityInfo());
        }

        // Store sensitive data in SharedPreferences
        storeCredentialsInSharedPrefs();

        // Setup validation
        EditText flagInput = binding.flagInput;
        Button validateButton = binding.validateButton;

        validateButton.setOnClickListener(v -> {
            String enteredFlag = flagInput.getText().toString().trim();
            boolean isValid = viewModel.validateFlag(enteredFlag);

            if (isValid) {
                progressTracker.markCompleted(FlagValidator.Module.IDS_CHALLENGE_2);
                int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.IDS_CHALLENGE_2);
                String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
                
                Toast.makeText(getContext(), "Correct! Flag validated successfully!", Toast.LENGTH_LONG).show();
                binding.resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                
                new AlertDialog.Builder(requireContext())
                    .setTitle("🎉 Success!")
                    .setMessage("Congratulations! You extracted data from SharedPreferences.\n\nFlag: " + enteredFlag + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
                    .setPositiveButton("OK", null)
                    .show();
            } else {
                Toast.makeText(getContext(), "Incorrect flag. Keep looking!", Toast.LENGTH_SHORT).show();
                binding.resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                flagInput.setText("");
            }
        });

        return root;
    }

    private void showVulnerabilityInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.insecure_data_intro);
        // vulnerabilitiesText.setText(R.string.insecure_data_locations);
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Insecure Data Storage - SharedPreferences")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void storeCredentialsInSharedPrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Store multiple sensitive credentials in plain text
        editor.putString("username", "admin");
        editor.putString("password", "SuperSecret2024!");
        editor.putString("api_key", "sk_live_51HyperSecureApiKey789");
        editor.putString("auth_token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        editor.putString("encryption_key", "AES256-MyVerySecretKey-DoNotShare");
        editor.putString("secret_flag", "MobileSh3ph3rd_Pr3fs_Vu1n");
        editor.putBoolean("is_admin", true);
        editor.putInt("user_id", 1337);
        
        editor.apply();

        // Show the file location to the user
        File prefsFile = new File(requireContext().getApplicationInfo().dataDir + "/shared_prefs/" + PREFS_NAME + ".xml");
        if (prefsFile.exists()) {
            Toast.makeText(getContext(), 
                "SharedPreferences file created at:\n" + prefsFile.getAbsolutePath(), 
                Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
