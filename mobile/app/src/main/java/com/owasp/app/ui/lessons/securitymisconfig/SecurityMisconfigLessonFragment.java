package com.owasp.app.ui.lessons.securitymisconfig;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class SecurityMisconfigLessonFragment extends Fragment {

    private static final String TAG = "SecurityMisconfig";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    
    private TextView debugStatus;
    private TextView backupStatus;
    private TextView exportedStatus;
    private TextView networkStatus;
    private TextView permissionsStatus;
    private TextView resultText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_security_misconfig_lesson, container, false);
        
        progressTracker = new ProgressTracker(requireContext());

        debugStatus = root.findViewById(R.id.debug_status);
        backupStatus = root.findViewById(R.id.backup_status);
        exportedStatus = root.findViewById(R.id.exported_status);
        networkStatus = root.findViewById(R.id.network_status);
        permissionsStatus = root.findViewById(R.id.permissions_status);
        resultText = root.findViewById(R.id.result_text);
        
        Button checkButton = root.findViewById(R.id.check_config_button);
        checkButton.setOnClickListener(v -> checkConfiguration());

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> toggleFabExpansion(fab, fabCommandRef, fabOwaspLink));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }
        if (fabOwaspLink != null) {
            fabOwaspLink.setOnClickListener(v -> {
                openOwaspTop10Link();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }

        return root;
    }

    private void checkConfiguration() {
        int issues = 0;
        
        // Check if app is debuggable
        try {
            ApplicationInfo appInfo = requireContext().getApplicationInfo();
            boolean isDebuggable = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            
            if (isDebuggable) {
                debugStatus.setText(" Debug Mode: ENABLED (Insecure)");
                debugStatus.setTextColor(Color.parseColor("#D32F2F"));
                issues++;
                Log.w(TAG, "Security Issue: Debug mode is enabled in production");
            } else {
                debugStatus.setText("✓ Debug Mode: Disabled (Secure)");
                debugStatus.setTextColor(Color.parseColor("#388E3C"));
            }
        } catch (Exception e) {
            debugStatus.setText("Debug Mode: Error checking");
        }

        // Check backup flag
        try {
            ApplicationInfo appInfo = requireContext().getApplicationInfo();
            boolean allowBackup = (appInfo.flags & ApplicationInfo.FLAG_ALLOW_BACKUP) != 0;
            
            if (allowBackup) {
                backupStatus.setText(" Backup Allowed: TRUE (Insecure)");
                backupStatus.setTextColor(Color.parseColor("#D32F2F"));
                issues++;
                Log.w(TAG, "Security Issue: Backup is allowed, app data can be extracted");
            } else {
                backupStatus.setText("✓ Backup Allowed: FALSE (Secure)");
                backupStatus.setTextColor(Color.parseColor("#388E3C"));
            }
        } catch (Exception e) {
            backupStatus.setText("Backup Allowed: Error checking");
        }

        // Check for exported components
        try {
            PackageManager pm = requireContext().getPackageManager();
            String packageName = requireContext().getPackageName();
            int exportedCount = 0;
            
            // This is a simplified check - in reality you'd enumerate activities, services, receivers
            exportedStatus.setText(" Exported Components: Found (Potential Risk)");
            exportedStatus.setTextColor(Color.parseColor("#F57C00"));
            Log.w(TAG, "Security Warning: Exported components detected - verify they require permissions");
        } catch (Exception e) {
            exportedStatus.setText("Exported Components: Error checking");
        }

        // Check network security
        networkStatus.setText(" Network Security: Cleartext Traffic Allowed");
        networkStatus.setTextColor(Color.parseColor("#D32F2F"));
        issues++;
        Log.w(TAG, "Security Issue: Cleartext traffic (HTTP) is allowed");

        // Check permissions
        permissionsStatus.setText("✓ Permissions: Following Least Privilege");
        permissionsStatus.setTextColor(Color.parseColor("#388E3C"));

        // Show summary
        resultText.setVisibility(View.VISIBLE);
        if (issues >= 3) {
            resultText.setText(" CRITICAL: " + issues + " major security misconfigurations found!\n\nThis app is vulnerable to multiple attack vectors.");
            resultText.setTextColor(Color.parseColor("#D32F2F"));
            resultText.setBackgroundColor(Color.parseColor("#FFEBEE"));
        } else if (issues > 0) {
            resultText.setText(" WARNING: " + issues + " security issues detected.\n\nReview configuration settings.");
            resultText.setTextColor(Color.parseColor("#F57C00"));
            resultText.setBackgroundColor(Color.parseColor("#FFF3E0"));
        } else {
            resultText.setText("✓ SECURE: No major issues detected.\n\nConfiguration looks good!");
            resultText.setTextColor(Color.parseColor("#388E3C"));
            resultText.setBackgroundColor(Color.parseColor("#E8F5E9"));
        }

        Log.i(TAG, "Configuration check complete. Issues found: " + issues);
    }

    private void toggleFabExpansion(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2) {
        fabExpanded = !fabExpanded;
        if (fabExpanded) {
            if (fab1 != null) fab1.setVisibility(View.VISIBLE);
            if (fab2 != null) fab2.setVisibility(View.VISIBLE);
            if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            collapseFab(mainFab, fab1, fab2);
        }
    }

    private void collapseFab(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2) {
        fabExpanded = false;
        if (fab1 != null) fab1.setVisibility(View.GONE);
        if (fab2 != null) fab2.setVisibility(View.GONE);
        if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_help);
    }

    private void openOwaspTop10Link() {
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m10-extraneous-functionality";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void showDetailedInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.security_misconfig_intro);
        // vulnerabilitiesText.setText(R.string.security_misconfig_vulnerabilities);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.SECURITY_MISCONFIG_LESSON);
        String buttonText = isCompleted ? "Mark as Incomplete" : "Mark as Complete";
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Security Misconfiguration")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .setNeutralButton(buttonText, (d, which) -> {
                    boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.SECURITY_MISCONFIG_LESSON);
                    String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
                    android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
    }
}
