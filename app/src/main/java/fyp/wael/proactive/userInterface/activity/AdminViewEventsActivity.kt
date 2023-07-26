package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : AdminViewEventsActivity.kt
// Description      : To allow admin to add events.
// First Written on : Sunday, 9-Jul-2023
// Edited on        : Tuesday, 11-Jul-2023

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import fyp.wael.proactive.R
import fyp.wael.proactive.adapters.EventAdapter
import fyp.wael.proactive.models.Event

class AdminViewEventsActivity : AppCompatActivity() {

    private lateinit var eventAdapter: EventAdapter
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_view_events)

        eventsRecyclerView = findViewById(R.id.eventsRecyclerView)
        progressBar = findViewById(R.id.progressBar)

        eventAdapter = EventAdapter(
            onDeleteClickListener = { event -> showConfirmationDialog(event) },
            onEditClickListener = { event -> openEditEventActivity(event) }
        )

        // Set up RecyclerView
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        eventsRecyclerView.adapter = eventAdapter

        // Call the function to fetch events
        fetchEvents()


    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun fetchEvents() {



        progressBar.visibility = View.VISIBLE // Show the progress bar

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("events")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val eventList = ArrayList<Event>()
                for (document in querySnapshot) {
                    val event = document.toObject(Event::class.java)
                    eventList.add(event)
                }

                // Pass event list to adapter
                eventAdapter.setData(eventList)
                eventAdapter.setAdminView(true) // Set admin view to true

                progressBar.visibility = View.GONE // Hide the progress bar after data is loaded
            }
            .addOnFailureListener { exception ->
                // Handle the error appropriately
                progressBar.visibility = View.GONE // Hide the progress bar on failure
            }
    }

    private fun showConfirmationDialog(event: Event) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to delete this event?")
        builder.setPositiveButton("Yes") { _, _ ->
            deleteEvent(event.eventId) // Passing the event ID
        }
        builder.setNegativeButton("No", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteEvent(eventId: String) {

        if (!isNetworkAvailable()) {
            showToast("No internet connection. Please check your network settings!")
        }

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("events")
            .document(eventId)
            .delete()
            .addOnSuccessListener {
                showToast("Event deleted successfully")
                fetchEvents() // Refresh the event list after deletion
            }
            .addOnFailureListener { exception ->
                showToast("Failed to delete event")
            }
    }

    private fun openEditEventActivity(event: Event) {
        if (!isNetworkAvailable()) {
            showToast("No internet connection. Please check your network settings!")
        }
        // Start the EditEventActivity and pass the event data
        val intent = Intent(this, EditEventActivity::class.java)
        intent.putExtra("event", event)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}


