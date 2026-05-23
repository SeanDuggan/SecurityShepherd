package com.owasp.app.ui.lessons.insecuredata;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentInsecureDataLessonBinding;
import com.owasp.app.utils.FlagProvider;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

import java.io.File;

public class InsecureDataLessonFragment extends Fragment {

    private FragmentInsecureDataLessonBinding binding;
    private SQLiteDatabase membersDb = null;
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    private String currentFlag = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInsecureDataLessonBinding.inflate(inflater, container, false);
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
            fabMarkComplete.setVisibility(View.GONE);
        }

        // Create insecure database for demonstration; seed users once flag is available
        createDatabase();
        FlagProvider.getFlag(requireContext(), FlagValidator.Module.IDS_LESSON,
                flagValue -> {
                    currentFlag = flagValue;
                    insertUsers();
                    displayUsers();
                });

        // Wire up credential submit button
        MaterialButton btnSubmit = root.findViewById(R.id.btn_submit_credentials);
        TextInputEditText editCredentials = root.findViewById(R.id.edit_credentials);
        TextView resultText = root.findViewById(R.id.text_credential_result);
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> checkCredentials(editCredentials, resultText));
        }

        return root;
    }

    private void createDatabase() {
        try {
            membersDb = requireContext().openOrCreateDatabase("Members", 
                android.content.Context.MODE_PRIVATE, null);
            membersDb.execSQL(
                "CREATE TABLE IF NOT EXISTS Members " +
                "(id integer primary key, name VARCHAR, password VARCHAR);");

            File database = requireActivity().getDatabasePath("Members.db");

            if (!database.exists()) {
                Toast.makeText(requireContext(), "Database Created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Database Already Exists", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("DB ERROR", "Error Creating Database", e);
        }
    }

    private void insertUsers() {
        if (membersDb != null) {
            try {
                membersDb.execSQL("DELETE FROM Members;");
                membersDb.execSQL("INSERT INTO Members (name, password) VALUES ('Admin', ?);",
                        new Object[]{currentFlag});
                membersDb.execSQL("INSERT INTO Members (name, password) VALUES ('john_doe','password123');");
                membersDb.execSQL("INSERT INTO Members (name, password) VALUES ('alice_smith','welcome2024');");
                membersDb.execSQL("INSERT INTO Members (name, password) VALUES ('bob_johnson','qwerty456');");
                membersDb.execSQL("INSERT INTO Members (name, password) VALUES ('sarah_wilson','letmein789');");
            } catch (Exception e) {
                Log.e("DB ERROR", "Error Inserting Users", e);
            }
        }
    }

    private void displayUsers() {
        if (membersDb != null && binding != null) {
            try {
                Cursor cursor = membersDb.rawQuery("SELECT name FROM Members", null);
                LinearLayout container = binding.getRoot().findViewById(R.id.users_list_container);
                
                if (container != null) {
                    container.removeAllViews();
                    
                    int userCount = 0;
                    while (cursor.moveToNext()) {
                        userCount++;
                        String username = cursor.getString(0);
                        
                        // Create a view for each user
                        LinearLayout userRow = new LinearLayout(requireContext());
                        userRow.setOrientation(LinearLayout.HORIZONTAL);
                        userRow.setPadding(0, 8, 0, 8);
                        
                        TextView userIcon = new TextView(requireContext());
                        userIcon.setText("• ");
                        userIcon.setTextSize(16);
                        
                        TextView userName = new TextView(requireContext());
                        userName.setText(username);
                        userName.setTextSize(16);
                        
                        TextView passwordHidden = new TextView(requireContext());
                        passwordHidden.setText("  •  Password: ******");
                        passwordHidden.setTextSize(14);
                        passwordHidden.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        
                        userRow.addView(userIcon);
                        userRow.addView(userName);
                        userRow.addView(passwordHidden);
                        container.addView(userRow);
                    }
                    cursor.close();
                    
                    // Add user count
                    if (userCount > 0) {
                        TextView countText = new TextView(requireContext());
                        countText.setText("\nTotal users: " + userCount);
                        countText.setTextSize(12);
                        countText.setTypeface(null, android.graphics.Typeface.ITALIC);
                        countText.setPadding(0, 12, 0, 0);
                        container.addView(countText);
                    }
                }
            } catch (Exception e) {
                Log.e("DB ERROR", "Error Displaying Users", e);
            }
        }
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
        // M9: Insecure Data Storage
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m9-insecure-data-storage";
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
        
        introText.setText(getString(R.string.insecure_data_intro));
        // vulnerabilitiesText.setText(getString(R.string.insecure_data_vulns));
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Insecure Data Storage");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }
    
    private void checkCredentials(TextInputEditText editCredentials, TextView resultText) {
        if (editCredentials == null || resultText == null) return;
        String input = editCredentials.getText() != null ? editCredentials.getText().toString().trim() : "";
        int colonIdx = input.indexOf(':');
        if (colonIdx <= 0 || colonIdx == input.length() - 1) {
            resultText.setVisibility(View.VISIBLE);
            resultText.setText("Please enter credentials as username:password");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark));
            return;
        }
        String enteredName = input.substring(0, colonIdx);
        String enteredPassword = input.substring(colonIdx + 1);

        if (membersDb == null) {
            resultText.setVisibility(View.VISIBLE);
            resultText.setText("Database not ready, please wait.");
            return;
        }

        Cursor cursor = membersDb.rawQuery(
                "SELECT name, password FROM Members WHERE name = ?",
                new String[]{enteredName});
        if (!cursor.moveToFirst()) {
            cursor.close();
            resultText.setVisibility(View.VISIBLE);
            resultText.setText("✗ Invalid credentials.");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.security_red));
            return;
        }
        String dbName = cursor.getString(0);
        String dbPassword = cursor.getString(1);
        cursor.close();

        if (!enteredPassword.equals(dbPassword)) {
            resultText.setVisibility(View.VISIBLE);
            resultText.setText("✗ Wrong password.");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.security_red));
            return;
        }

        resultText.setVisibility(View.VISIBLE);
        if (dbName.equalsIgnoreCase("Admin")) {
            resultText.setText("✓ Flag captured!");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green));
            if (!progressTracker.isCompleted(FlagValidator.Module.IDS_LESSON)) {
                progressTracker.markCompleted(FlagValidator.Module.IDS_LESSON);
                FlagValidator.validateFlag(requireContext(), FlagValidator.Module.IDS_LESSON,
                        currentFlag, correct -> Log.d("IDSLesson", "Server submission: " + correct));
            }
        } else {
            resultText.setText("✗ Valid user — but not admin.");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark));
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
        
        if (membersDb != null) {
            membersDb.close();
        }
        binding = null;
    }
}
