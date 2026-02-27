package com.owasp.app.ui.challenges.insecuredata3;

import android.os.Bundle;
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
import com.owasp.app.databinding.FragmentInsecureData3Binding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class InsecureData3Fragment extends Fragment {

    private FragmentInsecureData3Binding binding;
    private InsecureData3Model viewModel;
    private ProgressTracker progressTracker;

    // Weak "encryption" implementation - XOR cipher with fixed key
    private static final String XOR_KEY = "SHEPHERD";
    
    // This looks encrypted but uses weak XOR encryption
    private String encryptedFlag = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(InsecureData3Model.class);

        binding = FragmentInsecureData3Binding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Setup FAB for vulnerability information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showVulnerabilityInfo());
        }

        // "Encrypt" the secret data using weak XOR cipher
        String secretData = "SecureFlag{WeakXOR_Crypto_2024}";
        encryptedFlag = xorEncrypt(secretData, XOR_KEY);
        
        // Show the encrypted data to make it look secure
        String encryptedHex = bytesToHex(encryptedFlag.getBytes());
        Toast.makeText(getContext(), 
            "Data encrypted with custom algorithm!\nEncrypted: " + encryptedHex, 
            Toast.LENGTH_LONG).show();

        // Setup validation
        EditText flagInput = binding.flagInput;
        Button validateButton = binding.validateButton;

        validateButton.setOnClickListener(v -> {
            String enteredFlag = flagInput.getText().toString().trim();
            boolean isValid = viewModel.validateFlag(enteredFlag);

            if (isValid) {
                progressTracker.markCompleted(FlagValidator.Module.IDS_CHALLENGE_3);
                int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.IDS_CHALLENGE_3);
                String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
                
                Toast.makeText(getContext(), "Correct! You cracked the encryption!", Toast.LENGTH_LONG).show();
                binding.resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                
                // Show the decrypted value
                String decrypted = xorDecrypt(encryptedFlag, XOR_KEY);
                Toast.makeText(getContext(), "Decrypted value: " + decrypted, Toast.LENGTH_LONG).show();
                
                new AlertDialog.Builder(requireContext())
                    .setTitle("🎉 Success!")
                    .setMessage("Congratulations! You broke the weak XOR encryption.\n\nFlag: " + enteredFlag + completionText + "\n\nProgress: " + progressTracker.getCompletedChallengesCount() + "/" + progressTracker.getTotalChallengesCount() + " challenges completed")
                    .setPositiveButton("OK", null)
                    .show();
            } else {
                Toast.makeText(getContext(), "Incorrect flag. Analyze the encryption!", Toast.LENGTH_SHORT).show();
                binding.resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                flagInput.setText("");
            }
        });

        // Button to show encrypted data again
        binding.showEncryptedButton.setOnClickListener(v -> {
            String hex = bytesToHex(encryptedFlag.getBytes());
            Toast.makeText(getContext(), "Encrypted Data (Hex):\n" + hex, Toast.LENGTH_LONG).show();
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
        
        introText.setText(R.string.insufficient_crypto_intro);
        vulnerabilitiesText.setText(R.string.insufficient_crypto_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        
        bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Insecure Data Storage - Weak Encryption")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    /**
     * Weak XOR encryption - easily reversible
     * This is intentionally insecure for educational purposes
     */
    private String xorEncrypt(String data, String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            result.append((char) (data.charAt(i) ^ key.charAt(i % key.length())));
        }
        return result.toString();
    }

    /**
     * XOR decryption (same as encryption due to XOR properties)
     */
    private String xorDecrypt(String encrypted, String key) {
        return xorEncrypt(encrypted, key); // XOR is symmetric
    }

    /**
     * Convert bytes to hex string for display
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    /**
     * Helper method for students - demonstrates the weakness
     */
    public String getEncryptionInfo() {
        return "Encryption Algorithm: Custom XOR Cipher\n" +
               "Key Length: " + XOR_KEY.length() + " bytes\n" +
               "Encrypted Data Available: Yes\n" +
               "Hint: XOR encryption is symmetric and reversible";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
