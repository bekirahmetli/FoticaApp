package com.bekirahmetli.instagramapp.share

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.widget.VideoView
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
fun ShareGallery(selectedFolderIndex: Int,  onMediaSelected: (String) -> Unit ) {
    val root = Environment.getExternalStorageDirectory().path
    val kameraResimleri = "$root/DCIM/Camera"
    val indirilenResimler = "$root/Download"
    val whatsappResimleri = "$root/DCIM/Screenshots"

    val klasorPaths = listOf(indirilenResimler,kameraResimleri, whatsappResimleri)

    Log.e("ShareGallery", "Seçili klasör: $selectedFolderIndex")

    val klasordekiDosyalar = remember {
        mutableStateOf(getSortedMediaFiles(klasorPaths[selectedFolderIndex]))
    }

    LaunchedEffect(selectedFolderIndex) {
        Log.e("ShareGallery", "Klasör için dosyaları getir: ${klasorPaths[selectedFolderIndex]}")
        klasordekiDosyalar.value = getSortedMediaFiles(klasorPaths[selectedFolderIndex])
        Log.e("ShareGallery", "Bulunan dosya sayısı: ${klasordekiDosyalar.value.size}")
    }

    var selectedMedia by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        selectedMedia?.let { mediaPath ->
            if (mediaPath.endsWith(".mp4") || mediaPath.endsWith(".avi")) { // Video dosyası
                VideoPlayer(videoPath = mediaPath)
            } else { // Resim dosyası
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                val imageBitmap = BitmapFactory.decodeFile(mediaPath, options)?.asImageBitmap()
                imageBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    )
                } ?: run {
                    Log.e("ShareGallery", "Error: $mediaPath")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(klasordekiDosyalar.value) { dosyaYolu ->
                val isVideo = dosyaYolu.endsWith(".mp4") || dosyaYolu.endsWith(".avi")
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable {
                            Log.d("ShareGallery", "Medyaya tıklandı: $dosyaYolu")
                            selectedMedia = dosyaYolu
                            onMediaSelected(dosyaYolu) // Seçilen medya yolunu callback ile gönder
                        }
                ) {
                    if (isVideo) {
                        // Video önizleme
                        val videoThumbnail = ThumbnailUtils.createVideoThumbnail(dosyaYolu, MediaStore.Images.Thumbnails.MINI_KIND)
                        videoThumbnail?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        val resimBitmap by remember(dosyaYolu) {
                            mutableStateOf(decodeSampledBitmapFromFile(dosyaYolu, 100, 100)?.asImageBitmap())
                        }
                        resimBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop // Görselleri hücrelere tam oturtur
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getSortedMediaFiles(folderPath: String): List<String> {
    val imageExtensions = arrayOf("jpg", "jpeg", "png", "gif", "bmp")
    val videoExtensions = arrayOf("mp4", "avi", "mov", "mkv")

    val folder = File(folderPath)
    val allFiles = folder.listFiles()?.toList() ?: emptyList()

    // Resimleri ve videoları ayır
    val imageFiles = allFiles.filter { file ->
        imageExtensions.any { ext -> file.extension.equals(ext, ignoreCase = true) }
    }.sortedByDescending { it.lastModified() }  // Tarihe göre sıralandı (En yeni üstte)

    val videoFiles = allFiles.filter { file ->
        videoExtensions.any { ext -> file.extension.equals(ext, ignoreCase = true) }
    }.sortedByDescending { it.lastModified() }


    return imageFiles.map { it.absolutePath } + videoFiles.map { it.absolutePath }
}

@Composable
fun VideoPlayer(videoPath: String) {
    val context = LocalContext.current
    val videoView = remember { VideoView(context) }


    LaunchedEffect(videoPath) {
        videoView.setVideoPath(videoPath)
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false
            mediaPlayer.start()
        }
        videoView.setOnCompletionListener {
        }
    }

    AndroidView(
        factory = { videoView },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}


fun getVideoFiles(folderPath: String): List<String> {
    val videoExtensions = arrayOf("mp4", "avi", "mov", "mkv")
    val folder = File(folderPath)
    return folder.listFiles()?.filter { file ->
        videoExtensions.any { ext -> file.extension.equals(ext, ignoreCase = true) }
    }?.sortedByDescending { it.lastModified() }?.map { it.absolutePath } ?: emptyList()
}

fun decodeSampledBitmapFromFile(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(filePath, options)

    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false
    options.inPreferredConfig = Bitmap.Config.RGB_565

    return BitmapFactory.decodeFile(filePath, options)
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}