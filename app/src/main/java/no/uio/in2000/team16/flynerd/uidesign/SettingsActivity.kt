package no.uio.in2000.team16.flynerd.uidesign

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
//import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.navigation.NavigationView
import no.uio.in2000.team16.flynerd.MapActivity
import no.uio.in2000.team16.flynerd.R


class SettingsActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var menu: Menu? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
       /** if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar2)

        setSupportActionBar(toolbar)
        val textView = findViewById<View>(R.id.toolbarTextView) as TextView
        textView.text = "Setting"

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


    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }*/
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
                val intent = Intent(this@SettingsActivity, FlightStatusUI::class.java)
                startActivity(intent)

            }

            R.id.Setting -> {

            }

            R.id.airportweather -> {
                val intent = Intent(this@SettingsActivity, WeatherActivity::class.java)
                startActivity(intent)
            }


        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }
}