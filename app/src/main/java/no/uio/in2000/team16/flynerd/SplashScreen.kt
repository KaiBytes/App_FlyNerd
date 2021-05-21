package no.uio.in2000.team16.flynerd

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        supportActionBar?.hide()
        Handler().postDelayed({
            val intent = Intent(this@SplashScreen, MapActivity::class.java)
            startActivity(intent)
            finish()
        }, 4000)
    }
}