package no.uio.in2000.team16.flynerd.uidesign

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import no.uio.in2000.team16.flynerd.MapActivity
import no.uio.in2000.team16.flynerd.R

class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var menu: Menu? = null

    var switch2: Switch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar2)

        setSupportActionBar(toolbar)
        val textView = findViewById<View>(R.id.toolbarTextView) as TextView
        textView.text = getString(R.string.activity_settings_title)

        supportActionBar!!.setDisplayShowTitleEnabled(false)
        navigationView?.bringToFront()
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()
        navigationView?.setNavigationItemSelectedListener(this)
        navigationView?.setCheckedItem(R.id.Setting)

        val setting_tv1: TextView = findViewById(R.id.tv_setting)
        val mySwitch: SwitchCompat = findViewById(R.id.switch1)
        val mySwitch2: SwitchCompat = findViewById(R.id.switch2)
        mySwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // If the switch button is on
                setting_tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
                mySwitch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
                mySwitch2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
            } else {
                // If the switch button is off
                setting_tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
                mySwitch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
                mySwitch2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);

            }
        })

        mySwitch2.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // If the switch button is on
                setting_tv1.setTypeface(null, Typeface.BOLD);
                mySwitch.setTypeface(null, Typeface.BOLD);
                mySwitch2.setTypeface(null, Typeface.BOLD);
            } else {
                // If the switch button is off
                setting_tv1.setTypeface(null, Typeface.NORMAL);
                mySwitch.setTypeface(null, Typeface.NORMAL);
                mySwitch2.setTypeface(null, Typeface.NORMAL);

            }
        })
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this@SettingsActivity, MapActivity::class.java)
                startActivity(intent)
            }
            R.id.flightStatus -> {
                val intent = Intent(this@SettingsActivity, FlightStatusInfoSearchFlight::class.java)
                startActivity(intent)
            }

            R.id.Setting -> {
            }

            R.id.airportweather -> {
                val intent = Intent(this@SettingsActivity, AirportsListActivity::class.java)
                startActivity(intent)
            }

        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }
}

