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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentClientSideInjectionLessonBinding;
import com.owasp.app.ui.lessons.clientsideinjection.helpers.DatabaseHelper;

public class ClientSideInjectionLessonFragment extends Fragment {

    private FragmentClientSideInjectionLessonBinding binding;
    private DatabaseHelper dbHelper;
    private static final String TAG = "ClientSideInjection";
    
    // Hidden flag for successful SQL injection
    private static final String HIDDEN_FLAG = "FLAG{CL13NT_S1D3_SQL_1NJ3CT10N}";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClientSideInjectionLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize database
        dbHelper = new DatabaseHelper(requireContext());
        initializeDatabase();

        // Setup FAB for lesson info
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showDetailedInfo());
        }

        // Setup search button
        binding.searchButton.setOnClickListener(v -> performSearch());

        return root;
    }

    private void initializeDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // Clear existing data
        db.execSQL("DELETE FROM users");
        
        // Insert sample data
        insertUser(db, "admin", "admin@app.com", "Administrator", false);
        insertUser(db, "alice", "alice@app.com", "Alice Smith", false);
        insertUser(db, "bob", "bob@app.com", "Bob Jones", false);
        insertUser(db, "charlie", "charlie@app.com", "Charlie Brown", false);
        
        // Insert hidden admin user with flag (obscure username)
        insertUser(db, "sys_root", "root@system.internal", HIDDEN_FLAG, true);
        
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
                if (fullName.contains("FLAG{")) {
                    results.append("\n🎉 SUCCESS! You found the hidden flag!\n");
                    results.append("Flag: ").append(fullName).append("\n");
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

    private void showDetailedInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        TextView bestPracticesText = dialogView.findViewById(R.id.best_practices_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.client_side_injection_lesson_intro);
        vulnerabilitiesText.setText(R.string.client_side_injection_lesson_vulnerabilities);
        
        hintsSection.setVisibility(View.VISIBLE);
        hintsText.setText(R.string.client_side_injection_lesson_hints);
        
        bestPracticesSection.setVisibility(View.VISIBLE);
        bestPracticesText.setText(R.string.client_side_injection_lesson_best_practices);
        
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Client-Side Injection Lesson")
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
