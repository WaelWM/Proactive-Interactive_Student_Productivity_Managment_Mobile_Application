package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : LoginActivity.kt
// Description      : To allow users (students and admin) to log in into the mobile app.
// First Written on : Friday, 12-May-2023
// Edited on        : Wednesday, 14-Jun-2023

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import fyp.wael.proactive.R
import fyp.wael.proactive.firebase.FirestoreClass


class LoginActivity : ComponentActivity() {

    private lateinit var et_logEmail: EditText
    private lateinit var et_logPassword: EditText
    private lateinit var btn_login: Button
    private lateinit var cb_showPassword: CheckBox

    private lateinit var progressDialog: Dialog

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            if (currentUser.uid == "0IU8qII8Pga6BqTA3DzWZzTKjW32") {
                navigateToAdminActivity() // to keep the admin signed in even after closing the app
            } else {
                navigateToHome() // to keep other users signed in even after closing the app
            }
            return
        }

        setContentView(R.layout.activity_login)


        val forgetPassTextView: TextView = findViewById(R.id.forget_pass)
        forgetPassTextView.setOnClickListener {
            val intent = Intent(this, ForgotPassActivity::class.java)
            startActivity(intent)
        }
        // Make the forgot password text view underlined:
        forgetPassTextView.setPaintFlags(forgetPassTextView.getPaintFlags()
                or Paint.UNDERLINE_TEXT_FLAG)





        val newUserTextView: TextView = findViewById(R.id.new_userTV)
        newUserTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        //Making part of the Sign Up text view underlined:
        val fullText = "Don't have an account yet? Sign up"
        val underlinedText = "Sign up"

        val spannableString = SpannableString(fullText)
        val underlineSpan = UnderlineSpan()

        val startIndex = fullText.indexOf(underlinedText)
        val endIndex = startIndex + underlinedText.length

        spannableString.setSpan(
            underlineSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        newUserTextView.setText(spannableString)

        btn_login = findViewById(R.id.btn_login)
        et_logEmail = findViewById(R.id.et_logEmail)
        et_logPassword = findViewById(R.id.et_logPassword)
        cb_showPassword = findViewById(R.id.cb_showPassword)

        // Set the checkbox listener to show/hide password
        cb_showPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                et_logPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                et_logPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        btn_login.setOnClickListener {
            val email = et_logEmail.text.toString().trim()
            val password = et_logPassword.text.toString().trim()

            if (email.isEmpty()) {
                et_logEmail.error = "Please enter your Email Address!"
                et_logEmail.requestFocus()
                return@setOnClickListener
            } else if (password.isEmpty()) {
                et_logPassword.error = "Please enter your Password!"
                et_logPassword.requestFocus()
                return@setOnClickListener
            } else {
                showProgressDialog("Logging In...")
                val email: String = et_logEmail.text.toString().trim()
                val password: String = et_logPassword.text.toString().trim()

                // Verify the user credentials
                verifyUserCredentials(email, password)
            }
        }
    }

    private fun verifyUserCredentials(email: String, password: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("Admins").document("admin_credentials")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val adminCredentials = documentSnapshot.data

                if (adminCredentials != null && adminCredentials["email"] == email && adminCredentials["password"] == password) {
                    // Admin credentials are correct, add admin user to Firebase Authentication users list
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Admin user signed in successfully, navigate to AdminActivity
                                navigateToAdminActivity()
                            } else {
                                // Failed to sign in admin user
                                progressDialog.dismiss()
                                Toast.makeText(this, "An error occurred. Please try again later.", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    // Admin credentials are incorrect, try logging in as a normal user
                    loginUser(email, password)
                }
            }
            .addOnFailureListener { exception ->
                // Failed to fetch admin credentials from Firestore
                // You can handle the failure scenario here, such as showing an error message
                progressDialog.dismiss()
                Toast.makeText(this, "An error occurred. Please try again later.", Toast.LENGTH_LONG).show()
            }
    }

    private fun loginUser(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressDialog.dismiss()

                if (task.isSuccessful) {
                    FirestoreClass().getUserDetails(this)
                    Toast.makeText(this, "You have been logged in successfully", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, NavigationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                    intent.putExtra("email_id", email)
                    startActivity(intent)
                    finish()
                } else {
                    val exception = task.exception

                    if (exception is FirebaseAuthInvalidUserException ||
                        exception is FirebaseAuthInvalidCredentialsException
                    ) {
                        // Invalid email or password
                        Toast.makeText(this, "Invalid email or password!", Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseNetworkException ||
                        FirebaseAuth.getInstance().currentUser == null
                    ) {
                        // Network connection issue or no signed-in user
                        Toast.makeText(this, "Please check your internet connection and try again.", Toast.LENGTH_LONG).show()
                    } else {
                        // Other exceptions
                        Toast.makeText(this, "An error occurred. Please try again later.", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun navigateToAdminActivity() {
        val intent = Intent(this, AdminActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, NavigationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
}





