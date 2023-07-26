package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : StudySessionActivity.kt
// Description      : To allow students to view the joined group study session
// First Written on : Saturday, 1-Jul-2023
// Edited on        : Saturday, 15-Jul-2023

import android.app.Dialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import fyp.wael.proactive.R
import fyp.wael.proactive.models.StudySession
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import fyp.wael.proactive.adapters.DiscussionsAdapter
import fyp.wael.proactive.models.Discussion
import fyp.wael.proactive.models.User
import fyp.wael.proactive.utils.Validation
import fyp.wael.proactive.utils.Validation.Companion.isNetworkAvailable
import fyp.wael.proactive.utils.Validation.Companion.showToast

class StudySessionActivity : AppCompatActivity() {

    private lateinit var session: StudySession
    private lateinit var discussionsAdapter: DiscussionsAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var discussionsListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_session)

        // Retrieve the session details from the intent
        session = intent.getParcelableExtra<StudySession>("session") as StudySession

        // Update the UI with the session details
        updateUI()

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up the discussions listener
        discussionsListener = firestore.collection("study_sessions")
            .document(session.id)
            .collection("discussions")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                // Clear existing discussions
                session.discussions.clear()

                // Add new discussions from Firestore
                snapshots?.forEach { document ->
                    val discussion = document.toObject(Discussion::class.java)
                    session.discussions.add(discussion)
                }

                // Notify the adapter of the changes
                discussionsAdapter.notifyDataSetChanged()
            }

        // Initialize the discussions adapter
        discussionsAdapter = DiscussionsAdapter(session.discussions)

        // Set up the discussions RecyclerView
        val discussionsRecyclerView: RecyclerView = findViewById(R.id.recyclerViewDiscussions)
        discussionsRecyclerView.layoutManager = LinearLayoutManager(this)
        discussionsRecyclerView.adapter = discussionsAdapter

        // Set up the button click listener to create a new discussion
        val createDiscussionButton: ImageButton = findViewById(R.id.buttonCreateDiscussion)
        createDiscussionButton.setOnClickListener {
            showCreateDiscussionDialog()
        }
    }

    private fun updateUI() {
        findViewById<TextView>(R.id.textTitle)?.text = session.title
        findViewById<TextView>(R.id.textDescription)?.text = session.description
        findViewById<TextView>(R.id.textInterests)?.text = session.interests
    }

    private fun showCreateDiscussionDialog() {

        Validation.internetValidation(this)

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_create_discussion)

        val btnCancel: Button = dialog.findViewById(R.id.btnCancel)
        val btnCreate: Button = dialog.findViewById(R.id.btnCreate)
        val titleEditText: EditText = dialog.findViewById(R.id.editDiscussionTitle)
        val descriptionEditText: EditText = dialog.findViewById(R.id.editDiscussionDescription)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnCreate.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            if (title.isEmpty()) {
                titleEditText.error = "Please enter a title"
                titleEditText.requestFocus()
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                descriptionEditText.error = "Please enter a description"
                descriptionEditText.requestFocus()
                return@setOnClickListener
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            val createdBy = currentUser?.email ?: ""

            val discussion = Discussion(
                title = title,
                description = description,
                createdBy = createdBy
            )

            // Add the new discussion to Firestore
            addDiscussionToFirestore(discussion)
            dialog.dismiss()
        }

        dialog.show()
    }



    private fun addDiscussionToFirestore(discussion: Discussion) {

        Validation.internetValidation(this)

        val discussionsCollection = firestore.collection("study_sessions")
            .document(session.id)
            .collection("discussions")

        discussionsCollection.add(discussion)
            .addOnSuccessListener { documentReference ->
                showToast(this, "Discussion Created Successfully!")
            }
            .addOnFailureListener { exception ->
                showToast(this, "Error Adding Discussion," +
                        " please try again later!$exception")
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        discussionsListener.remove()
    }
}




