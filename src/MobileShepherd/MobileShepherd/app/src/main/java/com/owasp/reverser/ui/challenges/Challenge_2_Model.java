package com.owasp.reverser.ui.challenges;

import android.util.Base64;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Challenge_2_Model extends ViewModel {

    private final MutableLiveData<String> mText;
    
    // TODO: Replace this with your actual encoded flag
    // This is Base64 encoded: "OWASP{Base64_Encoded_Flag}"
    private static final String ENCODED_FLAG = "T1dBU1B7QmFzZTY0X0VuY29kZWRfRmxhZ30=";

    public Challenge_2_Model() {
        mText = new MutableLiveData<>();
        mText.setValue("Challenge 2");
    }

    public LiveData<String> getText() {
        return mText;
    }
    
    private String decodeFlag() {
        try {
            byte[] decodedBytes = Base64.decode(ENCODED_FLAG, Base64.DEFAULT);
            return new String(decodedBytes);
        } catch (Exception e) {
            return "";
        }
    }
    
    public boolean validateFlag(String input) {
        return decodeFlag().equals(input);
    }
}