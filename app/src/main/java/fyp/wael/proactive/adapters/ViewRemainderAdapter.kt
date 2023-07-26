package fyp.wael.proactive.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import fyp.wael.proactive.R
import fyp.wael.proactive.models.Remainder
import fyp.wael.proactive.utils.Validation.Companion.isNetworkAvailable
import fyp.wael.proactive.utils.Validation.Companion.showToast

class ViewRemainderAdapter(private var remainders: List<Remainder>) :
    RecyclerView.Adapter<ViewRemainderAdapter.ViewRemainderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewRemainderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_remainder, parent, false)
        return ViewRemainderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewRemainderViewHolder, position: Int) {
        val remainders = remainders[position]
        holder.bind(remainders)
    }

    override fun getItemCount(): Int {
        return remainders.size
    }

    fun setRemainders(remainders: List<Remainder>) {
        this.remainders = remainders
        notifyDataSetChanged()
    }

    inner class ViewRemainderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(remainder: Remainder) {
            titleTextView.text = remainder.title
            descriptionTextView.text = remainder.description
            dateTextView.text = remainder.date
            timeTextView.text = remainder.time

            deleteButton.setOnClickListener {
                if (!isNetworkAvailable(itemView.context)) {
                    showToast(itemView.context,"No internet connection. " +
                            "Please check your internet settings!")
                }
                else{
                    deleteRemainder(remainder)
                }

            }
        }

        private fun deleteRemainder(remainder: Remainder) {

            val alertDialogBuilder = AlertDialog.Builder(itemView.context)
            alertDialogBuilder.setTitle("Delete Reminder")
            alertDialogBuilder.setMessage("Are you sure you want to delete this reminder?")
            alertDialogBuilder.setPositiveButton("Delete") { _, _ ->
                val firestore = FirebaseFirestore.getInstance()
                val remainderRef = firestore.collection("remainders").document(remainder.id)

                remainderRef.delete()
                    .addOnSuccessListener {
                        val updatedRemainders = remainders.toMutableList()
                        updatedRemainders.remove(remainder)
                        setRemainders(updatedRemainders)
                        showToast(itemView.context,"Reminder deleted successfully")
                    }
                    .addOnFailureListener { e ->
                        showToast(itemView.context,"Failed to delete reminder," +
                                " please try again later!")
                    }
            }
            alertDialogBuilder.setNegativeButton("Cancel") { _, _ ->
                //dialog will be dismissed automatically
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }
}


