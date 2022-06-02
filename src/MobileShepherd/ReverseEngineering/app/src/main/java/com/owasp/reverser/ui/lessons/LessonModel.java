package com.owasp.reverser.ui.lessons;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LessonModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public LessonModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the Lesson");
    }

    public LiveData<String> getText() {
        return mText;
    }
}