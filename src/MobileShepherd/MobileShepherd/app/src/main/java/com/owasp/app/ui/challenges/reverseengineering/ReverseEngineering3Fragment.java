package com.owasp.app.ui.challenges.reverseengineering;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentReverseEngineering3Binding;
import com.owasp.app.ui.challenges.reverseengineering.ReverseEngineering3Model;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class ReverseEngineering3Fragment extends Fragment {

    private FragmentReverseEngineering3Binding binding;
    private ReverseEngineering3Model challengeModel;
    private ProgressTracker progressTracker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        challengeModel = new ViewModelProvider(this).get(ReverseEngineering3Model.class);

        binding = FragmentReverseEngineering3Binding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Setup FAB for vulnerability information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showVulnerabilityInfo());
        }

        final EditText inputFlag = binding.inputFlag;
        final Button btnValidate = binding.btnValidate;
        final Button btnClear = binding.btnClear;
        final TextView textResult = binding.textResult;

        btnValidate.setOnClickListener(v -> {
            String userInput = inputFlag.getText().toString().trim();
            if (challengeModel.validateFlag(userInput)) {
                progressTracker.markCompleted(FlagValidator.Module.RE_CHALLENGE_3);
                int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.RE_CHALLENGE_3);
                String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
                
                textResult.setText(R.string.challenge3_success);
                textResult.setTextColor(Color.GREEN);
                textResult.setVisibility(View.VISIBLE);
                
                new AlertDialog.Builder(requireContext())
                    .setTitle("🎉 Success!")
                    .setMessage("Congratulations! You reconstructed the obfuscated flag.\n\nFlag: " + userInput + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
                    .setPositiveButton("OK", null)
                    .show();
            } else {
                textResult.setText(R.string.challenge3_failure);
                textResult.setTextColor(Color.RED);
                textResult.setVisibility(View.VISIBLE);
            }
        });

        btnClear.setOnClickListener(v -> {
            inputFlag.setText("");
            textResult.setVisibility(View.GONE);
        });

        return root;
    }

    private void showVulnerabilityInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.lesson_intro);
        vulnerabilitiesText.setText(R.string.lesson_tools);
        
        hintsSection.setVisibility(View.GONE);
        
        bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Reverse Engineering Challenge 3")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}