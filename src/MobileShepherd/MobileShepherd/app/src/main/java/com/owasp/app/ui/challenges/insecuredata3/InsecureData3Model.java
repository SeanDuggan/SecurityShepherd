package com.owasp.app.ui.challenges.insecuredata3;

import androidx.lifecycle.ViewModel;

import com.owasp.app.utils.FlagValidator;

public class InsecureData3Model extends ViewModel {

    public boolean validateFlag(String flag) {
        return FlagValidator.validateFlag(FlagValidator.Module.IDS_CHALLENGE_3, flag);
    }
}
