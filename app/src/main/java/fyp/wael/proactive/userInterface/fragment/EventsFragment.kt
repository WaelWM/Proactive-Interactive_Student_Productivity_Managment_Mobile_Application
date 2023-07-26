package fyp.wael.proactive.userInterface.fragment

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : EventsFragment.kt
// Description      : To allow students vie and register for events
// First Written on : Monday, 12-Jun-2023
// Edited on        : Tuesday, 27-Jun-2023

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fyp.wael.proactive.R
import fyp.wael.proactive.adapters.EventAdapter
import fyp.wael.proactive.models.Event
import fyp.wael.proactive.models.EventRegistration
import fyp.wael.proactive.userInterface.activity.ViewRegisteredEventsActivity
import fyp.wael.proactive.utils.Validation.Companion.showToast
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventsAdapter: EventAdapter
    private lateinit var progressBar: ProgressBar



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)


        val viewRegisteredEventsButton: Button = view.findViewById(R.id.viewRegisteredEventsButton)
        viewRegisteredEventsButton.setOnClickListener {
            val intent = Intent(requireContext(), ViewRegisteredEventsActivity::class.java)
            startActivity(intent)
        }


        recyclerView = view.findViewById(R.id.eventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        eventsAdapter = EventAdapter(onRegisterClickListener = { event ->
            showRegistrationConfirmation(event)
        })
        progressBar = view.findViewById(R.id.progressBar)

        setHasOptionsMenu(true) // Enable options menu for filtering

        // Fetch events and populate the adapter
        fetchEvents()

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_filter_recent_added -> eventsAdapter.filterByRecentAdded()
            R.id.menu_filter_recent_events -> eventsAdapter.filterByRecentEvents()
            R.id.menu_filter_passed_events -> eventsAdapter.filterByPassedEvents()
            R.id.menu_show_all_events -> eventsAdapter.showAllEvents()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }



    private fun fetchEvents() {
        progressBar.visibility = View.VISIBLE // To Show the progress bar

        val firestore = FirebaseFirestore.getInstance() // To get the instance of the data from firebase

        firestore.collection("events")//Fetching data from the database collection
            .get()
            .addOnSuccessListener { querySnapshot ->
                val events = ArrayList<Event>()
                for (document in querySnapshot) {
                    val event = document.toObject(Event::class.java) // getting the objects from
                                                                    //the data class
                    events.add(event) // Making the events be displayed on the recyclerView
                }
                eventsAdapter.setData(events) // adding the object data to the eventsAdapter class
                recyclerView.adapter = eventsAdapter // Adding the data to the recyclerview
                                                     // from the eventsAdapter class

                progressBar.visibility = View.GONE // Hide the progress bar after data is loaded
            }
            .addOnFailureListener { exception ->
                // if failed to retrieve data, show an error message
                showToast(requireContext(), "Failed to retrieve data!," +
                        " please try again later $exception")
                progressBar.visibility = View.GONE // Hide the progress bar on failure
            }
    }

    private fun showRegistrationConfirmation(event: Event) {
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val eventDate = dateFormat.parse(event.date)

            if (eventDate != null && currentDate.after(eventDate)) {
                // Event date has already passed, notify the user and don't show the confirmation dialog.
                Toast.makeText(
                    requireContext(),
                    "This event has already passed, registration is closed.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

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
                        if (querySnapshot.isEmpty) {
                            // User is not registered for the event, show the registration confirmation dialog.
                            val alertDialogBuilder = AlertDialog.Builder(requireContext())
                            alertDialogBuilder.setTitle("Registration Confirmation")
                            alertDialogBuilder.setMessage("Are you sure you want to register for this event?")
                            alertDialogBuilder.setPositiveButton("Register") { _, _ ->
                                registerForEvent(event)
                            }
                            alertDialogBuilder.setNegativeButton("Cancel", null)
                            val alertDialog = alertDialogBuilder.create()
                            alertDialog.show()
                        } else {
                            // User is already registered for the event
                            Toast.makeText(
                                requireContext(),
                                "You are already registered for this event.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            requireContext(),
                            "Registration failed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } catch (e: ParseException) {
            // Handle the date parsing exception, if any.
            Toast.makeText(requireContext(), "Error parsing event date. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerForEvent(event: Event) {
        val firebaseAuth = FirebaseAuth.getInstance() // Getting an instance of the firebase authentication
        val currentUser = firebaseAuth.currentUser    // Getting the current user data for reference

        if (currentUser != null) { // getting the user id and email for the registration of event
            val userId = currentUser.uid
            val userEmail = currentUser.email

            val registrationData = // creating a data object on the EventRegistration Data class
                EventRegistration(event.eventId, event.title, userId, userEmail ?: "")

            // Adding the entered data into the Firebase database collection with only one data per student
            val firestore = FirebaseFirestore.getInstance()
            val registrationQuery = firestore.collection("EventRegistration")
                .whereEqualTo("eventId", event.eventId)
                .whereEqualTo("userId", userId)
                .limit(1)

            //addOnSuccessListener and data validations
            registrationQuery.get()
                .addOnSuccessListener { querySnapshot ->
                    val currentDate = Calendar.getInstance().time
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    try {
                        val eventDate = dateFormat.parse(event.date)

                        if (eventDate != null && currentDate.after(eventDate)) {
                            // Event date has already passed, notify the user and don't proceed with registration.
                            Toast.makeText(
                                requireContext(),
                                "This event has already passed, registration is closed.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@addOnSuccessListener
                        }

                        if (querySnapshot.isEmpty) {
                            // User is not registered for the event, proceed with registration
                            val eventRegistrationCollection =
                                firestore.collection("EventRegistration")
                            val registrationId = eventRegistrationCollection.document().id
                            val registrationWithId =
                                registrationData.copy(registrationId = registrationId)

                            eventRegistrationCollection.document(registrationId)
                                .set(registrationWithId)
                                .addOnSuccessListener {
                                    showToast(requireContext(),
                                        "You have registered for event:" +
                                                " ${event.title} successfully!")
                                }
                                .addOnFailureListener { exception ->
                                    showToast(requireContext(),
                                        "Registration failed. Please try again.$exception")
                                }
                        } else {
                            // User is already registered for the event
                            showToast(requireContext(),
                                "You are already registered for this event.")
                        }

                    } catch (e: ParseException) {
                        // Handle the date parsing exception, if any occurred.
                        showToast(requireContext(),
                            "Error parsing event date. Please try again.${e}")
                    }
                }
                .addOnFailureListener { exception ->
                    showToast(requireContext(),
                        "Registration failed. Please try again.$exception")
                }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = EventsFragment()
    }
}








