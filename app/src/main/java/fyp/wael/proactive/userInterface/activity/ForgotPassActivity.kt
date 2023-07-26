package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : ForgotPassActivity.kt
// Description      : To allow students to reset their password.
// First Written on : Sunday, 14-May-2023
// Edited on        : Sunday, 14-May-2023


import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import fyp.wael.proactive.R

class ForgotPassActivity : ComponentActivity() {
    private lateinit var btn_submit: Button
    private lateinit var et_forgot_email: EditText
    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)

        btn_submit = findViewById(R.id.btn_submit)
        et_forgot_email = findViewById(R.id.et_forgot_email)

        btn_submit.setOnClickListener{


            val email: String = et_forgot_email.text.toString().trim()
            if (email.isEmpty()){
                et_forgot_email.error = "Please Enter Your Email Address!"
                et_forgot_email.requestFocus()
            }else
            {
                showProgressDialog("Please Wait...")
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener{task ->
                        progressDialog.dismiss()
                        if(task.isSuccessful){
                            Toast.makeText(this, "Email Sent to rest Password Successfully!"
                            ,Toast.LENGTH_SHORT).show()

                            finish()
                        } else
                        {

                            Toast.makeText(
                                this,task.exception!!.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
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
}