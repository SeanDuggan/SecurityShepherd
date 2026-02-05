package com.owasp.app.ui.challenges.clientsideinjection;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentClientSideInjectionChallenge1Binding;
import com.owasp.app.ui.challenges.clientsideinjection.helpers.Challenge1DatabaseHelper;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class ClientSideInjectionChallenge1Fragment extends Fragment {

    private FragmentClientSideInjectionChallenge1Binding binding;
    private ClientSideInjectionChallenge1Model viewModel;
    private ClientSideInjectionChallenge1Model model;
    private Challenge1DatabaseHelper dbHelper;
    private ProgressTracker progressTracker;
    private static final String TAG = "CSI_Challenge1";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClientSideInjectionChallenge1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(ClientSideInjectionChallenge1Model.class);
        model = viewModel;

        // Initialize progress tracker
        progressTracker = new ProgressTracker(requireContext());

        // Initialize database
        dbHelper = new Challenge1DatabaseHelper(requireContext());
        initializeDatabase();

        // Setup FAB for vulnerability info
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showVulnerabilityInfo());
        }

        // Setup login button
        binding.loginButton.setOnClickListener(v -> attemptLogin());

        // Setup submit flag button
        binding.submitFlagButton.setOnClickListener(v -> submitFlag());

        return root;
    }

    private void initializeDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // Clear existing data
        db.execSQL("DELETE FROM accounts");
        
        // Insert sample accounts
        insertAccount(db, "user1", "password123", "Regular User", 100);
        insertAccount(db, "user2", "qwerty456", "Another User", 250);
        insertAccount(db, "guest", "guest", "Guest Account", 0);
        
        // Insert admin account with hidden flag (OWASP{} format)
        insertAccount(db, "admin", "OWASP{SQL_1nj3ct10n_4dm1n_Pwn}", "System Administrator", 9999);
        
        db.close();
    }

    private void insertAccount(SQLiteDatabase db, String username, String password, String role, int balance) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("role", role);
        values.put("balance", balance);
        db.insert("accounts", null, values);
    }

    private void attemptLogin() {
        String username = binding.usernameInput.getText().toString();
        String password = binding.passwordInput.getText().toString();
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // VULNERABLE: Building SQL query with string concatenation
        String query = "SELECT username, password, role, balance FROM accounts WHERE username = '" + 
                       username + "' AND password = '" + password + "'";
        
        Log.d(TAG, "Executing query: " + query);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(query, null);
            
            if (cursor.moveToFirst()) {
                String retrievedUsername = cursor.getString(0);
                String retrievedPassword = cursor.getString(1);
                String role = cursor.getString(2);
                int balance = cursor.getInt(3);
                
                StringBuilder result = new StringBuilder();
                result.append("✓ Login Successful!\n\n");
                result.append("Username: ").append(retrievedUsername).append("\n");
                result.append("Role: ").append(role).append("\n");
                result.append("Balance: $").append(balance).append("\n\n");
                
                // Check if admin credentials were retrieved
                if (retrievedPassword.equals("SourHatsAndAngryCats")) {
                    result.append("🎉 FLAG DISCOVERED!\n");
                    result.append("Flag: ").append(retrievedPassword).append("\n\n");
                    result.append("Submit this flag to complete the challenge!");
                }
                
                binding.resultText.setText(result.toString());
            } else {
                binding.resultText.setText("✗ Login failed!\n\nInvalid credentials.");
            }
            
        } catch (Exception e) {
            binding.resultText.setText("Error: " + e.getMessage() + "\n\nHint: Try manipulating the SQL query!");
            Log.e(TAG, "SQL Error", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    private void submitFlag() {
        String submittedFlag = binding.flagInput.getText().toString();
        
        if (submittedFlag.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the flag", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (model.validateFlag(submittedFlag)) {
            // Mark challenge as completed
            progressTracker.markCompleted(FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1);
            
            // Show completion stats
            int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1);
            String completionText = completionCount > 1 ? 
                " (Completed " + completionCount + " times)" : "";
            
            new AlertDialog.Builder(requireContext())
                    .setTitle("🎉 Success!")
                    .setMessage("Congratulations! You've successfully exploited the SQL injection vulnerability and retrieved the admin password.\n\nFlag: " + submittedFlag + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
                    .setPositiveButton("OK", null)
                    .show();
            binding.flagInput.setText("");
        } else {
            Toast.makeText(getContext(), "Incorrect flag. Keep trying!", Toast.LENGTH_LONG).show();
        }
    }

    private void showVulnerabilityInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        TextView bestPracticesText = dialogView.findViewById(R.id.best_practices_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.client_side_injection_challenge1_intro);
        vulnerabilitiesText.setText(R.string.client_side_injection_challenge1_vulnerabilities);
        
        hintsSection.setVisibility(View.VISIBLE);
        hintsText.setText(R.string.client_side_injection_challenge1_hints);
        
        bestPracticesSection.setVisibility(View.VISIBLE);
        bestPracticesText.setText(R.string.client_side_injection_challenge1_best_practices);
        
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Client-Side Injection Challenge 1")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
        binding = null;
    }
}
