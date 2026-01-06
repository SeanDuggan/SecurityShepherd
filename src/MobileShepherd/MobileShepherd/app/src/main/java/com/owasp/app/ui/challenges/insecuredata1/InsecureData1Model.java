package com.owasp.app.ui.challenges.insecuredata1;

import androidx.lifecycle.ViewModel;

public class InsecureData1Model extends ViewModel {

    public boolean validateFlag(String flag) {
        // TODO: Replace with actual flag from Security Shepherd server
        // The flag should be the plaintext password that produces the MD5 hash
        // Hash: 0e3a0c8c3a571a855c958813d9b851a1
        // This is the MD5 hash of "letmein2024"
        String correctFlag = "letmein2024";
        return flag.equals(correctFlag);
    }
}
