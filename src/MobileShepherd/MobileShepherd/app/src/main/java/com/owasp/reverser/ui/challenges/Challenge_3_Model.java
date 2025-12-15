package com.owasp.reverser.ui.challenges;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Challenge_3_Model extends ViewModel {

    private final MutableLiveData<String> mText;
    
    // TODO: Replace these with your actual obfuscated flag parts
    private static final String PART_1 = "OWASP{";
    private static final String PART_2 = "Obfuscated";
    private static final String PART_3 = "_Hard_";
    private static final String PART_4 = "Challenge";
    private static final String PART_5 = "}";

    public Challenge_3_Model() {
        mText = new MutableLiveData<>();
        mText.setValue("Challenge 3");
    }

    public LiveData<String> getText() {
        return mText;
    }
    
    private String constructFlag() {
        // Obfuscated flag construction
        StringBuilder sb = new StringBuilder();
        sb.append(PART_1);
        sb.append(PART_2);
        sb.append(PART_3);
        sb.append(PART_4);
        sb.append(PART_5);
        return sb.toString();
    }
    
    private String xorDecode(String input) {
        // Simple XOR obfuscation - could be enhanced
        return input;
    }
    
    public boolean validateFlag(String input) {
        return constructFlag().equals(input);
    }
}