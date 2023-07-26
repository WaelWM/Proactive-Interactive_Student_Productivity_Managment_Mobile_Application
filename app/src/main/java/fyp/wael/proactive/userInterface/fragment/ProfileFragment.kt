package fyp.wael.proactive.userInterface.fragment

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : ProactiveCentralFragment.kt
// Description      : To allow students to view and update their profile
// First Written on : Friday, 19-may-2023
// Edited on        : Monday,  17Jul-2023

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import fyp.wael.proactive.R
import fyp.wael.proactive.userInterface.activity.LoginActivity
import fyp.wael.proactive.utils.Validation
import fyp.wael.proactive.utils.Validation.Companion.showToast

class ProfileFragment : Fragment() {

    private lateinit var ed_profile_name: EditText
    private lateinit var ed_profile_email: EditText
    private lateinit var ed_profile_educationLevel: EditText
    private lateinit var ed_profile_interests: EditText
    private lateinit var btn_logout: Button
    private lateinit var TV_change_image: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var image_profileIV: ImageView

    private val PERMISSION_REQUEST_CODE = 1
    private val PICK_IMAGE_REQUEST = 2
    private val storageRef = FirebaseStorage.getInstance().reference
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)




        // Check if the user is logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            navigateToLogin()
            return null
        }

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading profile data...")
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btn_logout = view.findViewById(R.id.btn_logout)
        TV_change_image = view.findViewById((R.id.TV_change_image))
        TV_change_image.setPaintFlags(TV_change_image.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)


        image_profileIV = view.findViewById(R.id.image_profileIV)
        ed_profile_name = view.findViewById(R.id.ed_profile_name)
        ed_profile_email = view.findViewById(R.id.ed_profile_email)
        ed_profile_educationLevel = view.findViewById(R.id.ed_profile_educationLevel)
        ed_profile_interests = view.findViewById(R.id.ed_profile_interests)


        TV_change_image = view.findViewById(R.id.TV_change_image)
        TV_change_image.setOnClickListener {
            // Open image gallery
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val btnUpdateProfile = view.findViewById<Button>(R.id.btn_update_profile)
        btnUpdateProfile.setOnClickListener {
            checkAndRequestPermission()
            updateProfile()
        }

        btn_logout.setOnClickListener {
            // Logout from the app once the user clicks on the logout button
            try {
                // Logout from the app
                FirebaseAuth.getInstance().signOut()

                // Display a toast message indicating successful logout
                Toast.makeText(
                    requireContext(),
                    "You have been logged out successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Redirect to LoginActivity
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } catch (e: Exception) {
                // Display a toast message indicating logout failure
                Toast.makeText(
                    requireContext(),
                    "Logout failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        progressDialog.show()
        fetchUserDetails()

        return view
    }

    private fun fetchUserDetails() {

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                progressDialog.dismiss()
                if (document != null && document.exists()) {
                    val name = document.getString("name")
                    val email = document.getString("email")
                    val educationLevel = document.getString("educationLevel")
                    val interests = document.getString("interests")
                    val image = document.getString("image")

                    // Update the UI with the user details
                    ed_profile_name.setText(name)
                    ed_profile_email.setText(email)
                    ed_profile_educationLevel.setText(educationLevel)
                    ed_profile_interests.setText(interests)

                    // Load and display the profile image using Glide
                    Glide.with(requireContext())
                        .load(image)
                        .placeholder(R.drawable.default_profile_image)
                        .into(image_profileIV)
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch profile data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }


    private fun updateProfile() {

        progressDialog.setMessage("Updating profile...")
        progressDialog.show()

        if (!isNetworkAvailable(requireContext())) {
            showToast(requireContext(),
                "No internet connection. Please check your Internet settings!")
            progressDialog.dismiss()
        }
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userId)

        val name = ed_profile_name.text.toString().trim()
        val email = ed_profile_email.text.toString().trim()
        val educationLevel = ed_profile_educationLevel.text.toString().trim()
        val interests = ed_profile_interests.text.toString().trim()

        userDocRef.update(
            mapOf(
                "name" to name,
                "email" to email,
                "educationLevel" to educationLevel,
                "interests" to interests
            )
        ).addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(
                requireContext(),
                "Profile updated successfully",
                Toast.LENGTH_SHORT
            ).show()

        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(
                requireContext(),
                "Failed to update profile: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

     private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun checkAndRequestPermission() {
        val readPermission = "android.permission.READ_MEDIA_IMAGES"

        if (shouldShowRequestPermissionRationale(readPermission)) {
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(readPermission),
                PERMISSION_REQUEST_CODE
            )
            if (shouldShowRequestPermissionRationale(readPermission)) {
                // Show an explanation to the user
                Toast.makeText(
                    requireContext(),
                    "Please grant permission to access photos and videos",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Permission has not been granted and should not be requested again
                // Prompt the user to manually open the app settings
                Toast.makeText(
                    requireContext(),
                    "Permission Denied! Cannot upload images. Please go to the app settings and allow access to photos and videos.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun uploadProfileImage() {
        selectedImageUri?.let { imageUri ->
            if (!isNetworkAvailable()) {
                progressDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "No internet connection. Please check your network settings.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            progressDialog.setMessage("Updating image...")
            progressDialog.show()

            // Define a reference to the image file in Firebase Storage
            val storageFilePath =
                storageRef.child("profile_images").child("${FirebaseAuth.getInstance().currentUser?.uid}.jpg")

            // Upload the image file to Firebase Storage
            storageFilePath.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the download URL of the uploaded image
                    taskSnapshot.storage.downloadUrl
                        .addOnSuccessListener { downloadUri ->
                            // Update the user's profile image URL in Firestore
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val userId = currentUser?.uid ?: ""

                            val db = FirebaseFirestore.getInstance()
                            val userDocRef = db.collection("users").document(userId)

                            userDocRef.update("image", downloadUri.toString())
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    Toast.makeText(
                                        requireContext(),
                                        "Profile image updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Load and display the updated profile image using Glide
                                    Glide.with(requireContext())
                                        .load(downloadUri)
                                        .placeholder(R.drawable.default_profile_image)
                                        .into(image_profileIV)
                                }
                                .addOnFailureListener { e ->
                                    progressDialog.dismiss()
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to update profile image: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(
                                requireContext(),
                                "Failed to get download URL: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Failed to upload profile image: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // Get the selected image URI
            selectedImageUri = data.data

            // Upload the image to Firebase Storage
            uploadProfileImage()
        }
    }
}

