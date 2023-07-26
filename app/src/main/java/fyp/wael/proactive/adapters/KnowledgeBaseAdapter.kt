package fyp.wael.proactive.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fyp.wael.proactive.R
import fyp.wael.proactive.models.KnowledgeBase

class KnowledgeBaseAdapter(private val knowledgeBaseList: List<KnowledgeBase>) :
    RecyclerView.Adapter<KnowledgeBaseAdapter.KnowledgeBaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KnowledgeBaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return KnowledgeBaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: KnowledgeBaseViewHolder, position: Int) {
        val image = knowledgeBaseList[position]
        Glide.with(holder.imageView.context)
            .load(image.url)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return knowledgeBaseList.size
    }

    inner class KnowledgeBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}