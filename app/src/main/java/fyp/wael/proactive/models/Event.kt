package fyp.wael.proactive.models

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : Event.kt
// Description      : A data class for events.
// First Written on : Friday, 7-July-2023
// Edited on        : Saturday, 8-July-2023

import java.io.Serializable

data class Event(
    var eventId: String = "",
    var title: String = "",
    var description: String = "",
    var time: String = "",
    var date: String = "",
    var type: String = ""
) : Serializable {
    constructor() : this("", "", "", "", "", "")
}