package com.owasp.app.ui.lessons.poorauth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentPoorAuthLessonBinding;

public class PoorAuthLessonFragment extends Fragment {

    private FragmentPoorAuthLessonBinding binding;
    private static final String TAG = "PoorAuthLesson";
    private static final String HARDCODED_PIN = "1234";
    private static final String DEMO_FLAG = "OWASP{H4rdc0d3d_PIN_Vuln}";
    private int attemptCount = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPoorAuthLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup FAB for detailed information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> showDetailedInfo());
        }

        // Log the hardcoded PIN (intentionally insecure for demonstration)
        Log.d(TAG, "Initializing authentication system...");
        Log.d(TAG, "Default PIN configured: " + HARDCODED_PIN);
        Log.d(TAG, "System ready. PIN verification enabled.");

        // Setup demo section
        binding.verifyButton.setOnClickListener(v -> verifyPin());

        return root;
    }

    private void verifyPin() {
        String enteredPin = binding.pinInput.getText().toString().trim();
        attemptCount++;

        if (enteredPin.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "PIN verification attempt #" + attemptCount);
        Log.d(TAG, "Entered PIN: " + enteredPin + " | Expected PIN: " + HARDCODED_PIN);

        if (enteredPin.equals(HARDCODED_PIN)) {
            // Successful authentication
            binding.demoCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            binding.flagText.setText("✓ Authentication Successful!\n\nFlag: " + DEMO_FLAG);
            binding.flagText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Access Granted! Flag revealed!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Authentication successful! Flag: " + DEMO_FLAG);
            
            binding.verifyButton.setEnabled(false);
            binding.pinInput.setEnabled(false);
        } else {
            // Failed authentication
            binding.demoCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            binding.flagText.setVisibility(View.GONE);
            
            Toast.makeText(getContext(), "Access Denied! Incorrect PIN", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Authentication failed. Invalid PIN provided.");
            
            binding.pinInput.setText("");
        }
    }

    private void showDetailedInfo() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(getString(R.string.poor_auth_intro));
        vulnerabilitiesText.setText(getString(R.string.poor_auth_vulnerabilities));
        
        hintsSection.setVisibility(View.VISIBLE);
        hintsText.setText(getString(R.string.poor_auth_lesson_hint));
        
        bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Poor Authentication");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
