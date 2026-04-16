package com.owasp.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.owasp.app.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before calling super.onCreate
        applyTheme();
        
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        
        // Add scale animation to FAB on click
        binding.appBarMain.fab.setScaleX(1f);
        binding.appBarMain.fab.setScaleY(1f);
        
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Animate FAB scale on click
                view.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                    })
                    .start();
                    
                //create alert dialogue for floating "help" action button.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                //set the message
                String message = "Mobile Shepherd - Security Training Platform\n\n" +
                        "This application is designed to teach mobile application security through " +
                        "hands-on lessons and challenges based on the OWASP Mobile Top 10.\n\n" +
                        "Part of the OWASP Security Shepherd project.";

                builder.setMessage(message);
                builder.setTitle("About Mobile Shepherd");
                builder.setCancelable(true);
                
                builder.setPositiveButton("OWASP Mobile Top 10", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                            android.net.Uri.parse("https://owasp.org/www-project-mobile-top-10/"));
                        startActivity(browserIntent);
                    }
                });
                
                builder.setNeutralButton("Security Shepherd", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                            android.net.Uri.parse("https://owasp.org/www-project-security-shepherd/"));
                        startActivity(browserIntent);
                    }
                });
                
                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        
        // Setup RecyclerView for navigation
        RecyclerView navRecyclerView = findViewById(R.id.nav_recycler_view);
        navRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        List<NavigationItem> navigationItems = createNavigationItems();
        NavigationAdapter adapter = new NavigationAdapter(navigationItems, item -> {
            // Handle navigation item click
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(item.getNavigationId());
            drawer.closeDrawers();
        });
        navRecyclerView.setAdapter(adapter);
        
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_lesson, R.id.nav_insecure_data_lesson, R.id.nav_poor_auth_lesson, R.id.nav_insecure_authorization_lesson, R.id.nav_supply_chain_lesson, R.id.nav_insecure_comm_lesson, R.id.nav_insufficient_crypto_lesson, R.id.nav_security_misconfig_lesson,
                R.id.nav_challenge1, R.id.nav_challenge2, R.id.nav_challenge3,
            R.id.nav_insecure_data1, R.id.nav_insecure_data2,
                R.id.nav_poor_auth_challenge, R.id.nav_supply_chain_challenge, R.id.nav_insecure_comm_challenge, R.id.nav_insufficient_crypto_challenge,
                R.id.nav_security_misconfig_challenge2, R.id.nav_security_misconfig_challenge3,
                R.id.nav_input_validation_lesson, R.id.nav_xss_challenge,
                R.id.nav_privacy_lesson,
                R.id.nav_client_side_injection_lesson, R.id.nav_client_side_injection_challenge1, R.id.nav_client_side_injection_challenge2,
                R.id.nav_adb_reference
        ).setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    }

    private List<NavigationItem> createNavigationItems() {
        List<NavigationItem> items = new ArrayList<>();
        
        // Home
        items.add(new NavigationItem(1, "Home", R.drawable.ic_menu_home, R.id.nav_home));
        
        // Lessons group
        NavigationItem lessonsGroup = new NavigationItem(2, "Lessons", R.drawable.ic_menu_camera);
        lessonsGroup.addChild(new NavigationItem(21, "Reverse Engineering", 0, R.id.nav_lesson));
        lessonsGroup.addChild(new NavigationItem(22, "Insecure Data Storage", 0, R.id.nav_insecure_data_lesson));
        lessonsGroup.addChild(new NavigationItem(23, "Poor Authentication", 0, R.id.nav_poor_auth_lesson));
        lessonsGroup.addChild(new NavigationItem(24, "Insecure Authorization", 0, R.id.nav_insecure_authorization_lesson));
        lessonsGroup.addChild(new NavigationItem(25, "Supply Chain Security", 0, R.id.nav_supply_chain_lesson));
        lessonsGroup.addChild(new NavigationItem(26, "Insecure Communication", 0, R.id.nav_insecure_comm_lesson));
        lessonsGroup.addChild(new NavigationItem(27, "Insufficient Cryptography", 0, R.id.nav_insufficient_crypto_lesson));
        lessonsGroup.addChild(new NavigationItem(28, "Security Misconfiguration", 0, R.id.nav_security_misconfig_lesson));
        lessonsGroup.addChild(new NavigationItem(29, "Input Validation", 0, R.id.nav_input_validation_lesson));
        lessonsGroup.addChild(new NavigationItem(30, "Privacy Controls", 0, R.id.nav_privacy_lesson));
        lessonsGroup.addChild(new NavigationItem(31, "Client-Side Injection", 0, R.id.nav_client_side_injection_lesson));
        items.add(lessonsGroup);
        
        // Challenges group
        NavigationItem challengesGroup = new NavigationItem(3, "Challenges", R.drawable.ic_menu_code);
        
        // Reverse Engineering sub-group
        NavigationItem reverseEngGroup = new NavigationItem(40, "Reverse Engineering", 0);
        reverseEngGroup.addChild(new NavigationItem(41, "Challenge 1", 0, R.id.nav_challenge1));
        reverseEngGroup.addChild(new NavigationItem(42, "Challenge 2", 0, R.id.nav_challenge2));
        reverseEngGroup.addChild(new NavigationItem(43, "Challenge 3", 0, R.id.nav_challenge3));
        challengesGroup.addChild(reverseEngGroup);
        
        // Insecure Data Storage sub-group
        NavigationItem insecureDataGroup = new NavigationItem(44, "Insecure Data Storage", 0);
        insecureDataGroup.addChild(new NavigationItem(45, "Challenge 1", 0, R.id.nav_insecure_data1));
        insecureDataGroup.addChild(new NavigationItem(46, "Challenge 2", 0, R.id.nav_insecure_data2));
        challengesGroup.addChild(insecureDataGroup);
        
        // Individual challenges
        challengesGroup.addChild(new NavigationItem(47, "Poor Authentication", 0, R.id.nav_poor_auth_challenge));
        challengesGroup.addChild(new NavigationItem(48, "Supply Chain Security", 0, R.id.nav_supply_chain_challenge));
        challengesGroup.addChild(new NavigationItem(49, "Insecure Communication", 0, R.id.nav_insecure_comm_challenge));
        challengesGroup.addChild(new NavigationItem(50, "Insufficient Cryptography", 0, R.id.nav_insufficient_crypto_challenge));
        
        // Security Misconfiguration sub-group
        NavigationItem securityMisconfigGroup = new NavigationItem(51, "Security Misconfiguration", 0);
        securityMisconfigGroup.addChild(new NavigationItem(52, "Challenge 1", 0, R.id.nav_security_misconfig_challenge2));
        securityMisconfigGroup.addChild(new NavigationItem(53, "Challenge 2", 0, R.id.nav_security_misconfig_challenge3));
        challengesGroup.addChild(securityMisconfigGroup);
        
        challengesGroup.addChild(new NavigationItem(54, "XSS WebView", 0, R.id.nav_xss_challenge));
        
        // Client-Side Injection sub-group
        NavigationItem clientSideGroup = new NavigationItem(55, "Client-Side Injection", 0);
        clientSideGroup.addChild(new NavigationItem(56, "Challenge 1", 0, R.id.nav_client_side_injection_challenge1));
        clientSideGroup.addChild(new NavigationItem(57, "Challenge 2", 0, R.id.nav_client_side_injection_challenge2));
        challengesGroup.addChild(clientSideGroup);
        
        items.add(challengesGroup);
        
        // ADB Reference
        items.add(new NavigationItem(4, "ADB Reference", R.drawable.ic_menu_code, R.id.nav_adb_reference));
        
        return items;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_adb_reference: {
                    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
                    navController.navigate(R.id.nav_adb_reference);
                }
                return true;
            case R.id.action_settings: {
                    Intent goToSettings = new Intent(this, Preferences.class);
                    startActivity(goToSettings);
                Toast.makeText(this, "Settings Selected", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_owasp_top10: {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                        android.net.Uri.parse("https://owasp.org/www-project-mobile-top-10/"));
                    startActivity(browserIntent);
                }
                return true;
            case R.id.action_exit:
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void applyTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String themeValue = preferences.getString("theme_preference", "system");
        
        switch (themeValue) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}