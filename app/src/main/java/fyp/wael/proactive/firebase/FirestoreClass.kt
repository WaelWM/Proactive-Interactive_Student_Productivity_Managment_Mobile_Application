package fyp.wael.proactive.firebase

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : RemainderFragment.kt
// Description      : To connect the firebase to the mobile app and store the users data after registration
// First Written on : Friday, 12-May-2023
// Edited on        : Thursday, 22-Jun-2023

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import fyp.wael.proactive.models.User
import fyp.wael.proactive.userInterface.activity.RegisterActivity
import fyp.wael.proactive.utils.Constants

class FirestoreClass {

    private val firestore = FirebaseFirestore.getInstance()

    fun registerUser(activity: RegisterActivity, userInfo: User) {
        firestore.collection(Constants.USERS)
            .document(userInfo.id)
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener {
                Log.e(
                    activity.javaClass.simpleName,
                    "An Error Occurred While Registering!"
                )
            }
    }

    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getUserDetails(activity: Activity) {
        firestore.collection(Constants.USERS)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val userList = ArrayList<User>()
                for (document in querySnapshot) {
                    val user = document.toObject(User::class.java)
                    userList.add(user)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(activity.javaClass.simpleName, "Error retrieving user details", exception)
            }
    }

}

