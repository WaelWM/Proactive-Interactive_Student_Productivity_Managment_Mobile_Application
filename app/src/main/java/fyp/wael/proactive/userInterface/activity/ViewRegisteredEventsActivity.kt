package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : ViewRegisteredEventsActivity.kt
// Description      : To allow students to view their registered events
// First Written on : Wednesday, 31-May-2023
// Edited on        : Friday, 7-Jul-2023

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fyp.wael.proactive.R
import fyp.wael.proactive.adapters.RegisteredEventAdapter
import fyp.wael.proactive.models.Event



class ViewRegisteredEventsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var registeredEventAdapter: RegisteredEventAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_registered_events)

        recyclerView = findViewById(R.id.registeredEventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        registeredEventAdapter = RegisteredEventAdapter(onCancelClickListener = { event ->
            showCancellationConfirmation(event)
        })
        recyclerView.adapter = registeredEventAdapter

        progressBar = findViewById(R.id.progressBar)

        // Fetch registered events and populate the adapter
        fetchRegisteredEvents()
    }

    private fun fetchRegisteredEvents() {
        progressBar.visibility = View.VISIBLE // Show the progress bar

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("EventRegistration")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val registeredEvents = ArrayList<Event>()
                    for (document in querySnapshot) {
                        val event = document.toObject(Event::class.java)
                        registeredEvents.add(event)
                    }
                    registeredEventAdapter.setData(registeredEvents)

                    progressBar.visibility = View.GONE // Hide the progress bar after data is loaded
                }
                .addOnFailureListener { exception ->
                    // Handle the error appropriately

                    progressBar.visibility = View.GONE // Hide the progress bar on failure
                }
        }
    }

    private fun showCancellationConfirmation(event: Event) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Cancellation Confirmation")
        alertDialogBuilder.setMessage("Are you sure you want to cancel this event?")
        alertDialogBuilder.setPositiveButton("Cancel") { _, _ ->
            removeEvent(event)
        }
        alertDialogBuilder.setNegativeButton("Keep", null)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun removeEvent(event: Event) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            val firestore = FirebaseFirestore.getInstance()
            val registrationQuery = firestore.collection("EventRegistration")
                .whereEqualTo("eventId", event.eventId)
                .whereEqualTo("userId", userId)
                .limit(1)

            registrationQuery.get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // User is registered for the event, proceed with cancellation
                        val registrationDocument = querySnapshot.documents[0]
                        val registrationId = registrationDocument.id

                        // Remove event registration document from Firebase Firestore
                        firestore.collection("EventRegistration")
                            .document(registrationId)
                            .delete()
                            .addOnSuccessListener {
                                // Remove event from the adapter
                                registeredEventAdapter.removeEvent(event)

                                Toast.makeText(
                                    this,
                                    "Event cancellation successful.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    this,
                                    "Event cancellation failed. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        // User is not registered for the event
                        Toast.makeText(
                            this,
                            "You are not registered for this event.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Event cancellation failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}













