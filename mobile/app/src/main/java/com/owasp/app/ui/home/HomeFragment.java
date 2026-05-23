package com.owasp.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.owasp.app.MainActivity;
import com.owasp.app.databinding.FragmentHomeBinding;
import com.owasp.app.utils.AuthManager;
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
        updateAuthCard();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh card state when returning to this screen (e.g. after sign-in/out)
        updateAuthCard();
        updateProgressStats();
    }

    private void updateAuthCard() {
        TextView statusText = binding.getRoot().findViewById(com.owasp.app.R.id.home_auth_status_text);
        MaterialButton authButton = binding.getRoot().findViewById(com.owasp.app.R.id.home_auth_button);
        if (statusText == null || authButton == null) return;

        if (AuthManager.isAuthenticated(requireContext())) {
            String username = AuthManager.getUsername(requireContext());
            statusText.setText("Signed in as " + username + " — flags update from server");
            authButton.setText("Sign Out");
            authButton.setOnClickListener(v -> {
                AuthManager.logout(requireContext());
                updateAuthCard();
            });
        } else {
            statusText.setText("Offline mode — sign in for server-validated flags");
            authButton.setText("Sign In to Server");
            authButton.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openAuthDialog();
                }
            });
        }
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
