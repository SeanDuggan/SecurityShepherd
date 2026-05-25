package com.owasp.app.ui.lessons;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentLessonBinding;
import com.owasp.app.utils.FlagProvider;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class LessonFragment extends Fragment {

    private String currentFlag = "";
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

        // Setup key verification
        TextInputEditText keyInput = binding.keyInput;
        Button verifyButton = binding.verifyKeyButton;
        ProgressTracker progressTracker = new ProgressTracker(requireContext());
        FlagProvider.getFlag(requireContext(), FlagValidator.Module.RE_LESSON,
                flagValue -> currentFlag = flagValue);

        verifyButton.setOnClickListener(v -> {
            String enteredKey = keyInput.getText() != null ? keyInput.getText().toString().trim() : "";
            
            if (enteredKey.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a key", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (enteredKey.equals(currentFlag)) {
                // Correct key!
                progressTracker.markCompleted(FlagValidator.Module.RE_LESSON);
                FlagValidator.validateFlag(requireContext(), FlagValidator.Module.RE_LESSON,
                        enteredKey, correct -> android.util.Log.d("RELesson", "Server submission: " + correct));
                Toast.makeText(requireContext(), "✓ Correct! Lesson marked as complete!", Toast.LENGTH_LONG).show();
                keyInput.setText("");
            } else {
                // Incorrect key
                Toast.makeText(requireContext(), "✗ Incorrect key. Try again!", Toast.LENGTH_SHORT).show();
            }
        });

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
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.lesson_intro);
        // vulnerabilitiesText.setText(R.string.lesson_tools);
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
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

