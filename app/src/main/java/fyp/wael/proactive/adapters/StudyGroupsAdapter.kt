package fyp.wael.proactive.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fyp.wael.proactive.R
import fyp.wael.proactive.models.StudySession

class StudyGroupsAdapter(
    private val sessions: MutableList<StudySession>,
    private val onItemClick: (StudySession) -> Unit
) : RecyclerView.Adapter<StudyGroupsAdapter.SessionViewHolder>() {

    private var onJoinClickListener: ((StudySession) -> Unit)? = null

    fun setOnJoinClickListener(listener: (StudySession) -> Unit) {
        onJoinClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_group, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.bind(session)
    }

    override fun getItemCount(): Int = sessions.size

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textDescription)
        private val interestsTextView: TextView = itemView.findViewById(R.id.textInterests)
        private val joinButton: Button = itemView.findViewById(R.id.buttonJoin)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val session = sessions[position]
                    onItemClick(session)
                }
            }

            joinButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val session = sessions[position]
                    onJoinClickListener?.invoke(session)
                }
            }
        }

        fun bind(session: StudySession) {
            titleTextView.text = session.title
            descriptionTextView.text = session.description
            interestsTextView.text = session.interests
        }
    }
}


