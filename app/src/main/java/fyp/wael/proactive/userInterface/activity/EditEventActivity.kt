package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : EditEventsActivity.kt
// Description      : To allow admin to edit events.
// First Written on : Wednesday, 12-Jul-2023
// Edited on        : Friday, 14-Jul-2023

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import fyp.wael.proactive.R
import fyp.wael.proactive.models.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditEventActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var progressDialogLayout: View

    private lateinit var event: Event

    // Variables to store the chosen time and date
    private var selectedTime: String? = null
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        timeEditText = findViewById(R.id.timeEditText)
        dateEditText = findViewById(R.id.dateEditText)
        typeEditText = findViewById(R.id.typeEditText)
        updateButton = findViewById(R.id.updateButton)
        progressDialogLayout = findViewById(R.id.progressDialogLayout)

        // Set click listeners for timeEditText and dateEditText
        timeEditText.setOnClickListener {
            showTimePickerDialog()
        }


        dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        event = intent.getSerializableExtra("event") as Event

        populateEventData()

        updateButton.setOnClickListener {
            updateEvent()
            val intent = Intent(this, AdminViewEventsActivity::class.java)
            intent.putExtra("event", event)
            startActivity(intent)
            finish()
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val selectedTimeStr = formatTime(hourOfDay, minute)
                selectedTime = selectedTimeStr
                timeEditText.setText(selectedTimeStr)
            },
            hour,
            minute,
            false // Useing 12-hour format instead of 24-hour format
        )

        timePickerDialog.show()
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val selectedDateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                selectedDate = selectedDateStr
                dateEditText.setText(selectedDateStr)
            },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.show()
    }

    private fun showProgressDialog() {
        progressDialogLayout.visibility = View.VISIBLE
    }

    private fun dismissProgressDialog() {
        progressDialogLayout.visibility = View.GONE
    }

    private fun populateEventData() {
        titleEditText.setText(event.title)
        descriptionEditText.setText(event.description)
        timeEditText.setText(event.time)
        dateEditText.setText(event.date)
        typeEditText.setText(event.type)
    }

    private fun updateEvent() {
        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val time = timeEditText.text.toString().trim()
        val date = dateEditText.text.toString().trim()
        val type = typeEditText.text.toString().trim()

        var hasError = false

        if (title.isEmpty()) {
            titleEditText.error = "Title cannot be empty"
            hasError = true
        }

        if (description.isEmpty()) {
            descriptionEditText.error = "Description cannot be empty"
            hasError = true
        }

        if (time.isEmpty()) {
            timeEditText.error = "Time cannot be empty"
            hasError = true
        }

        if (date.isEmpty()) {
            dateEditText.error = "Date cannot be empty"
            hasError = true
        }

        if (type.isEmpty()) {
            typeEditText.error = "Type cannot be empty"
            hasError = true
        }

        if (hasError) {
            return dismissProgressDialog()
        }

        event.title = title
        event.description = description
        event.time = time
        event.date = date
        event.type = type

        if (!isNetworkAvailable()) {
            dismissProgressDialog()
            showToast("No internet connection. Please check your internet settings!")
        }

        showProgressDialog()
        // Update the event in the database
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("events")
            .document(event.eventId)
            .update(
                "title", event.title,
                "description", event.description,
                "time", event.time,
                "date", event.date,
                "type", event.type
            )
            .addOnSuccessListener {
                dismissProgressDialog()
                showToast("Event updated successfully")
            }
            .addOnFailureListener { e ->
                dismissProgressDialog()
                // Error occurred while updating event
                if (e.message != null && e.message!!.contains("offline")) {
                    showToast("No internet connection")
                } else {
                    showToast("Failed to update event: ${e.message}")
                }
            }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}



