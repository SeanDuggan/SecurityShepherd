package com.owasp.app.ui.challenges.insecuredata2;

import androidx.lifecycle.ViewModel;

import com.owasp.app.utils.FlagValidator;

public class InsecureData2Model extends ViewModel {

    public boolean validateFlag(String flag) {
        return FlagValidator.validateFlag(FlagValidator.Module.IDS_CHALLENGE_2, flag);
    }
}
