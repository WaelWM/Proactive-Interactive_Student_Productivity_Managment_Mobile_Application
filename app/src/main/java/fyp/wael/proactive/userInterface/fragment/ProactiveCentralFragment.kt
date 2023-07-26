package fyp.wael.proactive.userInterface.fragment

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : ProactiveCentralFragment.kt
// Description      : To allow students browse knowledge base uploaded contents
// First Written on : Tuesday, 11-Jul-2023
// Edited on        : Thursday, 13-Jul-2023

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import fyp.wael.proactive.R
import fyp.wael.proactive.models.KnowledgeBase

class ProactiveCentralFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var knowledgeBaseAdapter: fyp.wael.proactive.adapters.KnowledgeBaseAdapter
    private lateinit var knowledgeBaseList: MutableList<KnowledgeBase>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_proactive_central, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        knowledgeBaseList = mutableListOf()
        knowledgeBaseAdapter = fyp.wael.proactive.adapters.KnowledgeBaseAdapter(knowledgeBaseList)
        recyclerView.adapter = knowledgeBaseAdapter

        loadKnowledgeBase()

        return view
    }

    private fun loadKnowledgeBase() {
        val db = FirebaseFirestore.getInstance()
        val imageCollection = db.collection("KnowledgeBase")
            .orderBy("name", Query.Direction.DESCENDING)

        imageCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle the error
                return@addSnapshotListener
            }

            knowledgeBaseList.clear()

            if (snapshot != null) {
                for (document in snapshot.documents) {
                    val name = document.getString("name")
                    val url = document.getString("url")
                    val imageId = document.getString("imageId")

                    if (name != null && url != null && imageId != null) {
                        val knowledgeBase = KnowledgeBase(name, url, imageId)
                        knowledgeBaseList.add(knowledgeBase)
                    }
                }
                knowledgeBaseAdapter.notifyDataSetChanged()
            }
        }
    }
}
