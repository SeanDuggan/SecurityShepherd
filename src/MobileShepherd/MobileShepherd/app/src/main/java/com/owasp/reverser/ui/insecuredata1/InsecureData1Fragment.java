package com.owasp.reverser.ui.insecuredata1;

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
import com.owasp.reverser.databinding.FragmentInsecureData1Binding;

import java.io.File;

public class InsecureData1Fragment extends Fragment {

    private FragmentInsecureData1Binding binding;
    private InsecureData1Model viewModel;
    private SQLiteDatabase usersDB = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(InsecureData1Model.class);

        binding = FragmentInsecureData1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Create the vulnerable database
        createDatabase();
        insertData();

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
            usersDB = requireContext().openOrCreateDatabase("Users", android.content.Context.MODE_PRIVATE, null);
            usersDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS Users " +
                            "(id integer primary key, name VARCHAR, password VARCHAR);");

            File database = requireActivity().getApplication().getDatabasePath("Users.db");

            if (database.exists()) {
                Toast.makeText(getContext(), "Database initialized at: " + database.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e("DB ERROR", "Error Creating Database", e);
        }
    }

    private void insertData() {
        if (usersDB != null) {
            usersDB.execSQL("DELETE FROM Users;");

            usersDB.execSQL("INSERT INTO Users (name, password) VALUES ('Tyrkyr','ZG9jaGRvY2hkb2No');");
            usersDB.execSQL("INSERT INTO Users (name, password) VALUES ('ToothBrush','MmNvb2w0dWxvbD8=');");
            usersDB.execSQL("INSERT INTO Users (name, password) VALUES ('TroolMann','QnJpZGdlcw==');");
            usersDB.execSQL("INSERT INTO Users (name, password) VALUES ('Patrick','ZGlub3NhdXI=');");
            usersDB.execSQL("INSERT INTO Users (name, password) VALUES ('bottles','cGFzc3dvcmQxMjM0');");
            usersDB.execSQL("INSERT INTO Users (name, password) VALUES ('Root','V2Fyc2hpcHNBbmRXcmVuY2hlcw==');");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (usersDB != null) {
            usersDB.close();
        }
        binding = null;
    }
}
