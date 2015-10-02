package org.teamblueridge.pasteitapp;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

public class SettingsActivity extends AppCompatActivity {
    //private static final String TAG = "TeamBlueRidge";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Set up the status bar tint using the CarbonROM SysBarTintManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                & Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            SysBarTintManager.setupTranslucency(this, true, false);
            SysBarTintManager mTintManager = new SysBarTintManager(this);
            mTintManager.setStatusBarTintEnabled(true);
            mTintManager.setActionBarTintEnabled(true);
            mTintManager.setStatusBarTintColor(ContextCompat.getColor(this, R.color.blue_700));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue_700));
        }

        // Set-up up navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Display the fragment as the main content.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragment(), "SettingsFragment")
                    .addToBackStack("SettingsFragment")
                    .commit();
        }
    }
}