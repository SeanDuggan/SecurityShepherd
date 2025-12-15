package com.owasp.reverser.ui.insecuredata2;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.owasp.reverser.R;
import com.owasp.reverser.databinding.FragmentInsecureData2Binding;

import java.io.File;

public class InsecureData2Fragment extends Fragment {

    private FragmentInsecureData2Binding binding;
    private InsecureData2Model viewModel;
    private SQLiteDatabase passwordDB = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(InsecureData2Model.class);

        binding = FragmentInsecureData2Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
                Toast.makeText(getContext(), "Correct! Flag validated successfully!", Toast.LENGTH_LONG).show();
                binding.resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            } else {
                Toast.makeText(getContext(), "Incorrect flag. Keep looking!", Toast.LENGTH_SHORT).show();
                binding.resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                flagInput.setText("");
            }
        });

        return root;
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
