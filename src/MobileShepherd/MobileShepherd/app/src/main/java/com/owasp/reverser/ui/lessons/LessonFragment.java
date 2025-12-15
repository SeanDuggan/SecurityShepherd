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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LessonModel lessonModel = new ViewModelProvider(this).get(LessonModel.class);

        binding = FragmentLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Display device information
        final TextView textViewSerial = binding.textViewSerial;
        final TextView textViewBuild = binding.textViewBuild;
        final TextView textViewManufacturer = binding.textViewManufacturer;
        final TextView textViewBrand = binding.textViewBrand;
        final TextView textViewSDK = binding.textViewSDK;

        lessonModel.getSerialText().observe(getViewLifecycleOwner(), text -> 
            textViewSerial.setText("Serial: " + text));
        lessonModel.getModelText().observe(getViewLifecycleOwner(), text -> 
            textViewBuild.setText("Build: " + text));
        lessonModel.getManufacturerText().observe(getViewLifecycleOwner(), text -> 
            textViewManufacturer.setText("Manufacturer: " + text));
        lessonModel.getBrandText().observe(getViewLifecycleOwner(), text -> 
            textViewBrand.setText("Brand: " + text));
        lessonModel.getSDKText().observe(getViewLifecycleOwner(), text -> 
            textViewSDK.setText("SDK: " + text));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

