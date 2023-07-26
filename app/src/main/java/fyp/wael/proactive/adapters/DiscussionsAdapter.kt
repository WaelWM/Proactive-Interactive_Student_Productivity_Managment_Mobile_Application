package fyp.wael.proactive.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fyp.wael.proactive.R
import fyp.wael.proactive.models.Discussion

class DiscussionsAdapter(
    private val discussions: List<Discussion>
) : RecyclerView.Adapter<DiscussionsAdapter.DiscussionViewHolder>() {

    inner class DiscussionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textDiscussionTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textDiscussionDescription)
        private val createdByTextView: TextView = itemView.findViewById(R.id.textCreatedBy)

        fun bind(discussion: Discussion) {
            titleTextView.text = discussion.title
            descriptionTextView.text = discussion.description
            createdByTextView.text = "Created by: ${discussion.createdBy}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discussion, parent, false)
        return DiscussionViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiscussionViewHolder, position: Int) {
        val discussion = discussions[position]
        holder.bind(discussion)
    }

    override fun getItemCount(): Int = discussions.size
}
