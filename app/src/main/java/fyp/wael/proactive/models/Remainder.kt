package fyp.wael.proactive.models

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : Remainder.kt
// Description      : A data class for reminders.
// First Written on : Tuesday, 20-Jun-2023
// Edited on        : Saturday, 1-Jul-2023


import com.google.firebase.firestore.Exclude
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Remainder(
    var id: String,
    val title: String,
    val description: String,
    var date: String,
    var time: String,
    val userId: String
) {
    @get:Exclude
    var dateTime: Date
        get() = parseDateTime(date, time)
        set(value) {
            date = formatDate(value)
            time = formatTime(value)
        }

    constructor() : this("", "", "", "", "", "")

    private fun parseDateTime(date: String, time: String): Date {
        val dateTimeString = "$date $time"
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.parse(dateTimeString)
    }

    private fun formatDate(dateTime: Date): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(dateTime)
    }

    private fun formatTime(dateTime: Date): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(dateTime)
    }
}
