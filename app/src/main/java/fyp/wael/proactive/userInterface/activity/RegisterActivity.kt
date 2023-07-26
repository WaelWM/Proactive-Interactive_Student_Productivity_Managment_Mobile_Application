package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : EventRegistration.kt
// Description      : To allow students register into the mobile app.
// First Written on : Saturday, 13-May-2023
// Edited on        : Thursday, 1-Jul-2023

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import fyp.wael.proactive.R
import fyp.wael.proactive.firebase.FirestoreClass
import fyp.wael.proactive.models.User

class RegisterActivity : ComponentActivity() {
    private lateinit var text_name : EditText
    private lateinit var text_email : EditText
    private lateinit var spinner_educationLevel: Spinner
    private lateinit var text_interests : EditText
    private lateinit var text_password : EditText
    private lateinit var text_confirmPassword : EditText
    private lateinit var btn_register : Button

    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val signTextView: TextView = findViewById(R.id.text_sign)
        signTextView.setOnClickListener {
            // Handle the click event here
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        text_name = findViewById(R.id.ed_name)
        text_email = findViewById(R.id.ed_email)
        spinner_educationLevel = findViewById(R.id.spinner_educationLevel)
        text_interests = findViewById(R.id.ed_interests)
        text_password = findViewById(R.id.ed_password)
        text_confirmPassword = findViewById(R.id.ed_confirmPassword)
        btn_register = findViewById(R.id.btn_register)

        // Create adapters for the spinners
        val educationLevelAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.education_levels,
            android.R.layout.simple_spinner_item
        )
        educationLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_educationLevel.adapter = educationLevelAdapter


        btn_register.setOnClickListener{

            val name = text_name.text.toString().trim()
            val email = text_email.text.toString().trim()
            val educationLevel = spinner_educationLevel.selectedItem.toString()
            val interests = text_interests.text.toString().trim()
            val password = text_password.text.toString().trim()
            val confirmPassword = text_confirmPassword.text.toString().trim()

            if (name.isEmpty()){
                text_name.error = "Please Enter your name"
                text_name.requestFocus()
                return@setOnClickListener

            } else if (email.isEmpty()){
                text_email.error = "Please Enter your Email"
                text_email.requestFocus()
                return@setOnClickListener

            } else if (interests.isEmpty()){
                text_interests.error = "Please Enter your Interests"
                text_interests.requestFocus()
                return@setOnClickListener

            } else if (password.isEmpty()){
                text_password.error = "Please Enter Your Password"
                text_password.requestFocus()
                return@setOnClickListener

            } else if (confirmPassword.isEmpty()){
                text_confirmPassword.error = "Please Confirm your Password"
                text_confirmPassword.requestFocus()
                return@setOnClickListener
            } else if(password!= confirmPassword){
                text_confirmPassword.error = "The Confirm Password Does not match the Above password!"
                text_confirmPassword.requestFocus()
                return@setOnClickListener
            }
            else{
                showProgressDialog("Registering...")
                val email: String = text_email.text.toString().trim()
                val password: String = text_password.text.toString().trim()

                // Creating an instance and creating a registered user with email and password
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(
                        OnCompleteListener { task ->

                            //If the registration is successfully done
                            if (task.isSuccessful){
                                // Register user in the firebase
                                val firebaseUser: FirebaseUser = task.result!!.user!!

                                val user = User(
                                    firebaseUser.uid,
                                    text_name.text.toString().trim(),
                                    text_email.text.toString().trim(),
                                    educationLevel,
                                    text_interests.text.toString().trim()
                                )

                                FirestoreClass().registerUser(this, user)

                                Toast.makeText(
                                    this, "You have registered successfully!", Toast.LENGTH_SHORT
                                ).show()

                                // Automatically Sign-in the new user if the registration was successful
                                val intent = Intent(this, NavigationActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", firebaseUser.uid)
                                intent.putExtra("email_id", email)
                                startActivity(intent)
                                finish()

                            } else{
                                // If registration is not successfully then show an error message
                                Toast.makeText(
                                    this, task.exception!!.message.toString(), Toast.LENGTH_LONG
                                ).show()
                            }
                            progressDialog.dismiss() // Dismiss the progress dialog after registration process finishes
                        }
                    )
            }
        }
    }

    private fun showProgressDialog(text: String) {
        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.dialog_progress)

        val tvProgressText: TextView = progressDialog.findViewById(R.id.tv_progress_text)
        tvProgressText.text = text

        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)


        progressDialog.show()
    }

    fun userRegistrationSuccess(){

        Toast.makeText(
            this, "You have registered successfully!", Toast.LENGTH_SHORT
        ).show()

    }
}