package com.owasp.app.ui.challenges.poorauth;

import androidx.lifecycle.ViewModel;

public class PoorAuthChallengeModel extends ViewModel {

    private static final String FLAG = "OWASP{P00r_Auth_Weak_Questions_2024}";

    public String getFlag() {
        return FLAG;
    }

    public boolean validateFlag(String enteredFlag) {
        // TODO: Replace with actual flag from Security Shepherd server
        return enteredFlag.equals(FLAG);
    }
}
