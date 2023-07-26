package fyp.wael.proactive.userInterface.fragment

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : StudySessionFragment.kt
// Description      : To allow students to start personal study session with study and break durations
// First Written on : Tuesday, 23-may-2023
// Edited on        : Monday,  17-Jul-2023

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import fyp.wael.proactive.R
import android.provider.Settings
import androidx.core.app.ActivityCompat
import fyp.wael.proactive.databinding.FragmentStudySessionBinding

class StudySessionFragment : Fragment() {

    private var _binding: FragmentStudySessionBinding? = null
    private val binding get() = _binding!!


    private lateinit var studyTimer: CountDownTimer
    private lateinit var breakTimer: CountDownTimer

    private val studyDurationOptions = arrayOf(1, 20, 25, 40, 60)
    private val breakDurationOptions = arrayOf(2, 15, 20)

    private var currentStudyDuration = studyDurationOptions[0]
    private var currentBreakDuration = breakDurationOptions[0]

    private var isStudyTimerRunning = false
    private var isBreakTimerRunning = false

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentStudySessionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupStudyDurationSpinner()
        setupBreakDurationSpinner()

        binding.btnStart.setOnClickListener {
            if (isStudyTimerRunning || isBreakTimerRunning) {
                stopTimer()
            } else {
                if (isNotificationPermissionGranted()) {
                    startStudyTimer()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        requestNotificationPermission()
                    }
                }
            }
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
        _binding = null
    }

    private fun stopTimer() {
        if (isStudyTimerRunning) {
            studyTimer.cancel()
            isStudyTimerRunning = false
        }

        if (isBreakTimerRunning) {
            breakTimer.cancel()
            isBreakTimerRunning = false
        }
    }

    private fun setupStudyDurationSpinner() {
        val studyDurationAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            studyDurationOptions.map { it.toString() }
        )
        studyDurationAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.spinnerStudyDuration.adapter = studyDurationAdapter

        binding.spinnerStudyDuration.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    currentStudyDuration = studyDurationOptions[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupBreakDurationSpinner() {
        val breakDurationAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            breakDurationOptions.map { it.toString() }
        )
        breakDurationAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.spinnerBreakDuration.adapter = breakDurationAdapter

        binding.spinnerBreakDuration.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    currentBreakDuration = breakDurationOptions[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    @SuppressLint("SetTextI18n")
    private fun startStudyTimer() {
        stopTimer() //stopping the existing timer if the student clicks the start button again

        binding.tvStudyTimer.visibility = View.VISIBLE // making the study timer text view visible
        binding.tvSessionType.visibility = View.VISIBLE // making the study session type text view visible
        binding.tvSessionType.text = "Study Session"    // putting the study type session as study session text view

        // Creating a CountDownTimer object for the study session
        studyTimer = object : CountDownTimer(currentStudyDuration * 60 * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                // Updating the UI to display the remaining time in the format MM:SS
                binding.tvStudyTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvStudyTimer.visibility = View.GONE //Hide the text view when the study session finishes
                binding.tvBreakTimer.visibility = View.VISIBLE
                binding.tvSessionType.text = "Break Session" // changing the text view to break session
                startBreakTimer() // calling the function
                // Showing the notification once the study session finishes
                showNotification("Break Time", "Take a break now!")
            }
        }
        //starting the study timer session again once the break session finishes and creating the loop
        studyTimer.start()
        isStudyTimerRunning = true
    }

    @SuppressLint("SetTextI18n")
    private fun startBreakTimer() {
        stopTimer()

        binding.tvBreakTimer.visibility = View.VISIBLE
        binding.tvSessionType.visibility = View.VISIBLE
        binding.tvSessionType.text = "Break Session"

        breakTimer = object : CountDownTimer(currentBreakDuration * 60 * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvBreakTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvBreakTimer.visibility = View.GONE
                binding.tvSessionType.text = "Study Session"
                startStudyTimer()
                showNotification("Study Time", "Get back to studying!")
            }
        }

        breakTimer.start()
        isBreakTimerRunning = true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun isNotificationPermissionGranted(): Boolean {
        val manager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return manager.areNotificationsEnabled()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
        startActivityForResult(intent, NOTIFICATION_PERMISSION_REQUEST_CODE)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = getString(R.string.study_session_channel)
        val channelName = getString(R.string.study_session_channel_name)
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (isNotificationPermissionGranted()) {
                startStudyTimer()
            } else {
                // Notification permission denied, handle it accordingly
                Toast.makeText(requireContext(),
                    "Notification permission denied, please Enable it to start using the timer",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}

