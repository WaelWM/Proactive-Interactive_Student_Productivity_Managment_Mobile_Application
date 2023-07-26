package fyp.wael.proactive.utils

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : Validation.kt
// Description      : To create functions that can be used in other classes
// First Written on : Tuesday, 18-Jul-2023
// Edited on        : Saturday, 22-Jul-2023

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast

class Validation {
    companion object {
        //checking internet connection info function
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

        // Simplified toast message display function
        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        // Showing error message if no intenert connection function
        fun internetValidation(context: Context) {
            if (!isNetworkAvailable(context)) {
                showToast(context,
                    "No internet connection. Please check your Internet settings!")
            }
        }
    }
}
