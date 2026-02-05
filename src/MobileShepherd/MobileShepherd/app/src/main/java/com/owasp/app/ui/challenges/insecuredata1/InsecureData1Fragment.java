package com.owasp.app.ui.challenges.insecuredata1;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
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
import com.owasp.app.databinding.FragmentInsecureData1Binding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

import java.io.File;

public class InsecureData1Fragment extends Fragment {

    private FragmentInsecureData1Binding binding;
    private InsecureData1Model viewModel;
    private SQLiteDatabase passwordDB = null;
    private ProgressTracker progressTracker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(InsecureData1Model.class);

        binding = FragmentInsecureData1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Setup FAB for vulnerability information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showVulnerabilityInfo());
        }

        // Create the vulnerable database
        createDatabase();
        insertKey();

        // Setup validation
        EditText flagInput = binding.flagInput;
        Button validateButton = binding.validateButton;

        validateButton.setOnClickListener(v -> {
            String enteredFlag = flagInput.getText().toString().trim();
            boolean isValid = viewModel.validateFlag(enteredFlag);

            if (isValid) {
                progressTracker.markCompleted(FlagValidator.Module.IDS_CHALLENGE_1);
                int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.IDS_CHALLENGE_1);
                String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
                
                Toast.makeText(getContext(), "Correct! Flag validated successfully!", Toast.LENGTH_LONG).show();
                binding.resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                
                new AlertDialog.Builder(requireContext())
                    .setTitle("🎉 Success!")
                    .setMessage("Congratulations! You found the MD5 hash.\n\nFlag: " + enteredFlag + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
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
        TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.insecure_data_intro);
        vulnerabilitiesText.setText(R.string.insecure_data_vulns);
        
        hintsSection.setVisibility(View.VISIBLE);
        hintsText.setText(R.string.insecure_data1_hint);
        
        bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Insecure Data Storage - SQLite")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void createDatabase() {
        try {
            passwordDB = requireContext().openOrCreateDatabase("passwordDB", android.content.Context.MODE_PRIVATE, null);
            passwordDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS passwordDB " +
                            "(id integer primary key, name VARCHAR, password VARCHAR);");

            File database = requireActivity().getApplication().getDatabasePath("passwordDB.db");

            if (database.exists()) {
                Toast.makeText(getContext(), "Database initialized at: " + database.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e("DB ERROR", "Error Creating Database", e);
        }
    }

    private void insertKey() {
        if (passwordDB != null) {
            passwordDB.execSQL("DELETE FROM passwordDB;");
            passwordDB.execSQL(
                    "INSERT INTO passwordDB (name, password) VALUES " +
                            "('Admin','0e3a0c8c3a571a855c958813d9b851a1');");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (passwordDB != null) {
            passwordDB.close();
        }
        binding = null;
    }
}
