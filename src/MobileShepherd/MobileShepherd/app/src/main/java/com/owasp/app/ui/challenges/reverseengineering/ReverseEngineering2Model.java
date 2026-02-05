package com.owasp.app.ui.challenges.reverseengineering;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.owasp.app.utils.FlagValidator;

public class ReverseEngineering2Model extends ViewModel {

    private final MutableLiveData<String> mText;

    public ReverseEngineering2Model() {
        mText = new MutableLiveData<>();
        mText.setValue("Challenge 2");
    }

    public LiveData<String> getText() {
        return mText;
    }
    
    public boolean validateFlag(String input) {
        return FlagValidator.validateFlag(FlagValidator.Module.RE_CHALLENGE_2, input);
    }
}