package com.owasp.app.ui.challenges.reverseengineering;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.owasp.app.utils.FlagValidator;

public class ReverseEngineering3Model extends ViewModel {

    private final MutableLiveData<String> mText;

    public ReverseEngineering3Model() {
        mText = new MutableLiveData<>();
        mText.setValue("Challenge 3");
    }

    public LiveData<String> getText() {
        return mText;
    }
    
    public boolean validateFlag(String input) {
        return FlagValidator.validateFlag(FlagValidator.Module.RE_CHALLENGE_3, input);
    }
}