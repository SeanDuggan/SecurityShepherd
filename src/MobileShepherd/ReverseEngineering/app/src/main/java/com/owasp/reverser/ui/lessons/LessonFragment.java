package com.owasp.reverser.ui.lessons;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.owasp.reverser.databinding.FragmentLessonBinding;

public class LessonFragment extends Fragment {

    private FragmentLessonBinding binding;
    static String Serial = Build.SERIAL;
    static String Model = Build.MODEL;
    static String ID = "test";
    static String Manufacturer = Build.MANUFACTURER;
    static String Brand = Build.BRAND;
    static String Type = Build.TYPE;
    static String User = Build.USER;
   // static int Base = Build.VERSION_CODES.BASE;
    static String Incremental = Build.VERSION.INCREMENTAL;
    static String SDK = Build.VERSION.SDK;
    static String Board = Build.BOARD;
    static String Host = Build.HOST;
    static String Fingerprint = Build.FINGERPRINT;
    static String Release = Build.VERSION.RELEASE;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LessonModel LessonModel = new ViewModelProvider(this).get(LessonModel.class);

        binding = FragmentLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textViewSerial = binding.textViewSerial;
        final TextView textViewBuild = binding.textViewBuild;
        final TextView textViewID = binding.textViewID;
        final TextView textViewManufacturer = binding.textViewManufacturer;
        final TextView textViewBrand = binding.textViewBrand;
        final TextView textViewType = binding.textViewType;
        final TextView textViewUser = binding.textViewUser;
        //final TextView textViewBase = binding.textViewBase;
        final TextView textViewIncremental = binding.textViewIncremental;
        final TextView textViewSDK = binding.textViewSDK;
        final TextView textViewBoard = binding.textViewBoard;
        final TextView textViewHost = binding.textViewHost;
        final TextView textViewFingerprint = binding.textViewFingerprint;
        final TextView textViewRelease = binding.textViewRelease;


        LessonModel.getSerialText().observe(getViewLifecycleOwner(), textViewSerial::setText);
        LessonModel.getModelText().observe(getViewLifecycleOwner(), textViewBuild::setText);
        LessonModel.getIDText().observe(getViewLifecycleOwner(), textViewID::setText);
        LessonModel.getManufacturerText().observe(getViewLifecycleOwner(), textViewManufacturer::setText);
        LessonModel.getBrandText().observe(getViewLifecycleOwner(), textViewBrand::setText);
        LessonModel.getTypeText().observe(getViewLifecycleOwner(), textViewType::setText);
        LessonModel.getUserText().observe(getViewLifecycleOwner(), textViewUser::setText);
        //LessonModel.getBaseText().observe(getViewLifecycleOwner(), textViewBase::setText);
        LessonModel.getIncrementalText().observe(getViewLifecycleOwner(), textViewIncremental::setText);
        LessonModel.getSDKText().observe(getViewLifecycleOwner(), textViewSDK::setText);
        LessonModel.getBoardText().observe(getViewLifecycleOwner(), textViewBoard::setText);
        LessonModel.getHostText().observe(getViewLifecycleOwner(), textViewHost::setText);
        LessonModel.getFingerprintText().observe(getViewLifecycleOwner(), textViewFingerprint::setText);
        LessonModel.getReleaseText().observe(getViewLifecycleOwner(), textViewRelease::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}

