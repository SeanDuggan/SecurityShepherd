package com.owasp.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.owasp.app.databinding.FragmentHomeBinding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ProgressTracker progressTracker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());
        
        updateProgressStats();

        return root;
    }
    
    private void updateProgressStats() {
        // Count total lessons and challenges
        int totalLessons = 0;
        int totalChallenges = 0;
        
        for (FlagValidator.Module module : FlagValidator.Module.values()) {
            if (module.getType().equals(FlagValidator.TYPE_LESSON)) {
                totalLessons++;
            } else if (module.getType().equals(FlagValidator.TYPE_CHALLENGE)) {
                totalChallenges++;
            }
        }
        
        // Get completed counts
        int completedLessons = progressTracker.getCompletedLessonsCount();
        int completedChallenges = progressTracker.getCompletedChallengesCount();
        
        // Update UI
        TextView lessonsProgress = binding.getRoot().findViewById(com.owasp.app.R.id.lessons_progress);
        TextView challengesProgress = binding.getRoot().findViewById(com.owasp.app.R.id.challenges_progress);
        
        if (lessonsProgress != null) {
            lessonsProgress.setText(completedLessons + " of " + totalLessons + " lessons completed");
        }
        
        if (challengesProgress != null) {
            challengesProgress.setText(completedChallenges + " of " + totalChallenges + " challenges completed");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
