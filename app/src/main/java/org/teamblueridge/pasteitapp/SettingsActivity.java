package org.teamblueridge.pasteitapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

public class SettingsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        SysBarTintManager.setupTranslucency(this, true, false);

        SysBarTintManager mTintManager = new SysBarTintManager(this);
        mTintManager.setStatusBarTintEnabled(true);
        mTintManager.setActionBarTintEnabled(true);
        mTintManager.setTintColor(getResources().getColor(R.color.blue_700));

        // Set-up up navigation
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragment(), "SettingsFragment")
                    .addToBackStack("SettingsFragment")
                    .commit();
        }

    }

}