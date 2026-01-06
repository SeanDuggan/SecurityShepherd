package com.owasp.app.ui.challenges.insecurecomm;

import androidx.lifecycle.ViewModel;

public class InsecureCommChallengeModel extends ViewModel {

    private static final String CORRECT_FLAG = "OWASP{N3tw0rk_Sn1ff3d}";

    public boolean validateFlag(String flag) {
        return CORRECT_FLAG.equals(flag.trim());
    }
}
