package com.owasp.reverser.ui.challenges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.owasp.reverser.databinding.FragmentChallenge3Binding;
import com.owasp.reverser.ui.challenges.Challenge_3_Model;

public class Challenge3Fragment extends Fragment {

    private FragmentChallenge3Binding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Challenge_3_Model challengeViewModel =
                new ViewModelProvider(this).get(Challenge_3_Model.class);

        binding = FragmentChallenge3Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSlideshow;
        challengeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}