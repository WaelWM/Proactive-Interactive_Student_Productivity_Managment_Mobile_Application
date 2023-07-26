package fyp.wael.proactive.models

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : StudySession.kt
// Description      : A data class for group study sessions.
// First Written on : Friday, 23-Jun-2023
// Edited on        : Monday, 26-Jun-2023

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class StudySession(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val interests: String = "",
    val participants: MutableList<String> = mutableListOf(),
    val userId: String? = null,
    val userEmail: String? = null,
    val discussions: MutableList<Discussion> = mutableListOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList()?.toMutableList() ?: mutableListOf(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(interests)
        parcel.writeStringList(participants)
        parcel.writeString(userId)
        parcel.writeString(userEmail)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StudySession> {
        override fun createFromParcel(parcel: Parcel): StudySession {
            return StudySession(parcel)
        }

        override fun newArray(size: Int): Array<StudySession?> {
            return arrayOfNulls(size)
        }
    }
}

