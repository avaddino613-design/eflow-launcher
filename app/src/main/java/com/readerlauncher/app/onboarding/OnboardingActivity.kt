package com.readerlauncher.app.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.readerlauncher.app.MainActivity
import com.readerlauncher.app.R
import com.readerlauncher.app.databinding.ActivityOnboardingBinding
import com.readerlauncher.app.util.PermissionUtils
import com.readerlauncher.app.util.Prefs

/**
 * Three steps: welcome, set-as-default-launcher, done. The Next button is
 * always tappable regardless of whether the launcher role has been granted
 * yet - fixes the "stuck, no way to proceed" complaint seen in the
 * original app's reviews, without adding any new functionality.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var step = 0
    private val lastStep = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.setDefaultLauncherButton.setOnClickListener { PermissionUtils.requestDefaultLauncher(this) }

        binding.backButton.setOnClickListener { goToStep(step - 1) }
        binding.nextButton.setOnClickListener { if (step == lastStep) finishOnboarding() else goToStep(step + 1) }
        binding.skipButton.setOnClickListener { finishOnboarding() }

        goToStep(0)
    }

    override fun onResume() {
        super.onResume()
        refreshStatusLabels()
    }

    private fun goToStep(index: Int) {
        step = index.coerceIn(0, lastStep)
        binding.stepFlipper.displayedChild = step
        binding.backButton.visibility = if (step == 0) View.INVISIBLE else View.VISIBLE
        binding.onboardingProgress.progress = ((step + 1) * 100) / (lastStep + 1)
        refreshStatusLabels()
    }

    private fun refreshStatusLabels() {
        binding.defaultLauncherStatus.text = getString(
            if (PermissionUtils.isDefaultLauncher(this)) R.string.status_enabled else R.string.status_not_enabled
        )
    }

    private fun finishOnboarding() {
        Prefs.setOnboardingComplete(this, true)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
