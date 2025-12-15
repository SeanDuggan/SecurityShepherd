package com.owasp.reverser.ui.insecuredata;

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
import com.owasp.reverser.R;
import com.owasp.reverser.databinding.FragmentInsecureDataLessonBinding;

import java.io.File;

public class InsecureDataLessonFragment extends Fragment {

    private FragmentInsecureDataLessonBinding binding;
    private SQLiteDatabase membersDb = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInsecureDataLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup FAB for detailed information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showDetailedInfo());
        }

        // Create insecure database for demonstration
        createDatabase();
        insertKey();

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

    private void insertKey() {
        if (membersDb != null) {
            try {
                membersDb.execSQL("DELETE FROM Members;");
                membersDb.execSQL("INSERT INTO Members (name, password) VALUES ('Admin','Battery777');");
            } catch (Exception e) {
                Log.e("DB ERROR", "Error Inserting Key", e);
            }
        }
    }

    private void showDetailedInfo() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        TextView bestPracticesText = dialogView.findViewById(R.id.best_practices_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        TextView additionalTitle = dialogView.findViewById(R.id.additional_title);
        TextView additionalText = dialogView.findViewById(R.id.additional_text);
        
        introText.setText(getString(R.string.insecure_data_intro));
        vulnerabilitiesText.setText(getString(R.string.insecure_data_vulns));
        bestPracticesText.setText(getString(R.string.insecure_data_find));
        
        additionalSection.setVisibility(View.VISIBLE);
        additionalTitle.setText("💾 Common Storage Locations");
        additionalText.setText(getString(R.string.insecure_data_locations));
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("🗄️ Insecure Data Storage");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (membersDb != null) {
            membersDb.close();
        }
        binding = null;
    }
}
