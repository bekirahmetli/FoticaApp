package com.bekirahmetli.instagramapp.share

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bekirahmetli.instagramapp.R
import com.bekirahmetli.instagramapp.model.Posts
import com.bekirahmetli.instagramapp.navigation.Screens
import com.bekirahmetli.instagramapp.utils.LoadingAnimation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun ShareNext(mediaPath: String, navController: NavController) {
    val photoUri: Uri = Uri.fromFile(File(mediaPath))
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val mUser: FirebaseUser = mAuth.currentUser!!
    val mRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    val mStorageReference: StorageReference = FirebaseStorage.getInstance().reference

    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(50.dp),
                title = {
                    Text(text = "Şu Kişilerle Paylaş:", fontSize = 23.sp, textAlign = TextAlign.Left)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screens.ShareScreen.name)
                    }) {
                        Icon(painter = painterResource(id = R.drawable.geri), contentDescription = null)
                    }
                },
                actions = {
                    Text(
                        text = "Paylaş", fontSize = 27.sp, color = if (isLoading) Color.Gray else Color.Blue,
                        modifier = Modifier
                            .clickable(enabled = !isLoading) {
                                isLoading = true

                                val uniqueFileName = System.currentTimeMillis().toString()
                                val uploadTask = mStorageReference.child("users")
                                    .child(mUser.uid).child(uniqueFileName).putFile(photoUri)

                                uploadTask.addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        val downloadUrl = task.result.storage.downloadUrl
                                        downloadUrl.addOnSuccessListener { uri ->
                                            val yuklenenFotoURL = uri.toString()
                                            val postID = mRef.child("posts").child(mUser.uid).push().key

                                            val yuklenenPost = Posts(
                                                mUser.uid,
                                                postID,
                                                0,
                                                description,
                                                yuklenenFotoURL
                                            )

                                            mRef.child("posts").child(mUser.uid).child(postID!!).setValue(yuklenenPost)
                                            mRef.child("posts").child(mUser.uid).child(postID).child("yuklenme_tarih")
                                                .setValue(ServerValue.TIMESTAMP)

                                            mRef.child("users").child(mUser.uid).child("user_detail").addListenerForSingleValueEvent(object : ValueEventListener{
                                                override fun onDataChange(p0: DataSnapshot) {
                                                    var postNumber = p0.child("post").getValue().toString().toInt()
                                                    postNumber++
                                                    mRef.child("users").child(mUser.uid).child("user_detail").child("post").setValue(postNumber.toString())
                                                }

                                                override fun onCancelled(error: DatabaseError) {

                                                }

                                            })

                                            // Yükleme ve veri güncelleme işlemi tamamlandığında yönlendir
                                            navController.navigate(route = Screens.HomeScreen.name) {
                                                popUpTo("ShareNext") { inclusive = true }
                                            }
                                        }
                                    } else {
                                    }
                                }.addOnFailureListener { exception ->
                                    isLoading = false
                                }
                            }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (mediaPath.endsWith(".mp4") || mediaPath.endsWith(".avi")) {
                VideoPlayer(videoPath = mediaPath)
            } else {
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                val imageBitmap = BitmapFactory.decodeFile(mediaPath, options)?.asImageBitmap()
                imageBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { if (!isLoading) description = it },
                label = { Text("Açıklama ekle") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3,
                enabled = !isLoading
            )

            if (isLoading) {
                LoadingAnimation(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp)
                )
            }
        }
    }
}