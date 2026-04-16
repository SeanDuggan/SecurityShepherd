package com.owasp.app.ui.challenges.crypto;

import androidx.lifecycle.ViewModel;

import com.owasp.app.utils.FlagValidator;

public class InsufficientCryptoChallengeModel extends ViewModel {

    public boolean validateFlag(String flag) {
        return FlagValidator.validateFlag(FlagValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE, flag);
    }
}
