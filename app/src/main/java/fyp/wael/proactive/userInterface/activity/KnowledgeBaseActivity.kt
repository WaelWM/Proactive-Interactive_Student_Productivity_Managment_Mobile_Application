package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : KnowledgeBaseActivity.kt
// Description      : To allow admin to upload media to Firebase Storage and Firebase Database.
// First Written on : Wednesday, 19-July-2023
// Edited on        : Sunday, 23-Jul-2023

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.core.utilities.Validation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import fyp.wael.proactive.R
import fyp.wael.proactive.models.KnowledgeBase
import fyp.wael.proactive.utils.Validation.Companion.internetValidation
import fyp.wael.proactive.utils.Validation.Companion.showToast
import java.util.UUID


class KnowledgeBaseActivity : AppCompatActivity() {
    private lateinit var storageReference: StorageReference
    private val folderPath = "KnowledgeBase/"
    private val PERMISSION_REQUEST_CODE = 1
    private val FILE_UPLOAD_REQUEST_CODE = 2
    private lateinit var progressBar: ProgressBar
    private var uploadTask: UploadTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knowledge_base)

        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().reference

        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE

        val btnUpload = findViewById<Button>(R.id.btnUpload)
        btnUpload.setOnClickListener {
            if (checkPermission()) {
                openFileChooser()
            } else {
                requestPermission()
            }
        }
    }

    private fun checkPermission(): Boolean {
        val readPermission = "android.permission.READ_MEDIA_IMAGES"
        val resultRead = ContextCompat.checkSelfPermission(this, readPermission)
        return resultRead == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val readPermission = "android.permission.READ_MEDIA_IMAGES"

        if (shouldShowRequestPermissionRationale(readPermission)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(readPermission),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission has not been granted and should not be requested again
            // Directly open the app settings so that the user can grant the permission manually
            Toast.makeText(
                this,
                "Permission Denied! Cannot upload images. Please Allow Access to photos" +
                        " and videos for this app",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, FILE_UPLOAD_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_UPLOAD_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                // Multiple images selected
                val clipData = data.clipData

                for (i in 0 until clipData!!.itemCount) {
                    progressBar.visibility = View.VISIBLE
                    val fileUri = clipData.getItemAt(i).uri
                    uploadMedia(fileUri)
                }

            } else if (data.data != null) {
                // Single image selected
                val fileUri = data.data!!
                progressBar.visibility = View.VISIBLE
                uploadMedia(fileUri)

            }
        }
    }

    private fun uploadMedia(fileUri: Uri) {

        internetValidation(this) // Checking the internet connection using the created function

        val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}"

        //Generating a unique filename using current timestamp and a random UUID
        val fileReference = storageReference.child("$folderPath$fileName")

        //Create a reference to the storage location where the file will be uploaded
        uploadTask = fileReference.putFile(fileUri)

        uploadTask?.addOnProgressListener { taskSnapshot -> //Adding a progress listener
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            progressBar.progress = progress
        }?.continueWithTask { task ->
            if (!task.isSuccessful) { //Adding on failure listener
                throw task.exception ?: Exception("Unknown error occurred")
            }
            fileReference.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) { //Adding on success listener
                val downloadUrl = task.result.toString() //Getting the download URL from the task result

                val imageId = UUID.randomUUID().toString() //Generating a unique ID for the image
                val name = fileName

                //Creating a KnowledgeBase object with the needed information
                val knowledgeBase = KnowledgeBase(name, downloadUrl, imageId)

                //adding the KnowledgeBase object to the Firebase
                val db = FirebaseFirestore.getInstance()
                db.collection("KnowledgeBase")
                    .add(knowledgeBase)
                    .addOnSuccessListener { documentReference ->
                        showToast(this@KnowledgeBaseActivity, "Upload successful!")
                        progressBar.visibility = View.GONE //Hiding the progress bar upon success
                    }
                    .addOnFailureListener { e ->
                        showToast(this@KnowledgeBaseActivity,
                            "Failed to upload!: ${e.message}")
                        progressBar.visibility = View.GONE //Hiding the progress bar upon failure
                    }

            } else {
                showToast(this@KnowledgeBaseActivity,
                    "Upload failed: ${task.exception?.message}")
                progressBar.visibility = View.GONE
            }
        }
    }
}



