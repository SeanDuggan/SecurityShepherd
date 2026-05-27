package com.owasp.app.ui.lessons.clientsideinjection;

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

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentClientSideInjectionLessonBinding;
import com.owasp.app.ui.lessons.clientsideinjection.helpers.DatabaseHelper;
import com.owasp.app.utils.AuthManager;
import com.owasp.app.utils.FlagProvider;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class ClientSideInjectionLessonFragment extends Fragment {

    private FragmentClientSideInjectionLessonBinding binding;
    private DatabaseHelper dbHelper;
    private static final String TAG = "ClientSideInjection";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;

    // The flag seeded into the SQLite DB. In offline mode this is the static
    // plaintext value; in online mode FlagProvider replaces it with the
    // server-generated user-specific HMAC after the view is created.
    private String currentFlag = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClientSideInjectionLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize database
        dbHelper = new DatabaseHelper(requireContext());
        progressTracker = new ProgressTracker(requireContext());

        // Seed with offline flag immediately so the lesson is usable right away,
        // then asynchronously replace with the dynamic server flag if online.
        FlagProvider.getFlag(
                requireContext(),
                FlagValidator.Module.CLIENT_SIDE_INJECTION_LESSON,
                flag -> {
                    if (!isAdded()) return;
                    currentFlag = flag;
                    initializeDatabase(flag);
                });

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

        // Setup search button
        binding.searchButton.setOnClickListener(v -> performSearch());

        return root;
    }

    private void initializeDatabase(String flagValue) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // Clear existing data
        db.execSQL("DELETE FROM users");
        
        // Insert sample data
        insertUser(db, "admin", "admin@app.com", "Administrator", false);
        insertUser(db, "alice", "alice@app.com", "Alice Smith", false);
        insertUser(db, "bob", "bob@app.com", "Bob Jones", false);
        insertUser(db, "charlie", "charlie@app.com", "Charlie Brown", false);
        
        // Insert hidden admin user with flag (obscure username)
        insertUser(db, "sys_root", "root@system.internal", flagValue, true);
        
        db.close();
    }

    private void insertUser(SQLiteDatabase db, String username, String email, String fullName, boolean isAdmin) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("full_name", fullName);
        values.put("is_admin", isAdmin ? 1 : 0);
        db.insert("users", null, values);
    }

    private void performSearch() {
        String searchTerm = binding.searchInput.getText().toString();
        
        if (searchTerm.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        // VULNERABLE: Concatenating user input directly into SQL query
        String query = "SELECT username, email, full_name, is_admin FROM users WHERE username = '" + searchTerm + "'";
        
        Log.d(TAG, "Executing query: " + query);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(query, null);
            
            StringBuilder results = new StringBuilder();
            int count = 0;
            
            while (cursor.moveToNext()) {
                count++;
                String username = cursor.getString(0);
                String email = cursor.getString(1);
                String fullName = cursor.getString(2);
                int isAdmin = cursor.getInt(3);
                
                results.append("Username: ").append(username).append("\n");
                results.append("Email: ").append(email).append("\n");
                results.append("Full Name: ").append(fullName).append("\n");
                results.append("Admin: ").append(isAdmin == 1 ? "Yes" : "No").append("\n");
                results.append("---\n");
                
                // Check if flag was found
                if (fullName.contains("KEY{")) {
                    results.append("\nSUCCESS! You found the hidden flag!\n");
                    results.append("Flag: ").append(fullName).append("\n");
                    results.append("Validating against server...\n");
                    submitFlagToServer(fullName);
                }
            }
            
            if (count == 0) {
                binding.resultText.setText("No users found matching: " + searchTerm);
            } else {
                binding.resultText.setText(results.toString());
            }
            
        } catch (Exception e) {
            binding.resultText.setText("Error: " + e.getMessage() + "\n\nTip: Check your SQL syntax!");
            Log.e(TAG, "SQL Error", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
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
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m7-client-code-quality";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void showDetailedInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        TextView moduleBanner = dialogView.findViewById(R.id.module_path_banner);
        if (moduleBanner != null) moduleBanner.setText("com.owasp.client_side_injection");
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.client_side_injection_lesson_intro);
        // vulnerabilitiesText.setText(R.string.client_side_injection_lesson_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Client-Side Injection Lesson")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
    
    /**
     * Submits the discovered flag to the Shepherd server for validation.
     * Falls back to local SHA-256 comparison when no server is configured.
     * Marks the lesson complete and updates the FAB appearance on success.
     */
    private void submitFlagToServer(String flag) {
        // In case the DB was seeded before FlagProvider returned, use the
        // most recent flag value rather than the one passed by the search results.
        String flagToSubmit = currentFlag.isEmpty() ? flag : currentFlag;
        FlagValidator.validateFlag(
                requireContext(),
                FlagValidator.Module.CLIENT_SIDE_INJECTION_LESSON,
                flagToSubmit,
                correct -> {
                    if (!isAdded()) return;
                    if (correct) {
                        progressTracker.markCompleted(FlagValidator.Module.CLIENT_SIDE_INJECTION_LESSON);
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle("Lesson Complete")
                                .setMessage("Correct flag validated! You have successfully demonstrated "
                                        + "a client-side SQL injection attack.\n\n"
                                        + "Progress: "
                                        + progressTracker.getCompletedChallengesCount()
                                        + "/"
                                        + progressTracker.getTotalChallengesCount()
                                        + " modules completed")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        Toast.makeText(requireContext(),
                                "Flag incorrect — keep trying!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
        if (dbHelper != null) {
            dbHelper.close();
        }
        binding = null;
    }
}
