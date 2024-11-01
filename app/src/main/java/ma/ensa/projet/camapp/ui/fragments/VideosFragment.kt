package ma.ensa.projet.camapp.ui.fragments

import ma.ensa.projet.camapp.ui.activities.GalleryActivity
import ma.ensa.projet.camapp.ui.fragments.MediaAdapter
import ma.ensa.projet.camapp.R

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VideosFragment : Fragment() {
    private lateinit var mediaRecyclerView: RecyclerView
    private lateinit var mediaAdapter: MediaAdapter
    private val mediaList = mutableListOf<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaRecyclerView = view.findViewById(R.id.mediaRecyclerView)
        mediaRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        mediaAdapter = MediaAdapter(mediaList) { uri ->
            (activity as? GalleryActivity)?.showDeleteConfirmationDialog(uri)
        }
        mediaRecyclerView.adapter = mediaAdapter

        loadVideos()
    }

    private fun loadVideos() {
        val videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val videoProjection = arrayOf(MediaStore.Video.Media._ID)

        requireActivity().contentResolver.query(
            videoCollection,
            videoProjection,
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(videoCollection, id.toString())
                mediaList.add(contentUri)
            }
        }
        mediaAdapter.notifyDataSetChanged()
    }

    fun refreshMedia() {
        mediaList.clear()
        loadVideos()
    }
}