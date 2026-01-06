package com.owasp.app.ui.challenges.crypto;

import androidx.lifecycle.ViewModel;

public class InsufficientCryptoChallengeModel extends ViewModel {

    // Flag obfuscated in byte arrays (same as in Fragment)
    private static final byte[] F1 = {79, 87, 65, 83, 80, 123}; // OWASP{
    private static final byte[] F2 = {69, 67, 66, 95, 77}; // ECB_M
    private static final byte[] F3 = {48, 100, 51, 95}; // 0d3_
    private static final byte[] F4 = {86, 117, 108, 110}; // Vuln
    private static final byte[] F5 = {51, 114, 52, 98, 108, 51, 125}; // 3r4bl3}

    public boolean validateFlag(String flag) {
        String correctFlag = buildFlag();
        return flag.equals(correctFlag);
    }

    private String buildFlag() {
        StringBuilder flag = new StringBuilder();
        
        for (byte b : F1) flag.append((char) b);
        for (byte b : F2) flag.append((char) b);
        for (byte b : F3) flag.append((char) b);
        for (byte b : F4) flag.append((char) b);
        for (byte b : F5) flag.append((char) b);
        
        return flag.toString();
    }
}
