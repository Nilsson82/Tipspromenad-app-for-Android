package com.nilsson.tipspromenad

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.nilsson.tipspromenad.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        supportFragmentManager.setFragmentResultListener(
            LanguageSettingsDialog.ORGANIZER_KEY, this
        ) { _, _ ->
            val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
            val quiz = navHost?.childFragmentManager?.fragments?.filterIsInstance<FirstFragment>()?.firstOrNull()
            quiz?.showOrganizer()
            if (navController.currentDestination?.id == R.id.SecondFragment) navController.popBackStack()
        }

        supportFragmentManager.setFragmentResultListener(
            LanguageSettingsDialog.RESULT_KEY, this
        ) { _, _ ->
            val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
            navHost?.childFragmentManager?.fragments?.filterIsInstance<FirstFragment>()
                ?.forEach { it.refreshLanguageIfNeeded() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                val quiz = navHost?.childFragmentManager?.fragments?.filterIsInstance<FirstFragment>()?.firstOrNull()
                if (quiz?.showWalkSettings() == true) return true
                if (supportFragmentManager.findFragmentByTag(LanguageSettingsDialog.TAG) == null) {
                    LanguageSettingsDialog().show(supportFragmentManager, LanguageSettingsDialog.TAG)
                }
                true
            }
            R.id.action_info -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                if (navController.currentDestination?.id == R.id.FirstFragment) {
                    navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
                }
                true
            }
            R.id.action_quit -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
