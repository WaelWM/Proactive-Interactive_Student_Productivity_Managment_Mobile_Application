package fyp.wael.proactive.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import fyp.wael.proactive.R
import fyp.wael.proactive.models.Event
import fyp.wael.proactive.models.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EventAdapter(
    private val onDeleteClickListener: ((Event) -> Unit)? = null,
    private val onEditClickListener: ((Event) -> Unit)? = null,
    private val onRegisterClickListener: ((Event) -> Unit)? = null
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {


    private val events: MutableList<Event> = mutableListOf()
    private var isAdminView: Boolean = false // Check whether it's an admin view or not
    private lateinit var currentUser: User // Add a currentUser property

    private var originalEvents: List<Event> = mutableListOf()
    private var filteredEvents: List<Event> = mutableListOf()

    init {
        originalEvents = events.toList()
        filteredEvents = originalEvents
    }

    fun setData(eventList: List<Event>) {
        events.clear()
        events.addAll(eventList)
        originalEvents = eventList.toList()
        filteredEvents = originalEvents
        notifyDataSetChanged()
    }

    fun setAdminView(isAdminView: Boolean) {
        this.isAdminView = isAdminView
        notifyDataSetChanged()
    }

    fun setCurrentUser(user: User) {
        currentUser = user
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_events, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
        holder.setButtonVisibility(isAdminView)

        holder.registerButton.setOnClickListener {
            onRegisterClickListener?.invoke(event)
        }
    }

    // Function to filter events by recent added
    fun filterByRecentAdded() {
        filteredEvents = originalEvents.sortedByDescending { event ->
            event.eventId
        }
        notifyDataSetChanged()
    }

    // Function to filter events by recent events
    fun filterByRecentEvents() {
        val currentDate = Calendar.getInstance().time
        filteredEvents = originalEvents.filter { event ->
            val eventDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(event.date)
            eventDate != null && currentDate.before(eventDate)
        }.sortedBy { event ->
            event.date
        }
        notifyDataSetChanged()
    }

    // Function to filter events by already passed events
    fun filterByPassedEvents() {
        val currentDate = Calendar.getInstance().time
        filteredEvents = originalEvents.filter { event ->
            val eventDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(event.date)
            eventDate != null && currentDate.after(eventDate)
        }.sortedByDescending { event ->
            event.date
        }
        notifyDataSetChanged()
    }

    // Function to show all the events without filtration
    fun showAllEvents() {
        filteredEvents = originalEvents
        notifyDataSetChanged()
    }

    fun updateEvent(updatedEvent: Event) {
        val index = events.indexOfFirst { it.eventId == updatedEvent.eventId }
        if (index != -1) {
            events[index] = updatedEvent
            notifyItemChanged(index)
        }
    }

    override fun getItemCount(): Int {
        return filteredEvents.size
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        internal val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        internal val editButton: Button = itemView.findViewById(R.id.editButton)
        internal val registerButton: Button = itemView.findViewById(R.id.registerButton)

        init {
            editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val event = events[position]
                    onEditClickListener?.invoke(event)
                }
            }

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val event = events[position]
                    onDeleteClickListener?.invoke(event)
                }
            }
        }

        fun bind(event: Event) {
            titleTextView.text = event.title
            descriptionTextView.text = event.description
            timeTextView.text = event.time
            dateTextView.text = event.date
            typeTextView.text = event.type
        }

        fun setButtonVisibility(isAdminView: Boolean) {
            if (isAdminView) {
                deleteButton.visibility = View.VISIBLE
                editButton.visibility = View.VISIBLE
                registerButton.visibility = View.GONE
            } else {
                deleteButton.visibility = View.GONE
                editButton.visibility = View.GONE
                registerButton.visibility = View.VISIBLE
            }
        }
    }
}







