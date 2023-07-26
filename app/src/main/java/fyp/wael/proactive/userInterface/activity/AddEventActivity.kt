package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : AddEventActivity.kt
// Description      : To allow the admin to add events.
// First Written on : Friday, 30-Jun-2023
// Edited on        : Saturday, 15-Jul-2023

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import fyp.wael.proactive.R
import fyp.wael.proactive.models.Event
import fyp.wael.proactive.utils.Validation
import fyp.wael.proactive.utils.Validation.Companion.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEventActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var progressDialogLayout: View
    private lateinit var addButton: Button

    private val firestore = FirebaseFirestore.getInstance()

    // Variables to store the chosen time and date
    private var selectedTime: String? = null
    private var selectedDate: String? = null

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        timeEditText = findViewById(R.id.timeEditText)
        dateEditText = findViewById(R.id.dateEditText)
        typeEditText = findViewById(R.id.typeEditText)
        addButton = findViewById(R.id.addButton)
        progressDialogLayout = findViewById(R.id.progressDialogLayout)

        // Set click listeners for timeEditText and dateEditText
        timeEditText.setOnClickListener {
            showTimePickerDialog()
        }

        dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        addButton.setOnClickListener {
            addEvent()
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
    private fun addEvent() {


        if (!Validation.isNetworkAvailable(this)) {
            showToast(this,"No internet connection. Please check your internet settings!")
        }

        showProgressDialog() //Showing the progress dialog until the event is successfully added

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

        val event = Event(
            title = title,
            description = description,
            time = time,
            date = date,
            type = type
        )

        // Generate a unique ID for the event
        val eventId = firestore.collection("events").document().id
        event.eventId = eventId // Set the event ID

        firestore.collection("events")
            .document(eventId)
            .set(event)
            .addOnSuccessListener {
                dismissProgressDialog()
                showToast(this,"Event has been added successfully")
                val intent = Intent(this@AddEventActivity,
                    AdminViewEventsActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                dismissProgressDialog()
                showToast(this,"Failed to add event: ${e.message}")
            }
    }


}

