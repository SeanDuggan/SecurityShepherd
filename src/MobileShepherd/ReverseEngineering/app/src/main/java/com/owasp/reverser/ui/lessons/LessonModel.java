package com.owasp.reverser.ui.lessons;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LessonModel extends ViewModel {

    private final MutableLiveData<String> modelText;
    private final MutableLiveData<String> serialText;
    private final MutableLiveData<String> idText;
    private final MutableLiveData<String> manufacturerText;
    private final MutableLiveData<String> brandText;
    private final MutableLiveData<String> typeText;
    private final MutableLiveData<String> userText;
    private final MutableLiveData<Integer> baseText;
    private final MutableLiveData<String> incrementalText;
    private final MutableLiveData<String> sdkText;
    private final MutableLiveData<String> boardText;
    private final MutableLiveData<String> hostText;
    private final MutableLiveData<String> fingerprintText;
    private final MutableLiveData<String> releaseText;


    public LessonModel() {
        serialText = new MutableLiveData<>();
        modelText = new MutableLiveData<>();
        idText = new MutableLiveData<>();
        manufacturerText = new MutableLiveData<>();
        brandText = new MutableLiveData<>();
        typeText  = new MutableLiveData<>();
        userText = new MutableLiveData<>();
        baseText = new MutableLiveData<>();
        incrementalText = new MutableLiveData<>();
        sdkText = new MutableLiveData<>();
        boardText= new MutableLiveData<>();
        hostText = new MutableLiveData<>();
        fingerprintText = new MutableLiveData<>();
        releaseText= new MutableLiveData<>();


        String serial = LessonFragment.Serial;
        String model = LessonFragment.Model;
        String id = LessonFragment.ID;
        String manufacturer = LessonFragment.Manufacturer;
        String brand = LessonFragment.Brand;
        String type = LessonFragment.Type;
        String user = LessonFragment.User;
        //Integer base = LessonFragment.Base;
        String incremental = LessonFragment.Incremental;
        String sdk = LessonFragment.SDK;
        String board = LessonFragment.Board;
        String host = LessonFragment.Host;
        String fingerprint = LessonFragment.Fingerprint;
        String release = LessonFragment.Release;



        serialText.setValue(serial);
        modelText.setValue(model);
        idText.setValue(id);
        manufacturerText.setValue(manufacturer);
        brandText.setValue(brand);
        typeText.setValue(type);
        userText.setValue(user);
        //baseText.setValue(base);
        incrementalText.setValue(incremental);
        sdkText.setValue(sdk);
        boardText.setValue(board);
        hostText.setValue(host);
        fingerprintText.setValue(fingerprint);
        releaseText.setValue(release);
    }

    public LiveData<String> getSerialText() {

        return serialText;
    }

    public LiveData<String> getModelText() {

        return modelText;
    }

    public LiveData<String> getIDText() {

        return idText;
    }

    public LiveData<String> getManufacturerText() {

        return manufacturerText;
    }

    public LiveData<String> getBrandText() {
        return brandText;
    }

    public LiveData<String> getTypeText() {
        return typeText;
    }

    public LiveData<String> getUserText() {
        return userText;
    }

    public LiveData<Integer> getBaseText() {
        return baseText;
    }

    public LiveData<String> getIncrementalText() {
        return incrementalText;
    }

    public LiveData<String> getSDKText() {
        return sdkText;
    }

    public LiveData<String> getBoardText() {
        return boardText;
    }

    public LiveData<String> getHostText() {
        return hostText;
    }

    public LiveData<String> getFingerprintText() {
        return fingerprintText;
    }

    public LiveData<String> getReleaseText() {
        return releaseText;
    }



}