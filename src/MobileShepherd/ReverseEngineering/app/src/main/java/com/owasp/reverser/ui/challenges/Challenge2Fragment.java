package com.owasp.reverser.ui.challenges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.owasp.reverser.databinding.FragmentChallenge2Binding;
import com.owasp.reverser.ui.challenges.Challenge_2_Model;

public class Challenge2Fragment extends Fragment {

    private FragmentChallenge2Binding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Challenge_2_Model challengeModel =
                new ViewModelProvider(this).get(Challenge_2_Model.class);

        binding = FragmentChallenge2Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textChallenge2;
        challengeModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}