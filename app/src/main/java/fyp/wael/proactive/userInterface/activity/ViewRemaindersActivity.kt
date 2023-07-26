package fyp.wael.proactive.userInterface.activity

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : ViewRemaindersActivity.kt
// Description      : To allow students to view their added reminders
// First Written on : Wednesday, 31-May-2023
// Edited on        : Friday, 7-Jul-2023

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import fyp.wael.proactive.R
import fyp.wael.proactive.adapters.ViewRemainderAdapter
import fyp.wael.proactive.firebase.FirestoreClass
import fyp.wael.proactive.models.Remainder
import fyp.wael.proactive.utils.Validation.Companion.showToast


class ViewRemaindersActivity : AppCompatActivity() {

    private lateinit var viewRemainderAdapter: ViewRemainderAdapter
    private lateinit var remainderRecyclerView: RecyclerView
    private lateinit var noRemaindersTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_remainders)

        remainderRecyclerView = findViewById(R.id.remainderRecyclerView)
        noRemaindersTextView = findViewById(R.id.noRemaindersTextView)

        // Set up RecyclerView
        viewRemainderAdapter = ViewRemainderAdapter(emptyList())
        remainderRecyclerView.layoutManager = LinearLayoutManager(this)
        remainderRecyclerView.adapter = viewRemainderAdapter

        // Load user remainders from Firestore
        loadUserRemainders()
    }

    private fun loadUserRemainders() {
        val currentUserID = FirestoreClass().getCurrentUserID()

        val query = FirebaseFirestore.getInstance()
            .collection("remainders")
            .whereEqualTo("userId", currentUserID)
            .orderBy("date", Query.Direction.DESCENDING)

        query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                //showToast(this,"An error occurred, please try again later")
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val remainders = mutableListOf<Remainder>()
                for (document in snapshot.documents) {
                    val remainder = document.toObject(Remainder::class.java)
                    if (remainder != null) {
                        remainder.id = document.id
                        remainders.add(remainder)
                    }
                }

                if (remainders.isNotEmpty()) {
                    viewRemainderAdapter.setRemainders(remainders)
                    noRemaindersTextView.visibility = View.GONE
                } else {
                    noRemaindersTextView.visibility = View.VISIBLE
                }
            } else {
                noRemaindersTextView.visibility = View.VISIBLE
            }
        }
    }
}

