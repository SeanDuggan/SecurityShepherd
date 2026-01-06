package com.owasp.app.ui.challenges.supplychain;

import androidx.lifecycle.ViewModel;

public class SupplyChainChallengeModel extends ViewModel {

    private static final String CORRECT_FLAG = "OWASP{Suppl7_Ch41n_C0mpr0m1s3d}";

    public boolean validateFlag(String flag) {
        return CORRECT_FLAG.equals(flag.trim());
    }
}
