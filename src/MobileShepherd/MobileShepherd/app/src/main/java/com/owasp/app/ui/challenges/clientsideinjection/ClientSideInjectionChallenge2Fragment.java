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
import com.owasp.app.databinding.FragmentClientSideInjectionChallenge2Binding;
import com.owasp.app.ui.challenges.clientsideinjection.helpers.Challenge2DatabaseHelper;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class ClientSideInjectionChallenge2Fragment extends Fragment {

    private FragmentClientSideInjectionChallenge2Binding binding;
    private ClientSideInjectionChallenge2Model viewModel;
    private Challenge2DatabaseHelper dbHelper;
    private static final String TAG = "CSI_Challenge2";
    private ProgressTracker progressTracker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClientSideInjectionChallenge2Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(ClientSideInjectionChallenge2Model.class);
        progressTracker = new ProgressTracker(requireContext());

        // Initialize database
        dbHelper = new Challenge2DatabaseHelper(requireContext());
        initializeDatabase();

        // Setup FAB for vulnerability info
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showVulnerabilityInfo());
        }

        // Setup search button
        binding.searchProductButton.setOnClickListener(v -> searchProducts());

        // Setup submit flag button
        binding.submitFlagButton.setOnClickListener(v -> submitFlag());

        return root;
    }

    private void initializeDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // Clear existing data
        db.execSQL("DELETE FROM products");
        db.execSQL("DELETE FROM secrets");
        
        // Insert sample products
        insertProduct(db, "Laptop", "Electronics", 999.99, 15);
        insertProduct(db, "Mouse", "Electronics", 29.99, 50);
        insertProduct(db, "Keyboard", "Electronics", 79.99, 30);
        insertProduct(db, "Monitor", "Electronics", 299.99, 20);
        insertProduct(db, "Desk", "Furniture", 199.99, 10);
        
        // Insert secret table with hidden flag
        insertSecret(db, "admin_token", "BurpingChimneys");
        insertSecret(db, "api_key", "sk_test_1234567890");
        
        db.close();
    }

    private void insertProduct(SQLiteDatabase db, String name, String category, double price, int stock) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category", category);
        values.put("price", price);
        values.put("stock", stock);
        db.insert("products", null, values);
    }

    private void insertSecret(SQLiteDatabase db, String key, String value) {
        ContentValues values = new ContentValues();
        values.put("secret_key", key);
        values.put("secret_value", value);
        db.insert("secrets", null, values);
    }

    private void searchProducts() {
        String searchTerm = binding.productSearchInput.getText().toString();
        
        if (searchTerm.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        // VULNERABLE: User input concatenated into SQL query - allows UNION-based injection
        String query = "SELECT name, category, price, stock FROM products WHERE name LIKE '%" + searchTerm + "%' OR category LIKE '%" + searchTerm + "%'";
        
        Log.d(TAG, "Executing query: " + query);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(query, null);
            
            StringBuilder results = new StringBuilder();
            int count = 0;
            int columnCount = cursor.getColumnCount();
            
            while (cursor.moveToNext()) {
                count++;
                
                // Handle results dynamically (supports UNION injection)
                for (int i = 0; i < columnCount; i++) {
                    String columnName = cursor.getColumnName(i);
                    String value = cursor.getString(i);
                    
                    results.append(columnName).append(": ").append(value).append("\n");
                    
                    // Check if flag was extracted
                    if (value != null && value.equals("BurpingChimneys")) {
                        results.append("\n🎉 SECRET DISCOVERED!\n");
                        results.append("You've extracted data from the hidden 'secrets' table!\n");
                        results.append("Flag: ").append(value).append("\n\n");
                        results.append("Submit this flag to complete the challenge!\n");
                    }
                }
                results.append("---\n");
            }
            
            if (count == 0) {
                binding.resultText.setText("No products found matching: " + searchTerm);
            } else {
                binding.resultText.setText(results.toString());
            }
            
        } catch (Exception e) {
            binding.resultText.setText("Error: " + e.getMessage() + 
                    "\n\nHint: The secrets table has columns: secret_key, secret_value" +
                    "\nTry using UNION SELECT to query multiple tables!");
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

        if (viewModel.validateFlag(submittedFlag)) {
            progressTracker.markCompleted(FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_2);
            int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_2);
            String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
            
            new AlertDialog.Builder(requireContext())
                    .setTitle("🎉 Success!")
                    .setMessage("Congratulations! You've successfully used UNION-based SQL injection to extract data from the hidden 'secrets' table.\n\nFlag: " + submittedFlag + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
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
        
        introText.setText(R.string.client_side_injection_challenge2_intro);
        vulnerabilitiesText.setText(R.string.client_side_injection_challenge2_vulnerabilities);
        
        hintsSection.setVisibility(View.VISIBLE);
        hintsText.setText(R.string.client_side_injection_challenge2_hints);
        
        bestPracticesSection.setVisibility(View.VISIBLE);
        bestPracticesText.setText(R.string.client_side_injection_challenge2_best_practices);
        
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Client-Side Injection Challenge 2")
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
