package com.owasp.app.ui.lessons;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentLessonBinding;

public class LessonFragment extends Fragment {

    // TODO: Find this flag using reverse engineering!
    private static final String HIDDEN_FLAG = "FLAG{R3v3rs3_Eng1n33r1ng_M4st3r_2024}";
    
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

        // Setup FAB to show detailed lesson information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showDetailedInfo());
        }

        return root;
    }

    private void showDetailedInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.lesson_intro);
        vulnerabilitiesText.setText(R.string.lesson_tools);
        
        hintsSection.setVisibility(View.VISIBLE);
        hintsText.setText(R.string.lesson_hint);
        
        bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Reverse Engineering")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

