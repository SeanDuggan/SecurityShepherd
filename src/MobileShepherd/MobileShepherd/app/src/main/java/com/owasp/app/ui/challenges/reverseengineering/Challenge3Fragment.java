package com.owasp.app.ui.challenges.reverseengineering;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.owasp.app.R;
import com.owasp.app.databinding.FragmentChallenge3Binding;
import com.owasp.app.ui.challenges.reverseengineering.Challenge_3_Model;

public class Challenge3Fragment extends Fragment {

    private FragmentChallenge3Binding binding;
    private Challenge_3_Model challengeModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        challengeModel = new ViewModelProvider(this).get(Challenge_3_Model.class);

        binding = FragmentChallenge3Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final EditText inputFlag = binding.inputFlag;
        final Button btnValidate = binding.btnValidate;
        final Button btnClear = binding.btnClear;
        final TextView textResult = binding.textResult;

        btnValidate.setOnClickListener(v -> {
            String userInput = inputFlag.getText().toString().trim();
            if (challengeModel.validateFlag(userInput)) {
                textResult.setText(R.string.challenge3_success);
                textResult.setTextColor(Color.GREEN);
                textResult.setVisibility(View.VISIBLE);
            } else {
                textResult.setText(R.string.challenge3_failure);
                textResult.setTextColor(Color.RED);
                textResult.setVisibility(View.VISIBLE);
            }
        });

        btnClear.setOnClickListener(v -> {
            inputFlag.setText("");
            textResult.setVisibility(View.GONE);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}