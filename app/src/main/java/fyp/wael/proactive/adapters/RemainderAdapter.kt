package fyp.wael.proactive.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fyp.wael.proactive.R
import fyp.wael.proactive.models.Remainder

class RemainderAdapter(private var remainders: List<Remainder>) : RecyclerView.Adapter<RemainderAdapter.RemainderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemainderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_remainder, parent, false)
        return RemainderViewHolder(view)
    }

    override fun onBindViewHolder(holder: RemainderViewHolder, position: Int) {
        val remainder = remainders[position]
        holder.bind(remainder)
    }

    override fun getItemCount(): Int {
        return remainders.size
    }

    fun setRemainders(newRemainders: List<Remainder>) {
        remainders = newRemainders
        notifyDataSetChanged()
    }

    inner class RemainderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.remainderTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.remainderDescription)

        fun bind(remainder: Remainder) {
            titleTextView.text = remainder.title
            descriptionTextView.text = remainder.description
        }
    }
}



