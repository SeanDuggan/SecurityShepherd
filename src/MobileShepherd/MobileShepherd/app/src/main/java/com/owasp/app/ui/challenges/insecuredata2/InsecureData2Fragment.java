package com.owasp.app.ui.challenges.insecuredata2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.owasp.app.R;
import com.owasp.app.databinding.FragmentInsecureData2Binding;

import java.io.File;

public class InsecureData2Fragment extends Fragment {

    private FragmentInsecureData2Binding binding;
    private InsecureData2Model viewModel;
    private static final String PREFS_NAME = "UserCredentials";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(InsecureData2Model.class);

        binding = FragmentInsecureData2Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Store sensitive data in SharedPreferences
        storeCredentialsInSharedPrefs();

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

    private void storeCredentialsInSharedPrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Store multiple sensitive credentials in plain text
        editor.putString("username", "admin");
        editor.putString("password", "SuperSecret2024!");
        editor.putString("api_key", "sk_live_51HyperSecureApiKey789");
        editor.putString("auth_token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        editor.putString("encryption_key", "AES256-MyVerySecretKey-DoNotShare");
        editor.putString("secret_flag", "MobileSh3ph3rd_Pr3fs_Vu1n");
        editor.putBoolean("is_admin", true);
        editor.putInt("user_id", 1337);
        
        editor.apply();

        // Show the file location to the user
        File prefsFile = new File(requireContext().getApplicationInfo().dataDir + "/shared_prefs/" + PREFS_NAME + ".xml");
        if (prefsFile.exists()) {
            Toast.makeText(getContext(), 
                "SharedPreferences file created at:\n" + prefsFile.getAbsolutePath(), 
                Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
