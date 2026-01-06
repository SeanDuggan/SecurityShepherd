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

import com.google.android.material.snackbar.Snackbar;
import com.owasp.app.databinding.ActivityMainBinding;

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
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //create alert dialogue for floating "help" action button.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                //set the message
                builder.setMessage("This App is part of the Security Shepherd project. " +
                "In order to complete the Reverse Engineering Lesson and Challenges, " +
                "the player must extract the keys from this app.");

                builder.setTitle("Info");
                builder.setCancelable(false);
                builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        //default to cancel as alert box provides
                                        // context for player and no functionality
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_lesson, R.id.nav_insecure_data_lesson, R.id.nav_poor_auth_lesson, R.id.nav_insecure_authorization_lesson, R.id.nav_supply_chain_lesson, R.id.nav_insecure_comm_lesson, R.id.nav_insufficient_crypto_lesson, R.id.nav_security_misconfig_lesson,
                R.id.nav_challenge_1, R.id.nav_challenge_2, R.id.nav_challenge_3,
                R.id.nav_insecure_data1, R.id.nav_insecure_data2, R.id.nav_insecure_data3,
                R.id.nav_poor_auth_challenge, R.id.nav_supply_chain_challenge, R.id.nav_insecure_comm_challenge, R.id.nav_insufficient_crypto_challenge,
                R.id.nav_security_misconfig_challenge2, R.id.nav_security_misconfig_challenge3,
                R.id.nav_adb_reference
        ).setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
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