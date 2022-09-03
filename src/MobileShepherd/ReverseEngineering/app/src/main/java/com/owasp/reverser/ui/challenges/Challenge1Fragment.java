package com.owasp.reverser.ui.challenges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.owasp.reverser.databinding.FragmentChallenge1Binding;
import com.owasp.reverser.ui.challenges.Challenge_1_Model;

public class Challenge1Fragment extends Fragment {

    private FragmentChallenge1Binding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Challenge_1_Model challengeModel =
                new ViewModelProvider(this).get(Challenge_1_Model.class);

        binding = FragmentChallenge1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textBuild;
        challengeModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}