package com.owasp.app.ui.challenges.insecuredata3;

import androidx.lifecycle.ViewModel;

public class InsecureData3Model extends ViewModel {

    public boolean validateFlag(String flag) {
        // TODO: Replace with actual flag from Security Shepherd server
        // The flag is encrypted with XOR cipher using key "SHEPHERD"
        // Original: "SecureFlag{WeakXOR_Crypto_2024}"
        String correctFlag = "SecureFlag{WeakXOR_Crypto_2024}";
        return flag.equals(correctFlag);
    }
}
