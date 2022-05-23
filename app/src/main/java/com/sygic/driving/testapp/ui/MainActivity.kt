package com.sygic.driving.testapp.ui

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.settings.AppSettings
import com.sygic.driving.testapp.core.utils.getBatteryOptimizationState
import com.sygic.driving.testapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var appSettings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val navController = findNavController()
        binding.navigation.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            appSettings.setBatteryOptimizationState(getBatteryOptimizationState())
        }
    }
}

private fun MainActivity.findNavController(): NavController {
    return findNavController(R.id.nav_fragment)
}

fun Activity.findNavController(): NavController {
    return (this as MainActivity).findNavController()
}
