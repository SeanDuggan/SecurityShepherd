package com.owasp.app.ui.lessons.crypto;

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
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentInsufficientCryptoLessonBinding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import android.content.Intent;
import android.net.Uri;

public class InsufficientCryptoLessonFragment extends Fragment {

    private FragmentInsufficientCryptoLessonBinding binding;
    private static final String TAG = "InsufficientCryptoLesson";
    
    // Intentionally using weak DES encryption (56-bit key)
    private static final String WEAK_ALGORITHM = "DES";
    // Hardcoded encryption key (major vulnerability)
    private static final String HARDCODED_KEY = "BadKey01"; // DES requires exactly 8 bytes
    
    // Flag split across multiple parts for obfuscation
    private static final String FLAG_P1 = "KEY{";
    private static final String FLAG_P2 = "W3ak_";
    private static final String FLAG_P3 = "DES_";
    private static final String FLAG_P4 = "Encryp";
    private static final String FLAG_P5 = "t10n}";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInsufficientCryptoLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Setup FAB expansion
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

        // Log showing weak crypto usage
        Log.w(TAG, "WARNING: Application uses deprecated DES encryption");
        Log.w(TAG, "DES key length: 56 bits (insufficient for modern security)");
        Log.w(TAG, "Recommendation: Migrate to AES-256");
        
        // Setup demo buttons
        binding.encryptButton.setOnClickListener(v -> demonstrateWeakEncryption());

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
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m6-insufficient-cryptography";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void showDetailedInfo() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(getString(R.string.insufficient_crypto_intro));
        // vulnerabilitiesText.setText(getString(R.string.insufficient_crypto_vulnerabilities));
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON);
        String buttonText = isCompleted ? "Mark as Incomplete" : "Mark as Complete";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Insufficient Cryptography");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.setNeutralButton(buttonText, (d, which) -> {
            boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON);
            String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
        binding = null;
    }
}
