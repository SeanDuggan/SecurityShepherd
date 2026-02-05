package com.owasp.app.ui.challenges.supplychain;

import androidx.lifecycle.ViewModel;

import com.owasp.app.utils.FlagValidator;

public class SupplyChainChallengeModel extends ViewModel {

    public boolean validateFlag(String flag) {
        return FlagValidator.validateFlag(FlagValidator.Module.SUPPLY_CHAIN_CHALLENGE, flag.trim());
    }
}
