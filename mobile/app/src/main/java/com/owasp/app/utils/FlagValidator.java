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
    // Generated using updated_flag_generator.py - All flags use KEY{} format
    private static final Map<Module, String> FLAG_HASHES = new HashMap<Module, String>() {{
        // Lessons (11)
        put(Module.RE_LESSON, "92320bb1b921fbf0069190e4278c9eaa70a20cf959cdf1023ac284b8590c0442");
        put(Module.IDS_LESSON, "25e272fee96a02ee9c76debf4c5d9729bbbfd8b7f3ceca6b5a68f7217a9b5083");
        put(Module.POOR_AUTH_LESSON, "a41e2b63174f869f6cbcc2170d5027f37686fbdab44dce65747cdb878254c66a");
        put(Module.INSECURE_AUTH_LESSON, "2efe8cef55f8d57015fe7bebb24b83aefbc4a0493e8c664ad27cb7489a870eb5");
        put(Module.SUPPLY_CHAIN_LESSON, "739ce3ad0ac005fa38217ede45ab070932024b011b5fe5c0cbb41ef55dc2d03d");
        put(Module.INSECURE_COMM_LESSON, "3be3cd78ecb04af3f1ac5c82569faf0fd86ddbe9f4cfd44ad3368d88e05d7ea2");
        put(Module.INSUFFICIENT_CRYPTO_LESSON, "637dac318ce0fb277f8fde0edb9b33b55e9a0737aaceed1022bd9cfb8162bb80");
        put(Module.SECURITY_MISCONFIG_LESSON, "aaa7eb57d471bf3ee33592ae2194788ef87b1c27c32aece710b55c8487942bee");
        put(Module.INPUT_VALIDATION_LESSON, "777cc0c09a611929f1f310259231bbadf0eaebdc23b2453fb05075c19122a6b7");
        put(Module.PRIVACY_LESSON, "aea1550ffa9752f8b49e64b7782871d0f83df65b8521c9d75206b0d621239577");
        put(Module.CLIENT_SIDE_INJECTION_LESSON, "72ed8033f537426a5dd8d42b230d789ebd778b9f46136e866f99b806868497dc");
        
        // Challenges (15)
        put(Module.RE_CHALLENGE_1, "a42c2c442a38ff350d224718708d10bd4753d013a46677e5685a79b537d7e515");
        put(Module.RE_CHALLENGE_2, "7c54017efb81a47a6d2c9de537fb93362272a66203a3a7fa093c17c84cd44f33");
        put(Module.RE_CHALLENGE_3, "a436be654087eff5f4a272fd39f0ff32fbf1ca2e45341747bb1003976b8254d7");
        put(Module.IDS_CHALLENGE_1, "ee70d2426a09763f99099fd74a475ec79c012dfd0332c9a77caddc36f51686e0");
        put(Module.IDS_CHALLENGE_2, "5f9970d9fc4d69e636e102b0c6270c387d73bdac05beb9283eefbdd84240e347");
        put(Module.POOR_AUTH_CHALLENGE, "8774f054e96aa2fb4ab692c6008467e01f2c5dc19ddb4174c0cefde68afd03f6");
        put(Module.SUPPLY_CHAIN_CHALLENGE, "739ce3ad0ac005fa38217ede45ab070932024b011b5fe5c0cbb41ef55dc2d03d");
        put(Module.INSECURE_COMM_CHALLENGE, "902da64e328c69e90637dfdb1a1a9b00ac9ca59a765d9d8b34c5138fc78154be");
        put(Module.INSUFFICIENT_CRYPTO_CHALLENGE, "6db91af881848682f983cba5b0057350a3bec0260095acd2d836f0196ca43762");
        put(Module.SECURITY_MISCONFIG_CHALLENGE_2, "3d65ad9434f0c9d859f7b31a76dbf34030c6c9d2b3556cb85dfb82dcb7ba50c9");
        put(Module.SECURITY_MISCONFIG_CHALLENGE_3, "e54256b5c0dbf110afe1ebf9cc0e71f214579bc8e5b6fc9d15aee0c81ddde7f0");
        put(Module.XSS_CHALLENGE, "9bb87d8849b4bf7160d704f469e87c5028bc5a0058f2495ef6c0d6d5b16a87b0");
        put(Module.CLIENT_SIDE_INJECTION_CHALLENGE_1, "47a667b98924268439ad607442a3eb558641c4484d6b781690547c912d17fcdd");
        put(Module.CLIENT_SIDE_INJECTION_CHALLENGE_2, "cae83b66f0b1761894682ece0432c24c945c2d578da735a1aeac8060e45a4b71");
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
