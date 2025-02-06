package com.bekirahmetli.instagramapp.share

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bekirahmetli.instagramapp.R
import java.io.File

@Composable
fun ShareVideo(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )
        }
    }
    var recording by remember { mutableStateOf<Recording?>(null) }

    // İzin kontrolü ve izin isteme işlemi
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.CAMERA] == true -> {
                // Kamera izni verildi
            }
            permissions[Manifest.permission.RECORD_AUDIO] == true -> {
                // Ses kaydı izni verildi
            }
            else -> {
                Toast.makeText(context, "Kamera ve/veya Ses izni gereklidir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissions.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissionLauncher.launch(permissions)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        CameraPreview(
            controller = controller,
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = {
                controller.cameraSelector =
                    if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else CameraSelector.DEFAULT_BACK_CAMERA
            },
            modifier = Modifier.offset(16.dp, 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cameraswitch),
                contentDescription = null
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(
                onClick = {
                    if (recording != null) {
                        // Kayıt işlemini durdur
                        recording?.stop()
                        recording = null
                        Toast.makeText(
                            context,
                            "Video kaydı durduruldu",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Kayıt işlemini başlat
                        recordVideo(context, controller) { newRecording ->
                            recording = newRecording
                        }
                    }
                }
            ) {
                Icon(painter = painterResource(id = R.drawable.videocam), contentDescription = null, modifier = Modifier.size(45.dp))
            }
        }
    }
}


@SuppressLint("MissingPermission")
private fun recordVideo(
    context: Context,
    controller: LifecycleCameraController,
    onRecordingChange: (Recording?) -> Unit
) {
    if (!hasRequiredPermissions(context)) {
        Toast.makeText(context, "Kamera izni yok", Toast.LENGTH_SHORT).show()
        return
    }

    val outputFile = File(context.filesDir, "my-recording.mp4")
    val newRecording = controller.startRecording(
        FileOutputOptions.Builder(outputFile).build(),
        AudioConfig.create(true),
        ContextCompat.getMainExecutor(context)
    ) { event ->
        when (event) {
            is VideoRecordEvent.Finalize -> {
                if (event.hasError()) {
                    Log.e("Camera", "Error: ${event.error}")
                    Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
                } else {
                    saveVideoToGallery(context, outputFile) // Videoyu galeride sakla
                    Toast.makeText(context, "Video çekimi başarılı", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    onRecordingChange(newRecording)
}

private fun saveVideoToGallery(context: Context, file: File) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/Camera") // Kamera klasörüne kaydet
    }

    val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        context.contentResolver.openOutputStream(it).use { outputStream ->
            if (outputStream != null) {
                file.inputStream().copyTo(outputStream)
            }
        }
        Toast.makeText(context, "Video Galeriye Kaydedildi", Toast.LENGTH_SHORT).show()
    } ?: Toast.makeText(context, "Video Kaydedilemedi", Toast.LENGTH_SHORT).show()
}

private fun hasRequiredPermissions(context: Context): Boolean {
    val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    return requiredPermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}