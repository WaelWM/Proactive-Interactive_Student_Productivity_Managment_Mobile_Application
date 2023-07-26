package fyp.wael.proactive.userInterface.fragment

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : StudyGroupFragment.kt
// Description      : To allow students view, create, and manage study group sessions
// First Written on : Sunday, 9-Jul-2023
// Edited on        : Friday, 21-Jul-2023

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fyp.wael.proactive.R
import fyp.wael.proactive.adapters.StudyGroupsAdapter
import fyp.wael.proactive.databinding.FragmentStudyGroupsBinding
import fyp.wael.proactive.firebase.FirestoreClass
import fyp.wael.proactive.models.StudySession
import fyp.wael.proactive.models.User
import fyp.wael.proactive.userInterface.activity.StudySessionActivity
import fyp.wael.proactive.utils.Validation
import fyp.wael.proactive.utils.Validation.Companion.internetValidation
import fyp.wael.proactive.utils.Validation.Companion.isNetworkAvailable
import fyp.wael.proactive.utils.Validation.Companion.showToast

class StudyGroupsFragment : Fragment() {

    private var _binding: FragmentStudyGroupsBinding? = null
    private val binding get() = _binding!!

    private val studySessions = mutableListOf<StudySession>()
    private lateinit var adapter: StudyGroupsAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyGroupsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        progressBar = binding.progressBar

        // Initialize the adapter
        adapter = StudyGroupsAdapter(studySessions) { session ->
            navigateToStudySession(session)
        }

        adapter.setOnJoinClickListener { session ->
            val currentUserID = FirestoreClass().getCurrentUserID()

            if (session.participants.size >= 5) {
                // Session is already full
                Toast.makeText(requireContext(), "Session is full", Toast.LENGTH_SHORT).show()
            } else {
                if (!session.participants.contains(currentUserID)) {
                    // Add the current user to the session participants
                    session.participants.add(currentUserID)

                    // Update the UI
                    adapter.notifyDataSetChanged()

                    // Save session to Firestore
                    saveStudySessionToFirestore(session)
                }

                // Navigate to the study session screen
                navigateToStudySession(session)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = this@StudyGroupsFragment.adapter
        }

        binding.fabCreateSession.setOnClickListener {
            showCreateSessionDialog()
        }

        firestore = FirebaseFirestore.getInstance()

        // Load study sessions from Firestore
        loadStudySessionsFromFirestore()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showCreateSessionDialog() {

        internetValidation(requireContext())

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_create_session)
        dialog.setTitle("Create Study Session")

        val titleEditText = dialog.findViewById<EditText>(R.id.editTitle)
        val descriptionEditText = dialog.findViewById<EditText>(R.id.editDescription)
        val interestsEditText = dialog.findViewById<EditText>(R.id.editInterests)

        val cancelButton = dialog.findViewById<Button>(R.id.btnCancel)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        val createButton = dialog.findViewById<Button>(R.id.btnCreate)
        createButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val interests = interestsEditText.text.toString().trim()

            if (title.isEmpty()) {
                titleEditText.error = "Please enter a title"
                titleEditText.requestFocus()
                return@setOnClickListener
            }

            else if (description.isEmpty()) {
                descriptionEditText.error = "Please enter a description"
                descriptionEditText.requestFocus()
                return@setOnClickListener
            }

            else if (interests.isEmpty()) {
                interestsEditText.error = "Please enter interests"
                interestsEditText.requestFocus()
                return@setOnClickListener
            }

            val currentUserID = FirestoreClass().getCurrentUserID()
            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

            val session = StudySession(
                title = title,
                description = description,
                interests = interests,
                participants = mutableListOf(currentUserID),
                userId = currentUserID,
                userEmail = currentUserEmail
            )

            saveStudySessionToFirestore(session)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun saveStudySessionToFirestore(session: StudySession) {


        internetValidation(requireContext()) // To validate if there is an internet connection

        val firestore = FirebaseFirestore.getInstance()
        val collectionRef = firestore.collection("study_sessions")

        // Adding the study session to the Firebase Colleciton
        val query = collectionRef.whereEqualTo("title", session.title)
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result?.documents
                if (!documents.isNullOrEmpty()) {
                    // Session already exists, update it
                    val documentId = documents[0].id
                    collectionRef.document(documentId)
                        .set(session)
                        .addOnSuccessListener {
                            showToast(requireContext(),"Study session updated with ID: $documentId")
                            session.id = documentId
                            studySessions.add(session)
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { exception ->
                            showToast(requireContext(),"Error updating study session")
                        }
                } else {
                    // Session doesn't exist, create a new one
                    collectionRef.add(session)
                        .addOnSuccessListener { documentReference ->
                            showToast(requireContext(),
                                "Study session saved with ID: ${documentReference.id}")
                            session.id = documentReference.id
                            studySessions.add(session)
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { exception ->
                            showToast(requireContext(),
                                "Error saving study session," +
                                        "please try again later${task.exception}")
                        }
                }
            } else {
                showToast(requireContext(), "Error querying study session${task.exception}")
            }
        }
    }

    private fun loadStudySessionsFromFirestore() {

        progressBar.visibility = View.VISIBLE

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("study_sessions")
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                studySessions.clear()
                for (document in documents) {
                    val session = document.toObject(StudySession::class.java)
                    session.id = document.id  // Set the document id to the session id
                    studySessions.add(session)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Log.e(TAG, "Error loading study sessions", exception)
            }
    }

    private fun navigateToStudySession(session: StudySession) {
        val intent = Intent(requireContext(), StudySessionActivity::class.java)
        intent.putExtra("session", session)
        startActivity(intent)
    }
}


