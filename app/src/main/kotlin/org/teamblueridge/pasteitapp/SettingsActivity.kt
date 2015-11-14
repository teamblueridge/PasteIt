package org.teamblueridge.pasteitapp

import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import org.teamblueridge.utils.SysBarTintManager

import kotlinx.android.synthetic.activity_main.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Set up the status bar tint using the CarbonROM SysBarTintManager
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) &&
                (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)) {
            SysBarTintManager.setupTranslucency(this, true, false)
            val mTintManager = SysBarTintManager(this)
            mTintManager.isStatusBarTintEnabled = true
            mTintManager.isActionBarTintEnabled = true
            mTintManager.setStatusBarTintColor(ContextCompat.getColor(this, R.color.blue_700))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.blue_700)
        }

        // Set-up up navigation
        supportActionBar.setHomeButtonEnabled(true)
        supportActionBar.setDisplayHomeAsUpEnabled(true)

        // Display the fragment as the main content.
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, SettingsFragment(), "SettingsFragment")
                    .addToBackStack("SettingsFragment")
                    .commit()
        }
    }
}