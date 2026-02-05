package com.owasp.app.utils;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized flag validation system using SHA-256 hashes.
 * Provides secure client-side validation and supports progress tracking.
 */
public class FlagValidator {
    
    private static final String TAG = "FlagValidator";
    
    // Module type constants
    public static final String TYPE_LESSON = "lesson";
    public static final String TYPE_CHALLENGE = "challenge";
    
    // Module identifiers
    public enum Module {
        // Reverse Engineering
        RE_LESSON("re_lesson", TYPE_LESSON),
        RE_CHALLENGE_1("re_challenge_1", TYPE_CHALLENGE),
        RE_CHALLENGE_2("re_challenge_2", TYPE_CHALLENGE),
        RE_CHALLENGE_3("re_challenge_3", TYPE_CHALLENGE),
        
        // Insecure Data Storage
        IDS_LESSON("ids_lesson", TYPE_LESSON),
        IDS_CHALLENGE_1("ids_challenge_1", TYPE_CHALLENGE),
        IDS_CHALLENGE_2("ids_challenge_2", TYPE_CHALLENGE),
        IDS_CHALLENGE_3("ids_challenge_3", TYPE_CHALLENGE),
        
        // Poor Authentication
        POOR_AUTH_LESSON("poor_auth_lesson", TYPE_LESSON),
        POOR_AUTH_CHALLENGE("poor_auth_challenge", TYPE_CHALLENGE),
        
        // Insecure Authorization
        INSECURE_AUTH_LESSON("insecure_auth_lesson", TYPE_LESSON),
        
        // Supply Chain
        SUPPLY_CHAIN_LESSON("supply_chain_lesson", TYPE_LESSON),
        SUPPLY_CHAIN_CHALLENGE("supply_chain_challenge", TYPE_CHALLENGE),
        
        // Insecure Communication
        INSECURE_COMM_LESSON("insecure_comm_lesson", TYPE_LESSON),
        INSECURE_COMM_CHALLENGE("insecure_comm_challenge", TYPE_CHALLENGE),
        
        // Insufficient Cryptography
        INSUFFICIENT_CRYPTO_LESSON("insufficient_crypto_lesson", TYPE_LESSON),
        INSUFFICIENT_CRYPTO_CHALLENGE("insufficient_crypto_challenge", TYPE_CHALLENGE),
        
        // Security Misconfiguration
        SECURITY_MISCONFIG_LESSON("security_misconfig_lesson", TYPE_LESSON),
        SECURITY_MISCONFIG_CHALLENGE_2("security_misconfig_challenge_2", TYPE_CHALLENGE),
        SECURITY_MISCONFIG_CHALLENGE_3("security_misconfig_challenge_3", TYPE_CHALLENGE),
        
        // Input Validation
        INPUT_VALIDATION_LESSON("input_validation_lesson", TYPE_LESSON),
        XSS_CHALLENGE("xss_challenge", TYPE_CHALLENGE),
        
        // Privacy Controls
        PRIVACY_LESSON("privacy_lesson", TYPE_LESSON),
        
        // Client-Side Injection
        CLIENT_SIDE_INJECTION_LESSON("client_side_injection_lesson", TYPE_LESSON),
        CLIENT_SIDE_INJECTION_CHALLENGE_1("client_side_injection_challenge_1", TYPE_CHALLENGE),
        CLIENT_SIDE_INJECTION_CHALLENGE_2("client_side_injection_challenge_2", TYPE_CHALLENGE);
        
        private final String id;
        private final String type;
        
        Module(String id, String type) {
            this.id = id;
            this.type = type;
        }
        
        public String getId() {
            return id;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // SHA-256 hashes of correct flags
    // Generated using updated_flag_generator.py - All flags use OWASP{} format
    private static final Map<Module, String> FLAG_HASHES = new HashMap<Module, String>() {{
        // Lessons (11)
        put(Module.RE_LESSON, "fbccd46515f935306ef8a44b825d66175c80b35f192acddcb26f71e81e78b35c");
        put(Module.IDS_LESSON, "842d417438ce5ba6dcffcb483a8d77e3adae5d415fba59639c579c8bade759ab");
        put(Module.POOR_AUTH_LESSON, "cff1c8e89048560357bb624ef044735ba1537b5fea2e9619f95d0da39ca39491");
        put(Module.INSECURE_AUTH_LESSON, "6ddcf0e5f9981697a2a5e9f132a4f2bfc559124b28abe4c3347e702469f68efd");
        put(Module.SUPPLY_CHAIN_LESSON, "c08e429460e4f67b2a9663c4a192e3ef883c87cf0f3932bd4e3cfdbe4a607ef8");
        put(Module.INSECURE_COMM_LESSON, "18c2d9d0d869931df6e552aad9701939b48608697a298ca3cc2fb0449e4618c4");
        put(Module.INSUFFICIENT_CRYPTO_LESSON, "f1eda51009ceda1b6f8388eaaa11cb44dbc0f1e2f8e9a01574b17ce6d1e3d8f7");
        put(Module.SECURITY_MISCONFIG_LESSON, "005e2350b9ac6be1f970ad7b26900cb7ccca7cd46735e4b48812119c51bde721");
        put(Module.INPUT_VALIDATION_LESSON, "fbb278fed12e3e6dc634aaa6ed7f2b5979a3253fc55d1d186c745250d95734e6");
        put(Module.PRIVACY_LESSON, "9db5cf8547ce25102972c10be78dbec6521c49eec1bbe9d47b29b6fc0255db53");
        put(Module.CLIENT_SIDE_INJECTION_LESSON, "5337f5da347728238d63451bfc8fd971f51b631f48be1d04fa63ae2196d4500d");
        
        // Challenges (15)
        put(Module.RE_CHALLENGE_1, "5e76190266d739de15b8ec7a351882e329d65fc5b1a98a6e133a1b062805d466");
        put(Module.RE_CHALLENGE_2, "f198d69295a51ad7a5e36299d8c00f72162f30e792f2d9da8cb1099ac3081eae");
        put(Module.RE_CHALLENGE_3, "696c851905a9d210bb18d3ab842515f8ac6578768e9a80e3f0a88551762f0948");
        put(Module.IDS_CHALLENGE_1, "32549f450a18f5a4a08fb86b2393580fe65b3ae18158ff0c4bcd941e168f29c8");
        put(Module.IDS_CHALLENGE_2, "09e392b0b1f8902274ad8acf781d8c85e2d4437cb515cc83982710e362cae635");
        put(Module.IDS_CHALLENGE_3, "796f0a00e0a1b452b88aca981ad480fc66539980da80ebd7ffebd1f614ad07e3");
        put(Module.POOR_AUTH_CHALLENGE, "39820fb86ed62c7226b60097483ef1a476e0cd199812db636354720e6a44c74a");
        put(Module.SUPPLY_CHAIN_CHALLENGE, "6619992df3f7214ad1a8d1b50af76445bf573e9fd98acb8d7405d0dc74557bce");
        put(Module.INSECURE_COMM_CHALLENGE, "a6fbb4dd023c497596c616aaa33be7db2cf7416631e7762d7353da223922820a");
        put(Module.INSUFFICIENT_CRYPTO_CHALLENGE, "31ce77aa70f1db687667b639d7f1090c8600b4ae125c0b35e3367b558d6859de");
        put(Module.SECURITY_MISCONFIG_CHALLENGE_2, "db8cff06e77261383860898457ba7528a75d260fea9eee00e6c5d30452e07dc0");
        put(Module.SECURITY_MISCONFIG_CHALLENGE_3, "1403f9726a9c49582c6d87477b1da8279f42d08a567ff3042ea942eb4774cc8c");
        put(Module.XSS_CHALLENGE, "aff5e70fdc1fa14a8fe134bd38e934ad422236eda8aa754620d617886ff6d837");
        put(Module.CLIENT_SIDE_INJECTION_CHALLENGE_1, "823c8d3b24128f1b22931f274741db9ce9150d55a6f4dd94980ccff1b9384641");
        put(Module.CLIENT_SIDE_INJECTION_CHALLENGE_2, "8d150cb8f20fdac04183170b7538846fd7ccd3b42e0b8d443f4095fcf90a6d02");
    }};
    
    /**
     * Validates a flag submission using SHA-256 hash comparison.
     * 
     * @param module The module being validated
     * @param submittedFlag The flag submitted by the user
     * @return true if the flag is correct, false otherwise
     */
    public static boolean validateFlag(Module module, String submittedFlag) {
        if (submittedFlag == null || submittedFlag.trim().isEmpty()) {
            return false;
        }
        
        String expectedHash = FLAG_HASHES.get(module);
        if (expectedHash == null) {
            Log.e(TAG, "No hash found for module: " + module.getId());
            return false;
        }
        
        String submittedHash = sha256(submittedFlag.trim());
        boolean isValid = expectedHash.equalsIgnoreCase(submittedHash);
        
        if (isValid) {
            Log.d(TAG, "✓ Correct flag for " + module.getId());
        } else {
            Log.d(TAG, "✗ Incorrect flag for " + module.getId());
            Log.d(TAG, "Expected: " + expectedHash);
            Log.d(TAG, "Got:      " + submittedHash);
        }
        
        return isValid;
    }
    
    /**
     * Computes SHA-256 hash of the input string.
     * 
     * @param input The string to hash
     * @return Hexadecimal representation of the hash
     */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Convert bytes to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 algorithm not available", e);
            return "";
        }
    }
    
    /**
     * Helper method to generate hash from plaintext (for development/testing).
     * DO NOT use this in production - it's here for generating the hashes above.
     * 
     * Usage: String hash = FlagValidator.generateHash("your_flag_here");
     */
    public static String generateHash(String plaintext) {
        return sha256(plaintext);
    }
}
