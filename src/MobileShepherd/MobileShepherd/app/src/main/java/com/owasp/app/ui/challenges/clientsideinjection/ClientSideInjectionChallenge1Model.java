package com.owasp.app.ui.challenges.clientsideinjection;

import androidx.lifecycle.ViewModel;

import com.owasp.app.utils.FlagValidator;

public class ClientSideInjectionChallenge1Model extends ViewModel {
    
    public boolean validateFlag(String flag) {
        return FlagValidator.validateFlag(
            FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1, 
            flag
        );
    }
}
