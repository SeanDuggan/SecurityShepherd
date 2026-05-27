package com.owasp.app.ui.lessons.crypto;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentInsufficientCryptoLessonBinding;
import com.owasp.app.utils.FlagProvider;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class InsufficientCryptoLessonFragment extends Fragment {

    private FragmentInsufficientCryptoLessonBinding binding;
    private static final String TAG = "InsufficientCryptoLesson";
    
    // Using DES encryption (56-bit key)
    private static final String ALGORITHM = "DES";
    
    // Encrypted secrets (preloaded)
    private List<EncryptedSecret> encryptedSecrets;
    private int userSecretCount = 0;
    
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    private String currentFlag = "";

    // Helper method to pad keys to 8 bytes for DES (DES requires exactly 8-byte keys)
    private static byte[] padKeyTo8Bytes(String key) {
        byte[] keyBytes = new byte[8];
        byte[] inputBytes = key.getBytes();
        System.arraycopy(inputBytes, 0, keyBytes, 0, Math.min(inputBytes.length, 8));
        // Remaining bytes are automatically 0 (null padding)
        return keyBytes;
    }

    private static class EncryptedSecret {
        String label;
        String hint;
        String encryptedValue;
        String decryptedValue;
        
        EncryptedSecret(String label, String hint, String plaintext, String key) {
            this.label = label;
            this.hint = hint;
            this.decryptedValue = null;
            try {
                byte[] paddedKey = padKeyTo8Bytes(key);
                SecretKey secretKey = new SecretKeySpec(paddedKey, "DES");
                Cipher cipher = Cipher.getInstance("DES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] encrypted = cipher.doFinal(plaintext.getBytes());
                this.encryptedValue = Base64.encodeToString(encrypted, Base64.NO_WRAP);
            } catch (Exception e) {
                this.encryptedValue = "ERROR";
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInsufficientCryptoLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Initialise secrets once the flag is available (FlagProvider fires on next tick offline)
        FlagProvider.getFlag(requireContext(), FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON,
                flagValue -> {
                    currentFlag = flagValue;
                    initializeSecrets();
                    displaySecrets();
                });

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
        

        // Set initial FAB appearance based on completion status

        // Setup encrypt/decrypt buttons
        binding.encryptButton.setOnClickListener(v -> encryptText());
        binding.decryptButton.setOnClickListener(v -> decryptText());

        return root;
    }

    private void initializeSecrets() {
        encryptedSecrets = new ArrayList<>();
        // Preload encrypted secrets - each uses a different guessable key (all lowercase)
        encryptedSecrets.add(new EncryptedSecret("Secret 1", "Animal", "Some sheep have gone missing", "sheep"));
        encryptedSecrets.add(new EncryptedSecret("Secret 2", "Role", "Reallocating budget for new staff", "shepherd"));
        encryptedSecrets.add(new EncryptedSecret("Secret 3", "Not Human Food", "Grass is turning blue.", "grass"));
        encryptedSecrets.add(new EncryptedSecret("Secret 4", "Material for warmth", currentFlag, "wool"));
    }

    private void displaySecrets() {
        LinearLayout container = binding.secretsContainer;
        container.removeAllViews();

        for (EncryptedSecret secret : encryptedSecrets) {
            View secretView = createSecretView(secret);
            container.addView(secretView);
        }
    }

    private View createSecretView(EncryptedSecret secret) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 12, 16, 12);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        layout.setLayoutParams(params);
        layout.setBackgroundColor(getResources().getColor(R.color.card_info_bg, null));
        
        // Make the entire layout clickable
        layout.setClickable(true);
        layout.setFocusable(true);
        layout.setOnClickListener(v -> promptToDecryptSecret(secret));
        
        // Label
        TextView labelView = new TextView(requireContext());
        labelView.setText(secret.label);
        labelView.setTextSize(14);
        labelView.setTextColor(getResources().getColor(R.color.card_info_text, null));
        labelView.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(labelView);
        
        // Hint
        TextView hintView = new TextView(requireContext());
        hintView.setText("Hint: " + secret.hint);
        hintView.setTextSize(12);
        hintView.setTextColor(getResources().getColor(R.color.warning_orange, null));
        hintView.setPadding(0, 4, 0, 8);
        layout.addView(hintView);
        
        // Encrypted value
        TextView encryptedView = new TextView(requireContext());
        encryptedView.setText("Encrypted: " + secret.encryptedValue);
        encryptedView.setTextSize(12);
        encryptedView.setTypeface(android.graphics.Typeface.MONOSPACE);
        encryptedView.setPadding(0, 8, 0, 8);
        layout.addView(encryptedView);
        
        // Decrypted value (initially hidden)
        TextView decryptedView = new TextView(requireContext());
        if (secret.decryptedValue != null) {
            decryptedView.setText("Decrypted: " + secret.decryptedValue);
            decryptedView.setTextSize(12);
            decryptedView.setTypeface(android.graphics.Typeface.MONOSPACE);
            decryptedView.setTextColor(getResources().getColor(R.color.success_text, null));
            decryptedView.setVisibility(View.VISIBLE);
        } else {
            decryptedView.setText("Decrypted: ???");
            decryptedView.setTextSize(12);
            decryptedView.setTypeface(android.graphics.Typeface.MONOSPACE);
            decryptedView.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            decryptedView.setVisibility(View.VISIBLE);
        }
        layout.addView(decryptedView);
        
        // Add tap instruction if not yet decrypted
        if (secret.decryptedValue == null) {
            TextView tapHint = new TextView(requireContext());
            tapHint.setText("\u2192 Tap to decrypt");
            tapHint.setTextSize(11);
            tapHint.setTextColor(getResources().getColor(R.color.primary_blue, null));
            tapHint.setPadding(0, 4, 0, 0);
            tapHint.setTypeface(null, android.graphics.Typeface.ITALIC);
            layout.addView(tapHint);
        }
        
        return layout;
    }

    private void promptToDecryptSecret(EncryptedSecret secret) {
        // If already decrypted, just show a message
        if (secret.decryptedValue != null) {
            Toast.makeText(requireContext(), "Already decrypted: " + secret.decryptedValue, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create a dialog to get the decryption key
        View dialogView = LayoutInflater.from(requireContext()).inflate(android.R.layout.select_dialog_item, null);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Decrypt " + secret.label);
        builder.setMessage("Hint: " + secret.hint + "\n\nEnter the decryption key:");
        
        // Create an EditText for key input
        final com.google.android.material.textfield.TextInputEditText input = new com.google.android.material.textfield.TextInputEditText(requireContext());
        input.setHint("Enter key (0-8 characters)");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 20);
        container.addView(input);
        
        builder.setView(container);
        builder.setPositiveButton("Decrypt", (dialog, which) -> {
            String key = input.getText().toString().trim();
            attemptDecryptSecret(secret, key);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void attemptDecryptSecret(EncryptedSecret secret, String key) {
        if (key.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a key", Toast.LENGTH_SHORT).show();
            return;
        }
        
        key = key.toLowerCase();
        
        if (key.length() > 8) {
            Toast.makeText(requireContext(), "Key must be 8 characters or less", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            byte[] paddedKey = padKeyTo8Bytes(key);
            SecretKey secretKey = new SecretKeySpec(paddedKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = Base64.decode(secret.encryptedValue, Base64.NO_WRAP);
            byte[] decrypted = cipher.doFinal(encryptedBytes);
            String decryptedText = new String(decrypted);
            
            // Success! Update the secret
            secret.decryptedValue = decryptedText;
            displaySecrets();
            
            Toast.makeText(requireContext(), "✓ Successfully decrypted!", Toast.LENGTH_SHORT).show();
            
            // Check if user found the flag
            if (decryptedText.contains("KEY{") && decryptedText.contains("}")) {
                progressTracker.markCompleted(FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON);
                FlagValidator.validateFlag(requireContext(), FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON,
                        currentFlag, correct -> android.util.Log.d("CryptoLesson", "Server submission: " + correct));
                Toast.makeText(requireContext(), "✓ Flag found! Lesson complete!", Toast.LENGTH_LONG).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(requireContext(), "✗ Incorrect key. Try again!", Toast.LENGTH_SHORT).show();
        }
    }

    private void encryptText() {
        String plaintext = binding.plaintextInput.getText().toString().trim();
        String key = binding.keyInput.getText().toString().trim().toLowerCase();
        String hint = binding.hintInput.getText().toString().trim();

        if (plaintext.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter text to encrypt", Toast.LENGTH_SHORT).show();
            return;
        }

        if (key.isEmpty()) {
            // No encryption with empty key - just show the plaintext
            binding.cryptoOutput.setText("No encryption (empty key):\n" + plaintext);
            binding.cryptoOutput.setVisibility(View.VISIBLE);
            return;
        }

        if (key.length() > 8) {
            Toast.makeText(requireContext(), "Key must be 8 characters or less", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            byte[] paddedKey = padKeyTo8Bytes(key);
            SecretKey secretKey = new SecretKeySpec(paddedKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());
            String encryptedBase64 = Base64.encodeToString(encrypted, Base64.NO_WRAP);
            
            binding.cryptoOutput.setText("Encrypted:\n" + encryptedBase64);
            binding.cryptoOutput.setVisibility(View.VISIBLE);
            
            // Add to the secrets list
            userSecretCount++;
            String label = "User Secret " + userSecretCount;
            String finalHint = hint.isEmpty() ? "No hint provided" : hint;
            
            EncryptedSecret newSecret = new EncryptedSecret(label, finalHint, plaintext, key);
            encryptedSecrets.add(newSecret);
            displaySecrets();
            
            // Clear inputs for next encryption
            binding.plaintextInput.setText("");
            binding.hintInput.setText("");
            
            Toast.makeText(requireContext(), "Added to encrypted secrets list!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Encryption failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void decryptText() {
        String ciphertext = binding.plaintextInput.getText().toString().trim();
        String key = binding.keyInput.getText().toString().trim().toLowerCase();

        if (ciphertext.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter encrypted text to decrypt", Toast.LENGTH_SHORT).show();
            return;
        }

        if (key.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a key to decrypt", Toast.LENGTH_SHORT).show();
            return;
        }

        if (key.length() > 8) {
            Toast.makeText(requireContext(), "Key must be 8 characters or less", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            byte[] paddedKey = padKeyTo8Bytes(key);
            SecretKey secretKey = new SecretKeySpec(paddedKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = Base64.decode(ciphertext, Base64.NO_WRAP);
            byte[] decrypted = cipher.doFinal(encryptedBytes);
            String decryptedText = new String(decrypted);
            
            binding.cryptoOutput.setText("Decrypted:\n" + decryptedText);
            binding.cryptoOutput.setVisibility(View.VISIBLE);
            
            // Check if user found the flag
            if (decryptedText.contains("KEY{") && decryptedText.contains("}")) {
                progressTracker.markCompleted(FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON);
                FlagValidator.validateFlag(requireContext(), FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON,
                        currentFlag, correct -> android.util.Log.d("CryptoLesson", "Server submission (2): " + correct));
                Toast.makeText(requireContext(), "✓ Flag found! Lesson complete!", Toast.LENGTH_LONG).show();
            }
            
            // Try to decrypt secrets with this key
            tryDecryptSecretsWithKey(key);
            
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Decryption failed: Invalid key or ciphertext", Toast.LENGTH_SHORT).show();
        }
    }

    private void tryDecryptSecretsWithKey(String key) {
        boolean anyDecrypted = false;
        for (EncryptedSecret secret : encryptedSecrets) {
            // Only try to decrypt if not already decrypted
            if (secret.decryptedValue == null) {
                try {
                    byte[] paddedKey = padKeyTo8Bytes(key);
                    SecretKey secretKey = new SecretKeySpec(paddedKey, ALGORITHM);
                    Cipher cipher = Cipher.getInstance(ALGORITHM);
                    cipher.init(Cipher.DECRYPT_MODE, secretKey);
                    
                    byte[] encryptedBytes = Base64.decode(secret.encryptedValue, Base64.NO_WRAP);
                    byte[] decrypted = cipher.doFinal(encryptedBytes);
                    secret.decryptedValue = new String(decrypted);
                    anyDecrypted = true;
                } catch (Exception e) {
                    // Key doesn't work for this secret - that's ok
                }
            }
        }
        if (anyDecrypted) {
            displaySecrets();
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
        TextView moduleBanner = dialogView.findViewById(R.id.module_path_banner);
        if (moduleBanner != null) moduleBanner.setText("com.owasp.insufficient_cryptography");
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(getString(R.string.insufficient_crypto_intro));
        additionalSection.setVisibility(View.GONE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Insufficient Cryptography");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
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
