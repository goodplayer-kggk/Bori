package com.goodground.bori.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.goodground.bori.R
import com.google.android.gms.ads.MobileAds
import com.goodground.bori.ui.main.MainActivity
import com.goodground.bori.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}

        val logo = binding.ivBoriLogo
        val anim = AnimationUtils.loadAnimation(this, R.anim.bori_scale_down)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // 애니메이션이 끝나는 즉시 메인으로 이동
                goToMain()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        logo.startAnimation(anim)
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // API 34 이상: 애니메이션 없음 (리소스 0 사용)
            startActivity(intent)
            overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_OPEN,
                0, // enterAnim 리소스 ID
                0  // exitAnim 리소스 ID
            )
        } else {
            // API 33 이하 (이전 버전): 기존 overridePendingTransition 사용
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
        finish()
    }
}