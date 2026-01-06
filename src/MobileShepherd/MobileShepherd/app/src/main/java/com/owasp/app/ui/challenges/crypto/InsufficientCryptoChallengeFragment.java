package com.owasp.app.ui.challenges.crypto;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.owasp.app.databinding.FragmentInsufficientCryptoChallengeBinding;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

public class InsufficientCryptoChallengeFragment extends Fragment {

    private FragmentInsufficientCryptoChallengeBinding binding;
    private InsufficientCryptoChallengeModel viewModel;
    private static final String TAG = "InsufficientCryptoChallenge";
    
    // Multiple crypto vulnerabilities demonstrated
    
    // Vulnerability 1: Using ECB mode (patterns visible in ciphertext)
    private static final String WEAK_CIPHER = "AES/ECB/PKCS5Padding";
    
    // Vulnerability 2: Weak key derivation (simple MD5 hash of password)
    private static final String WEAK_PASSWORD = "admin123";
    
    // Vulnerability 3: Using insecure Random instead of SecureRandom
    private static final Random INSECURE_RANDOM = new Random(12345); // Fixed seed!
    
    // Vulnerability 4: Static IV (when used with CBC mode elsewhere)
    private static final byte[] STATIC_IV = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    
    // Flag stored in obfuscated byte arrays
    private static final byte[] ENC_PART1 = {79, 87, 65, 83, 80, 123}; // OWASP{
    private static final byte[] ENC_PART2 = {69, 67, 66, 95, 77}; // ECB_M
    private static final byte[] ENC_PART3 = {48, 100, 51, 95}; // 0d3_
    private static final byte[] ENC_PART4 = {86, 117, 108, 110}; // Vuln
    private static final byte[] ENC_PART5 = {51, 114, 52, 98, 108, 51, 125}; // 3r4bl3}

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInsufficientCryptoChallengeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        viewModel = new ViewModelProvider(this).get(InsufficientCryptoChallengeModel.class);

        // Log crypto implementation details
        Log.d(TAG, "=== Cryptographic Implementation Details ===");
        Log.d(TAG, "Cipher suite: " + WEAK_CIPHER);
        Log.d(TAG, "WARNING: Using ECB mode (Electronic Codebook)");
        Log.d(TAG, "ECB vulnerability: Identical plaintext blocks produce identical ciphertext blocks");
        Log.d(TAG, "Key derivation: MD5 hash of password");
        Log.d(TAG, "WARNING: MD5 is cryptographically broken");
        
        // Simulate app initialization
        initializeCryptoSystem();
        
        binding.analyzeButton.setOnClickListener(v -> analyzeCryptoVulnerabilities());
        binding.validateButton.setOnClickListener(v -> validateFlag());
        
        binding.showHintButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Analyze logcat for crypto implementation flaws", Toast.LENGTH_LONG).show();
            Log.d(TAG, "HINT: Check encryption mode, key derivation, and IV usage");
            Log.d(TAG, "HINT: Password used for key derivation: " + WEAK_PASSWORD);
            Log.d(TAG, "HINT: Random seed: 12345 (predictable!)");
        });

        return root;
    }

    private void initializeCryptoSystem() {
        try {
            Log.d(TAG, "Initializing crypto system...");
            
            // Demonstrate weak key derivation
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] weakKey = md.digest(WEAK_PASSWORD.getBytes());
            String weakKeyHex = bytesToHex(weakKey);
            
            Log.d(TAG, "Deriving encryption key from password: " + WEAK_PASSWORD);
            Log.d(TAG, "Using MD5 for key derivation (VULNERABLE)");
            Log.d(TAG, "Derived key (hex): " + weakKeyHex);
            Log.w(TAG, "CRITICAL: MD5 collisions can be generated, compromising key security");
            
            // Demonstrate insecure random
            Log.d(TAG, "Random number generator: java.util.Random with fixed seed");
            Log.d(TAG, "Next 'random' values: " + INSECURE_RANDOM.nextInt() + ", " + INSECURE_RANDOM.nextInt());
            Log.w(TAG, "CRITICAL: Predictable random numbers compromise cryptographic security");
            
            // Demonstrate static IV
            Log.d(TAG, "Initialization Vector (IV): " + bytesToHex(STATIC_IV));
            Log.w(TAG, "CRITICAL: Static IV reuse allows pattern analysis attacks");
            
            // Show encrypted sample data
            demonstrateECBWeakness();
            
        } catch (Exception e) {
            Log.e(TAG, "Crypto initialization error: " + e.getMessage());
        }
    }

    private void demonstrateECBWeakness() {
        try {
            Log.d(TAG, "\n=== Demonstrating ECB Mode Weakness ===");
            
            // Encrypt repeated data to show ECB pattern vulnerability
            String repeatedData = "AAAAAAAAAAAAAAAA"; // 16 bytes - one AES block
            
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] keyBytes = md.digest(WEAK_PASSWORD.getBytes());
            SecretKey key = new SecretKeySpec(keyBytes, "AES");
            
            Cipher cipher = Cipher.getInstance(WEAK_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            byte[] encrypted1 = cipher.doFinal(repeatedData.getBytes());
            byte[] encrypted2 = cipher.doFinal(repeatedData.getBytes());
            
            Log.d(TAG, "Plaintext: " + repeatedData);
            Log.d(TAG, "Encrypted (1st): " + Base64.encodeToString(encrypted1, Base64.NO_WRAP));
            Log.d(TAG, "Encrypted (2nd): " + Base64.encodeToString(encrypted2, Base64.NO_WRAP));
            
            if (Base64.encodeToString(encrypted1, Base64.NO_WRAP).equals(Base64.encodeToString(encrypted2, Base64.NO_WRAP))) {
                Log.w(TAG, "ECB VULNERABILITY: Identical plaintext produces identical ciphertext!");
                Log.w(TAG, "This reveals patterns in encrypted data");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "ECB demo error: " + e.getMessage());
        }
    }

    private void analyzeCryptoVulnerabilities() {
        Log.d(TAG, "\n=== Vulnerability Analysis Requested ===");
        
        int vulnCount = 0;
        StringBuilder analysis = new StringBuilder();
        
        analysis.append("Identified Vulnerabilities:\n\n");
        
        // Vulnerability 1: ECB Mode
        vulnCount++;
        analysis.append("✗ ECB Mode Usage\n");
        analysis.append("  Impact: Pattern analysis attacks\n");
        analysis.append("  Fix: Use CBC or GCM mode\n\n");
        Log.d(TAG, "Vulnerability 1: ECB mode - patterns visible");
        
        // Vulnerability 2: Weak Key Derivation
        vulnCount++;
        analysis.append("✗ MD5 Key Derivation\n");
        analysis.append("  Impact: Weak key generation\n");
        analysis.append("  Fix: Use PBKDF2, bcrypt, or scrypt\n\n");
        Log.d(TAG, "Vulnerability 2: MD5 for key derivation");
        
        // Vulnerability 3: Insecure Random
        vulnCount++;
        analysis.append("✗ Predictable Random Numbers\n");
        analysis.append("  Impact: Cryptographic values guessable\n");
        analysis.append("  Fix: Use SecureRandom\n\n");
        Log.d(TAG, "Vulnerability 3: Fixed seed Random() instead of SecureRandom");
        
        // Vulnerability 4: Static IV
        vulnCount++;
        analysis.append("✗ Static IV Reuse\n");
        analysis.append("  Impact: Pattern exposure\n");
        analysis.append("  Fix: Generate random IV per encryption\n\n");
        Log.d(TAG, "Vulnerability 4: Static IV allows correlation attacks");
        
        analysis.append("Total: ").append(vulnCount).append(" critical vulnerabilities\n");
        analysis.append("\nRecommendation: Migrate to AES-256-GCM with proper key management");
        
        binding.analysisResult.setText(analysis.toString());
        binding.analysisResult.setVisibility(View.VISIBLE);
        binding.analysisCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        
        Log.d(TAG, "Analysis complete: " + vulnCount + " vulnerabilities identified");
        
        Toast.makeText(getContext(), vulnCount + " crypto vulnerabilities found! Check details above.", Toast.LENGTH_LONG).show();
    }

    private void validateFlag() {
        String enteredFlag = binding.flagInput.getText().toString().trim();

        if (enteredFlag.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a flag", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Flag validation attempt: " + enteredFlag);

        if (viewModel.validateFlag(enteredFlag)) {
            binding.resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            binding.resultText.setText("✓ SUCCESS!\n\nYou identified all the cryptographic vulnerabilities and extracted the hidden flag!");
            binding.resultText.setVisibility(View.VISIBLE);
            
            binding.validateButton.setEnabled(false);
            binding.flagInput.setEnabled(false);
            
            Toast.makeText(getContext(), "Congratulations! Challenge completed!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "SUCCESS: Challenge solved! Flag validated.");
        } else {
            binding.resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            binding.resultText.setText("✗ Incorrect flag. Keep analyzing the crypto implementation!");
            binding.resultText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Incorrect flag", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Flag validation failed");
            
            binding.flagInput.setText("");
        }
    }

    private String reconstructFlag() {
        // Helper method to reconstruct flag from byte arrays
        StringBuilder flag = new StringBuilder();
        
        for (byte b : ENC_PART1) flag.append((char) b);
        for (byte b : ENC_PART2) flag.append((char) b);
        for (byte b : ENC_PART3) flag.append((char) b);
        for (byte b : ENC_PART4) flag.append((char) b);
        for (byte b : ENC_PART5) flag.append((char) b);
        
        return flag.toString();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
