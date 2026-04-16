package com.owasp.app.ui.challenges.insecurecomm;

import androidx.lifecycle.ViewModel;

import com.owasp.app.utils.FlagValidator;

public class InsecureCommChallengeModel extends ViewModel {

    public boolean validateFlag(String flag) {
        return FlagValidator.validateFlag(FlagValidator.Module.INSECURE_COMM_CHALLENGE, flag.trim());
    }
}
