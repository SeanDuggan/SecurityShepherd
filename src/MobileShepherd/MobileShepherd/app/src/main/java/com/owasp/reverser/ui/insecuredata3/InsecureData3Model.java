package com.owasp.reverser.ui.insecuredata3;

import androidx.lifecycle.ViewModel;

public class InsecureData3Model extends ViewModel {

    public boolean validateFlag(String flag) {
        // TODO: Replace with actual flag from Security Shepherd server
        // The flag is stored in SharedPreferences under key "secret_flag"
        String correctFlag = "MobileSh3ph3rd_Pr3fs_Vu1n";
        return flag.equals(correctFlag);
    }
}
