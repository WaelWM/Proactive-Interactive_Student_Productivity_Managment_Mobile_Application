package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : AdminActivity.kt
// Description      : The main screen of the admin which allow the admin to access all the functions
// First Written on : Saturday, 24-Jun-2023
// Edited on        : Sunday, 2-Jul-2023

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import fyp.wael.proactive.R

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val btnLogout: Button = findViewById(R.id.btnAdminLogout)
        btnLogout.setOnClickListener {
            logoutAdmin()
        }

        val btnAddEvent: Button = findViewById(R.id.btnAddEvent)
        btnAddEvent.setOnClickListener{
            intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }

        val btnAdminViewEvent: Button = findViewById(R.id.btnAdminViewEvent)
        btnAdminViewEvent.setOnClickListener{
            intent = Intent(this, AdminViewEventsActivity::class.java)
            startActivity(intent)
        }

        val btnAddKnowledgeBase: Button = findViewById(R.id.btnAddKnowledgeBase)
        btnAddKnowledgeBase.setOnClickListener{
            intent = Intent(this, KnowledgeBaseActivity::class.java)
            startActivity(intent)
        }
    }

    private fun logoutAdmin() {
        FirebaseAuth.getInstance().signOut()

        // Redirect the admin to the login screen
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
        Toast.makeText(this, "You have been Logged Out Successfully!",
            Toast.LENGTH_SHORT).show()
    }
}