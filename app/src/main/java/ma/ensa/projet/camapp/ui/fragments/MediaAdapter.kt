package ma.ensa.projet.camapp.ui.fragments

import ma.ensa.projet.camapp.ui.player.MediaPlayerActivity
import ma.ensa.projet.camapp.R


import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MediaAdapter(
    private val mediaList: MutableList<Uri>,
    private val deleteCallback: (Uri) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val uri = mediaList[position]
        Glide.with(holder.mediaImageView.context)
            .load(uri)
            .into(holder.mediaImageView)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, MediaPlayerActivity::class.java).apply {
                putExtra(MediaPlayerActivity.EXTRA_MEDIA_URI, uri)
            }
            holder.itemView.context.startActivity(intent)
        }

        // Long press to delete
        holder.itemView.setOnLongClickListener {
            deleteCallback(uri)
            true
        }
    }

    override fun getItemCount(): Int = mediaList.size

    fun removeItem(uri: Uri) {
        val position = mediaList.indexOf(uri)
        if (position >= 0) {
            mediaList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, mediaList.size)
        }
    }

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mediaImageView: ImageView = itemView.findViewById(R.id.mediaImageView)
    }
}