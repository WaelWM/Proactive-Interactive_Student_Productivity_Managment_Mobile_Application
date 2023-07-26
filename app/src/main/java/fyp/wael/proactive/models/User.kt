package fyp.wael.proactive.models

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : User.kt
// Description      : A data class for users collection in Firebase.
// First Written on : Wednesday, 10-May-2023
// Edited on        : Sunday, 28-May-2023

class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val educationLevel: String = "",
    val interests: String = "",
    val image: String = ""
){
    // Adding an empty constructor for Firestore deserialization
    constructor() : this("", "", "",
        "", "", "")
}