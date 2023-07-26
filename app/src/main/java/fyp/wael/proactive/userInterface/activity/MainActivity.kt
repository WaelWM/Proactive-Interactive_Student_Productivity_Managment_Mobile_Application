package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : MainActivity.kt
// Description      : Main activity for testing purposes only.
// First Written on : Friday, 10-May-2023
// Edited on        : Friday, 10-May-2023

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fyp.wael.proactive.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    //Function Just for testing the connectivity of the firebase database
//    fun sendData(view: View){
//        Log.d("MainActivity", "sendData function called!")
//        val database = Firebase.database
//        val myRef = database.getReference("User")
//
//        myRef.setValue("Testing done Successfully! Database Created!")
//    }

}

