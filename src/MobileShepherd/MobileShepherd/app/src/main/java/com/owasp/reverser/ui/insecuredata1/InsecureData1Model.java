package com.owasp.reverser.ui.insecuredata1;

import androidx.lifecycle.ViewModel;

public class InsecureData1Model extends ViewModel {

    public boolean validateFlag(String flag) {
        // TODO: Replace with actual flag from Security Shepherd server
        // The flag should be one of the Base64 decoded passwords from the database
        // For example, 'WarshipsAndWrenches' (Root's decoded password)
        String correctFlag = "WarshipsAndWrenches";
        return flag.equals(correctFlag);
    }
}
