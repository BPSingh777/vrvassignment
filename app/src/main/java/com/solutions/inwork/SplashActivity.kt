package com.solutions.inwork

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val background: ImageView = findViewById(R.id.imageView3)
        val loadAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.drop)
        background.startAnimation(loadAnimation)

        loadAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (ContextCompat.checkSelfPermission(
                            this@SplashActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        val intent = Intent(this@SplashActivity, IntroActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@SplashActivity, LogInActivity::class.java)
                        startActivity(intent)
                    }
                    finish()
                }, 2000) // 3000 is the delayed time in milliseconds.
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }
}
