package com.owasp.app.ui.challenges.reverseengineering;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Challenge_1_Model extends ViewModel {

    private final MutableLiveData<String> mText;
    
    // TODO: Replace this with your actual flag
    private static final String FLAG = "OWASP{Simple_Flag_Easy_To_Find}";

    public Challenge_1_Model() {
        mText = new MutableLiveData<>();
        mText.setValue("Challenge 1");
    }

    public LiveData<String> getText() {
        return mText;
    }
    
    public boolean validateFlag(String input) {
        return FLAG.equals(input);
    }
}