package ma.ensa.projet.camapp.ui.fragments

import ma.ensa.projet.camapp.ui.activities.GalleryActivity
import ma.ensa.projet.camapp.ui.fragments.MediaAdapter


import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.projet.camapp.R

class ImagesFragment : Fragment() {
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

        loadImages()
    }

    private fun loadImages() {
        val imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imageProjection = arrayOf(MediaStore.Images.Media._ID)

        requireActivity().contentResolver.query(
            imageCollection,
            imageProjection,
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(imageCollection, id.toString())
                mediaList.add(contentUri)
            }
        }
        mediaAdapter.notifyDataSetChanged()
    }

    fun refreshMedia() {
        mediaList.clear()
        loadImages()
    }
}
