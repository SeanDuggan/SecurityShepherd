package com.owasp.app.ui.lessons.insecureauthorization;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.owasp.app.R;
import com.owasp.app.databinding.FragmentInsecureAuthorizationLessonBinding;

public class InsecureAuthorizationLessonFragment extends Fragment {

    private FragmentInsecureAuthorizationLessonBinding binding;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "UserSession";
    
    // Demo credentials
    private static final String DEMO_USERNAME = "testuser";
    private static final String DEMO_PASSWORD = "password123";
    
    // The flag that should only be accessible to admins
    private static final String ADMIN_FLAG = "Shepherd{Pr1v1l3g3_Esc4l4t10n_Pwn3d}";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInsecureAuthorizationLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);

        // Check if already logged in
        if (isLoggedIn()) {
            showDashboard();
        } else {
            showLoginForm();
        }

        // Log the vulnerability hint
        Log.d("AuthVulnerability", "Authorization check uses client-side role from SharedPreferences");
        Log.d("AuthVulnerability", "Role key: 'user_role' - Values: 'user' or 'admin'");

        return root;
    }

    private void showLoginForm() {
        binding.loginCard.setVisibility(View.VISIBLE);
        binding.dashboardCard.setVisibility(View.GONE);

        Button loginButton = binding.loginButton;
        EditText usernameInput = binding.usernameInput;
        EditText passwordInput = binding.passwordInput;

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.equals(DEMO_USERNAME) && password.equals(DEMO_PASSWORD)) {
                // Successful login - store session with basic user role
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("is_logged_in", true);
                editor.putString("username", username);
                editor.putString("user_role", "user"); // Insecure: stored client-side!
                editor.putLong("login_timestamp", System.currentTimeMillis());
                editor.apply();

                Log.i("Authorization", "User logged in: " + username + " with role: user");
                Log.d("Authorization", "Session stored in SharedPreferences: " + 
                      requireContext().getApplicationInfo().dataDir + "/shared_prefs/UserSession.xml");

                Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                showDashboard();
            } else {
                Toast.makeText(getContext(), "Invalid credentials. Try: testuser / password123", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDashboard() {
        binding.loginCard.setVisibility(View.GONE);
        binding.dashboardCard.setVisibility(View.VISIBLE);

        String username = prefs.getString("username", "User");
        String role = prefs.getString("user_role", "user");

        binding.welcomeText.setText("Welcome, " + username + "!");
        binding.roleText.setText("Current Role: " + role);

        Log.d("Authorization", "Dashboard loaded for user: " + username + " (role: " + role + ")");

        // Access Admin Panel button
        binding.adminPanelButton.setOnClickListener(v -> {
            accessAdminPanel();
        });

        // Logout button
        binding.logoutButton.setOnClickListener(v -> {
            logout();
        });
    }

    private void accessAdminPanel() {
        String role = prefs.getString("user_role", "user");
        
        Log.d("Authorization", "Admin panel access attempt - Current role: " + role);

        // INSECURE: Authorization check relies on client-controlled value
        if ("admin".equals(role)) {
            // Admin access granted
            binding.adminContentCard.setVisibility(View.VISIBLE);
            binding.flagText.setText("🎉 Admin Flag: " + ADMIN_FLAG);
            binding.accessDeniedText.setVisibility(View.GONE);
            
            Toast.makeText(getContext(), "Admin access granted! Flag revealed!", Toast.LENGTH_LONG).show();
            Log.i("Authorization", "ADMIN ACCESS GRANTED - Flag revealed: " + ADMIN_FLAG);
        } else {
            // Access denied
            binding.adminContentCard.setVisibility(View.VISIBLE);
            binding.flagText.setText("");
            binding.accessDeniedText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Access Denied: Admin privileges required", Toast.LENGTH_SHORT).show();
            Log.w("Authorization", "Admin access DENIED for role: " + role);
        }
    }

    private boolean isLoggedIn() {
        return prefs.getBoolean("is_logged_in", false);
    }

    private void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        showLoginForm();
        binding.adminContentCard.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
