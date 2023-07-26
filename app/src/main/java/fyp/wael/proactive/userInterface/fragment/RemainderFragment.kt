package fyp.wael.proactive.userInterface.fragment

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : RemainderFragment.kt
// Description      : To allow students to add reminders
// First Written on : Monday, 22-May-2023
// Edited on        : Thursday, 6-Jul-2023

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import fyp.wael.proactive.databinding.FragmentRemainderBinding
import fyp.wael.proactive.firebase.FirestoreClass
import fyp.wael.proactive.models.Remainder
import fyp.wael.proactive.userInterface.activity.ViewRemaindersActivity
import fyp.wael.proactive.utils.Validation.Companion.isNetworkAvailable
import fyp.wael.proactive.utils.Validation.Companion.showToast
import java.text.SimpleDateFormat
import java.util.*

class RemainderFragment : Fragment() {

    private var _binding: FragmentRemainderBinding? = null
    private val binding get() = _binding!!

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRemainderBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val viewRemindersBtn: Button = binding.viewRemindersBtn
        viewRemindersBtn.setOnClickListener{

            val intent = Intent(requireContext(), ViewRemaindersActivity::class.java)
            startActivity(intent)
        }

        val saveButton: Button = binding.saveButton
        saveButton.setOnClickListener {
            if (isInternetAvailable()) {
                saveRemainder()
            } else {
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            }
        }

        val cancelButton: Button = binding.cancelButton
        cancelButton.setOnClickListener {
            clearFields()
        }

        return root
    }


    private fun saveRemainder() {

        if (!isNetworkAvailable(requireContext())) {
            showToast(requireContext(),"No internet connection. Please check your network settings!")
        }

        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val date = getDateFromDatePicker(binding.datePicker)
        val time = getTimeFromTimePicker(binding.timePicker)

        // Check if title and description are not empty
        if (title.isEmpty()) {
            binding.titleEditText.error = "Please fill the Title field!"
            binding.titleEditText.requestFocus()
            return
        } else if (description.isEmpty()) {
            binding.descriptionEditText.error = "Please fill the Description field!"
            binding.descriptionEditText.requestFocus()
            return
        }

        val currentUserID = FirestoreClass().getCurrentUserID()

        val remainder = Remainder("", title, description, date, time, currentUserID)

        // Generate a unique ID for the Remainder
        val remainderId = firestore.collection("remainders").document().id
        remainder.id = remainderId

        // Save the remainder in Firestore
        firestore.collection("remainders")
            .document(remainderId)
            .set(remainder)
            .addOnSuccessListener {
                // Handle the remainder addition or any other UI updates
                showToast(requireContext(),"Reminder added successfully!")
                clearFields()
            }
            .addOnFailureListener { exception ->
                showToast(requireContext(), "Failed to add reminder")
            }
            .addOnCompleteListener {
            }
    }


    private fun clearFields() {
        binding.titleEditText.text.clear()
        binding.descriptionEditText.text.clear()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getDateFromDatePicker(datePicker: DatePicker): String {
        val day = datePicker.dayOfMonth
        val month = datePicker.month
        val year = datePicker.year

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        return dateFormat.format(calendar.time)
    }

    private fun getTimeFromTimePicker(timePicker: TimePicker): String {
        val hour = timePicker.hour
        val minute = timePicker.minute

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        return timeFormat.format(calendar.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}







