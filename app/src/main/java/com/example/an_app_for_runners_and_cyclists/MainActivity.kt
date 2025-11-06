package com.example.an_app_for_runners_and_cyclists

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.an_app_for_runners_and_cyclists.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupCustomToolbar()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        // Следим за навигацией и обновляем заголовок
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.runTrackingFragment -> updateToolbarTitle("Track Run")
                R.id.runHistoryFragment -> updateToolbarTitle("Run History")
                R.id.profileFragment -> updateToolbarTitle("My Profile")
                R.id.otherRunnersFragment -> updateToolbarTitle("Community")
            }
        }
    }

    private fun setupCustomToolbar() {
        // Обработчик нажатия на иконку меню (три полоски)
        binding.menuIcon.setOnClickListener {
            showDropdownMenu()
        }

        // Обработчик нажатия на иконку истории (справа)
        binding.historyIcon.setOnClickListener {
            navController.navigate(R.id.runHistoryFragment)
        }
    }

    private fun showDropdownMenu() {
        val popup = PopupMenu(this, binding.menuIcon)
        popup.menuInflater.inflate(R.menu.main_dropdown_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_tracking -> {
                    navController.navigate(R.id.runTrackingFragment)
                    true
                }
                R.id.action_history -> {
                    navController.navigate(R.id.runHistoryFragment)
                    true
                }
                R.id.action_profile -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                R.id.action_community -> {
                    navController.navigate(R.id.otherRunnersFragment)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun updateToolbarTitle(title: String) {
        binding.toolbarTitle.text = title
    }
}