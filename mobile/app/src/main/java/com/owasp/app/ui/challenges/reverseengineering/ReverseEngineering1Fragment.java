package com.owasp.app.ui.challenges.reverseengineering;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentReverseEngineering1Binding;
import com.owasp.app.ui.challenges.reverseengineering.ReverseEngineering1Model;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class ReverseEngineering1Fragment extends Fragment {

    private FragmentReverseEngineering1Binding binding;
    private ReverseEngineering1Model challengeModel;
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        challengeModel = new ViewModelProvider(this).get(ReverseEngineering1Model.class);

        binding = FragmentReverseEngineering1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        FloatingActionButton fabMarkComplete = requireActivity().findViewById(R.id.fab_mark_complete);

        if (fab != null) {
            fab.setOnClickListener(v -> toggleFabExpansion(fab, fabCommandRef, fabOwaspLink, fabMarkComplete));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showVulnerabilityInfo();
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

        final EditText inputFlag = binding.inputFlag;
        final Button btnValidate = binding.btnValidate;
        final Button btnClear = binding.btnClear;
        final TextView textResult = binding.textResult;

        btnValidate.setOnClickListener(v -> {
            String userInput = inputFlag.getText().toString().trim();
            if (challengeModel.validateFlag(userInput)) {
                progressTracker.markCompleted(FlagValidator.Module.RE_CHALLENGE_1);
                int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.RE_CHALLENGE_1);
                String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
                
                textResult.setText(R.string.reverse_engineering_1_success);
                textResult.setTextColor(Color.GREEN);
                textResult.setVisibility(View.VISIBLE);
                
                new AlertDialog.Builder(requireContext())
                    .setTitle("🎉 Success!")
                    .setMessage("Congratulations! You found the flag.\n\nFlag: " + userInput + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
                    .setPositiveButton("OK", null)
                    .show();
            } else {
                textResult.setText(R.string.reverse_engineering_1_failure);
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
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m9-reverse-engineering";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void showVulnerabilityInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.lesson_intro);
        // vulnerabilitiesText.setText(R.string.lesson_tools);
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Reverse Engineering Challenge 1")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
    
    private void toggleCompleteStatus() {
        boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.RE_CHALLENGE_1);
        String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
    }
    
    private void updateMarkCompleteFabAppearance(FloatingActionButton fabMarkComplete) {
        if (fabMarkComplete == null) return;
        
        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.RE_CHALLENGE_1);
        
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
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        FloatingActionButton fabMarkComplete = requireActivity().findViewById(R.id.fab_mark_complete);
        collapseFab(fab, fabCommandRef, fabOwaspLink, fabMarkComplete);
        binding = null;
    }
}