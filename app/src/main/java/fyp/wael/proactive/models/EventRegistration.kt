package fyp.wael.proactive.models

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : EventRegistration.kt
// Description      : A data class for events registration.
// First Written on : Friday, 8-July-2023
// Edited on        : Friday, 8-July-2023

data class EventRegistration(
    val eventId: String = "",
    val eventTitle: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val registrationId: String = ""
)