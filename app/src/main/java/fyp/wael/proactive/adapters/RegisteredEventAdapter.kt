package fyp.wael.proactive.adapters

import android.icu.text.CaseMap.Title
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import fyp.wael.proactive.R
import fyp.wael.proactive.models.Event

class RegisteredEventAdapter(
    private val onCancelClickListener: (Event) -> Unit
) : RecyclerView.Adapter<RegisteredEventAdapter.EventViewHolder>() {

    private val eventList = ArrayList<Event>()

    fun setData(events: List<Event>) {
        eventList.clear()
        eventList.addAll(events)
        notifyDataSetChanged()
    }

    fun removeEvent(event: Event) {
        val position = eventList.indexOf(event)
        if (position != -1) {
            eventList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registered_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        private val cancelButton: Button = itemView.findViewById(R.id.cancelButton)

        fun bind(event: Event) {
            titleTextView.text = event.title
            // Fetch and display the event details
            fetchEventDetails(event.eventId)

            // Set click listener for cancel button
            cancelButton.setOnClickListener {
                onCancelClickListener.invoke(event)
            }
        }

        private fun fetchEventDetails(eventId: String) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val event = documentSnapshot.toObject(Event::class.java)
                    if (event != null) {
                        titleTextView.text = event.title
                        descriptionTextView.text = event.description
                        timeTextView.text = event.time
                        dateTextView.text = event.date
                        typeTextView.text = event.type
                    }
                }
                .addOnFailureListener { exception ->
                }
        }
    }
}




