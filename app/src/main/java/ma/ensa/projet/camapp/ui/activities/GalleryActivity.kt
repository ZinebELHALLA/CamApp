package ma.ensa.projet.camapp.ui.activities

import ma.ensa.projet.camapp.ui.fragments.ImagesFragment
import ma.ensa.projet.camapp.ui.fragments.VideosFragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ma.ensa.projet.camapp.R

class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val imagesButton = findViewById<ImageButton>(R.id.imagesButton)
        val videosButton = findViewById<ImageButton>(R.id.videosButton)

        // Set initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ImagesFragment())
                .commit()
        }

        imagesButton.setOnClickListener {
            switchFragment(ImagesFragment())
        }

        videosButton.setOnClickListener {
            switchFragment(VideosFragment())
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun showDeleteConfirmationDialog(uri: Uri) {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete this media?")
            .setPositiveButton("Delete") { _, _ -> deleteMedia(uri) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMedia(uri: Uri) {
        val deletedRows = contentResolver.delete(uri, null, null)
        if (deletedRows > 0) {
            Toast.makeText(this, "Media deleted successfully", Toast.LENGTH_SHORT).show()
            // Refresh current fragment
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)?.let {
                when (it) {
                    is ImagesFragment -> it.refreshMedia()
                    is VideosFragment -> it.refreshMedia()
                    else -> {}
                }
            }
        } else {
            Toast.makeText(this, "Error deleting media", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, GalleryActivity::class.java)
        }
    }
}
