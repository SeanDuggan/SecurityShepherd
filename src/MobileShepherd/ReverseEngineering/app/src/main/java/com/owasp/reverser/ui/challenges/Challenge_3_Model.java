package com.owasp.reverser.ui.challenges;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Challenge_3_Model extends ViewModel {

    private final MutableLiveData<String> mText;

    public Challenge_3_Model() {
        mText = new MutableLiveData<>();
        mText.setValue("This is challenge 3!");
    }

    public LiveData<String> getText() {
        return mText;
    }
}