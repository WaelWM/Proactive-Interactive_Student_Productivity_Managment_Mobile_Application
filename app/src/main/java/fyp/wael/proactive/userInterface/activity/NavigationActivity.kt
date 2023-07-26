package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : NavigationActivity.kt
// Description      : Navigation activity for student functions fragments
// First Written on : Sunday, 21-May-2023
// Edited on        : Wednesday, 19-Jul-2023

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import fyp.wael.proactive.R
import fyp.wael.proactive.databinding.ActivityNavigationBinding
import fyp.wael.proactive.userInterface.fragment.CombinedFragment
import fyp.wael.proactive.utils.Validation.Companion.showToast

class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding

    private var doubleBackToExit = false
    private var isOptionsMenuShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_navigation)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_remainder, R.id.navigation_app_usage_limit,
                R.id.navigation_study_groups, R.id.navigation_profile,
                R.id.navigation_events_proactive_central
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_profile -> {
                    // Pass user details to ProfileFragment
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val name = currentUser?.displayName
                    val email = currentUser?.email
                    val educationLevel = ""
                    val educationBackground = ""
                    val interests = ""

                    val bundle = Bundle()
                    bundle.putString("name", name)
                    bundle.putString("email", email)
                    bundle.putString("educationLevel", educationLevel)
                    bundle.putString("educationBackground", educationBackground)
                    bundle.putString("interests", interests)

                    navController.navigate(R.id.navigation_profile, bundle)
                    isOptionsMenuShown = false
                    invalidateOptionsMenu()
                    true
                }
                R.id.navigation_events_proactive_central -> {
                    // Handle combined fragment navigation (EventsFragment + ProactiveCentralFragment)
                    navigateToCombinedFragment()
                    isOptionsMenuShown = true
                    invalidateOptionsMenu()
                    true
                }
                R.id.navigation_remainder -> {
                    // Navigate to the ReminderFragment
                    navController.navigate(R.id.navigation_remainder)
                    isOptionsMenuShown = false
                    invalidateOptionsMenu()
                    true
                }
                else -> {
                    // Use the navigation controller to navigate to the selected destination
                    navController.navigate(menuItem.itemId)
                    isOptionsMenuShown = false
                    invalidateOptionsMenu()
                    true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isOptionsMenuShown) {
            menuInflater.inflate(R.menu.menu_events, menu)
        } else {
            menu.clear()
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_activity_navigation).navigateUp()
    }

    @Deprecated("DEPRECATION")
    override fun onBackPressed() {
        doubleBackToExit()
    }

    private fun doubleBackToExit() {
        if (doubleBackToExit) {
            finish() // Close the activity and exit the app
            return
        }

        doubleBackToExit = true
        showToast(this, "Please Double Click Back to Exit the App")

        Handler().postDelayed({ doubleBackToExit = false }, 2000)
    }

    private fun navigateToCombinedFragment() {
        val combinedFragment = CombinedFragment() // Create an instance of the CombinedFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_activity_navigation, combinedFragment)
            .commit()
    }
}




