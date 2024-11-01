package ma.ensa.projet.camapp.ui.player

import ma.ensa.projet.camapp.R

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var imageView: ImageView
    private var mediaUri: Uri? = null
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        playerView = findViewById(R.id.playerView)
        imageView = findViewById(R.id.imageView)

        // Get the URI from the intent
        mediaUri = intent.getParcelableExtra(EXTRA_MEDIA_URI)

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        mediaUri?.let { uri ->
            // Check whether the media is a video or image based on content resolver.
            val mimeType = contentResolver.getType(uri)
            if (mimeType != null) {
                when {
                    mimeType.startsWith("video/") -> {
                        // Play video
                        val mediaItem = MediaItem.Builder()
                            .setUri(uri)
                            .build()
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.play()
                        imageView.visibility = ImageView.GONE
                        playerView.visibility = PlayerView.VISIBLE
                    }
                    mimeType.startsWith("image/") -> {
                        // Load image
                        Glide.with(this)
                            .load(uri)
                            .into(imageView)
                        imageView.visibility = ImageView.VISIBLE
                        playerView.visibility = PlayerView.GONE
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Release player when the activity is stopped
        player.release()
    }

    companion object {
        const val EXTRA_MEDIA_URI = "extra_media_uri"
    }
}