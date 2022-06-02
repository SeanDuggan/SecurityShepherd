package com.owasp.reverser.ui.challenges;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Challenge_2_Model extends ViewModel {

    private final MutableLiveData<String> mText;

    public Challenge_2_Model() {
        mText = new MutableLiveData<>();
        mText.setValue("This is challenge 2!");
    }

    public LiveData<String> getText() {
        return mText;
    }
}