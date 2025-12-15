package com.owasp.reverser.ui.crypto;

import android.os.Bundle;
import android.util.Base64;
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
import com.owasp.reverser.databinding.FragmentInsufficientCryptoLessonBinding;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class InsufficientCryptoLessonFragment extends Fragment {

    private FragmentInsufficientCryptoLessonBinding binding;
    private static final String TAG = "InsufficientCryptoLesson";
    
    // Intentionally using weak DES encryption (56-bit key)
    private static final String WEAK_ALGORITHM = "DES";
    // Hardcoded encryption key (major vulnerability)
    private static final String HARDCODED_KEY = "BadKey01"; // DES requires exactly 8 bytes
    
    // Flag split across multiple parts for obfuscation
    private static final String FLAG_P1 = "OWASP{";
    private static final String FLAG_P2 = "W3ak_";
    private static final String FLAG_P3 = "DES_";
    private static final String FLAG_P4 = "Encryp";
    private static final String FLAG_P5 = "t10n}";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInsufficientCryptoLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup FAB for detailed information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showDetailedInfo());
        }

        // Log showing weak crypto usage
        Log.w(TAG, "WARNING: Application uses deprecated DES encryption");
        Log.w(TAG, "DES key length: 56 bits (insufficient for modern security)");
        Log.w(TAG, "Recommendation: Migrate to AES-256");
        
        // Setup demo buttons
        binding.encryptButton.setOnClickListener(v -> demonstrateWeakEncryption());
        
        binding.showHintButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Hint: Check logcat for encryption implementation details", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Encryption algorithm: " + WEAK_ALGORITHM);
            Log.d(TAG, "Hardcoded key detected: " + HARDCODED_KEY);
            Log.d(TAG, "Key storage: Plaintext in source code (CRITICAL VULNERABILITY)");
        });

        return root;
    }

    private void demonstrateWeakEncryption() {
        String plaintext = binding.plaintextInput.getText().toString().trim();

        if (plaintext.isEmpty()) {
            plaintext = "SecretData123";
            binding.plaintextInput.setText(plaintext);
        }

        try {
            // Demonstrate DES encryption
            Log.d(TAG, "Attempting DES encryption...");
            Log.d(TAG, "Plaintext: " + plaintext);
            Log.d(TAG, "Using hardcoded key: " + HARDCODED_KEY);
            
            // Create DES key
            SecretKey key = new SecretKeySpec(HARDCODED_KEY.getBytes(), WEAK_ALGORITHM);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(WEAK_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            // Encrypt
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());
            String encryptedBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT);
            
            binding.encryptedOutput.setText(encryptedBase64.trim());
            binding.encryptedOutput.setVisibility(View.VISIBLE);
            
            Log.d(TAG, "Encrypted (Base64): " + encryptedBase64.trim());
            Log.w(TAG, "WARNING: Data encrypted with weak DES algorithm");
            Log.w(TAG, "DES is vulnerable to brute-force attacks");
            Log.w(TAG, "Modern GPUs can crack DES in hours");
            
            // Show vulnerability warning
            binding.resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            binding.vulnerabilityWarning.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Data encrypted with weak DES! See logcat for details.", Toast.LENGTH_LONG).show();
            
            // Check if user is trying to validate the flag
            if (plaintext.equals(FLAG_P1 + FLAG_P2 + FLAG_P3 + FLAG_P4 + FLAG_P5)) {
                binding.resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                binding.flagText.setText("✓ Flag Validated!\n\nYou found: " + plaintext);
                binding.flagText.setVisibility(View.VISIBLE);
                Log.d(TAG, "SUCCESS: Flag validated - " + plaintext);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Encryption failed: " + e.getMessage());
            Toast.makeText(getContext(), "Encryption error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        
        introText.setText(getString(R.string.insufficient_crypto_intro));
        vulnerabilitiesText.setText(getString(R.string.insufficient_crypto_vulnerabilities));
        bestPracticesText.setText(getString(R.string.insufficient_crypto_best_practices));
        
        additionalSection.setVisibility(View.VISIBLE);
        additionalTitle.setText("💡 Real-World Impact");
        additionalText.setText(getString(R.string.insufficient_crypto_impact));
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("🔐 Insufficient Cryptography");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
