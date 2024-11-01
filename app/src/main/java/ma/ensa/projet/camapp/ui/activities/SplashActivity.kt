package ma.ensa.projet.camapp.ui.activities

import ma.ensa.projet.camapp.ui.activities.MainActivity
import ma.ensa.projet.camapp.R


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import ma.ensa.projet.camapp.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the GIF into the ImageView
        Glide.with(this)
            .asGif()
            .load(R.drawable.cheez_gif)
            .into(binding.splashImageView) // reference the ImageView through binding

        // Proceed to MainActivity after a delay
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Close the SplashActivity
        }, 5000) // Duration in milliseconds (3 seconds)
    }
}
