package ma.ensa.projet.camapp.data

import android.net.Uri

data class MediaItem(
    val uri: Uri,
    val isVideo: Boolean,
    val isHeader: Boolean = false,
    val headerTitle: String = ""
)