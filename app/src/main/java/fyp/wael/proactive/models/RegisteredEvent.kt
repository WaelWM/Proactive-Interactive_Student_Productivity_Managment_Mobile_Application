package fyp.wael.proactive.models

import java.io.Serializable

data class RegisteredEvent(
    var eventId: String = "",
    var title: String = "",
    var description: String = "",
    var time: String = "",
    var date: String = "",
    var type: String = ""
) : Serializable {
    constructor(event: Event) : this(
        eventId = event.eventId,
        title = event.title,
        description = event.description,
        time = event.time,
        date = event.date,
        type = event.type
    )
}

