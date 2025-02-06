@file:Suppress("UNUSED_EXPRESSION")

package com.bekirahmetli.instagramapp.profile

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.bekirahmetli.instagramapp.R
import com.bekirahmetli.instagramapp.model.Users
import com.bekirahmetli.instagramapp.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage



@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileEdit(onNavigateBack: () -> Unit,navController: NavController) {
    var userName by remember { mutableStateOf("") }
    var adiSoyadi by remember { mutableStateOf("") }
    var biyografi by remember { mutableStateOf("") }
    var webSite by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val mRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val mUser: FirebaseUser = mAuth.currentUser!!

    LaunchedEffect(Unit) {
        try {
            mRef.child("users").child(mUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    try {
                        val userData = p0.getValue(Users::class.java)
                        userName = userData?.user_name.orEmpty()
                        adiSoyadi = userData?.adi_soyadi.orEmpty()
                        biyografi = userData?.user_detail?.biography.orEmpty()
                        webSite = userData?.user_detail?.web_site.orEmpty()


                        val profilePicUrl = userData?.user_detail?.profile_picture
                        if (!profilePicUrl.isNullOrEmpty()) {
                            selectedImageUri = Uri.parse(profilePicUrl)
                            Log.e("ProfileScreen", "Profil resmi: $profilePicUrl")
                        } else {
                            selectedImageUri = null
                            Log.e("ProfileScreen", "Profil resmi boş veya null")
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileScreen", "Error: ${e.message}")
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.e("ProfileScreen", "DatabaseError: ${p0.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Error: ${e.message}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(50.dp),
                title = { Text(text = "Profili Düzenle", fontStyle = FontStyle.Italic, fontSize = 27.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screens.ProfileScreen.name)
                    }) {
                        Icon(painter = painterResource(id = R.drawable.close), contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {

                        selectedImageUri?.let { uri ->
                            val storageRef = FirebaseStorage.getInstance().reference
                            val userId = mUser.uid
                            val fileName = uri.lastPathSegment ?: "profile_picture.jpg"
                            val imageRef = storageRef.child("users/$userId/$fileName")

                            // Resmi Firebase yükle
                            val uploadTask = imageRef.putFile(uri)
                            uploadTask.continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let { throw it }
                                }
                                imageRef.downloadUrl
                            }.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val downloadUri = task.result.toString()

                                    // Profil bilgilerini güncelle
                                    val updatedUserInfo = mapOf(
                                        "user_name" to userName,
                                        "adi_soyadi" to adiSoyadi,
                                        "user_detail/biography" to biyografi,
                                        "user_detail/web_site" to webSite,
                                        "user_detail/profile_picture" to downloadUri // Yeni profil resmi
                                    )

                                    // Profil güncellenir
                                    mRef.child("users").child(mUser.uid).updateChildren(updatedUserInfo)
                                        .addOnCompleteListener { updateTask ->
                                            if (updateTask.isSuccessful) {
                                                Log.e("ProfileUpdate", "Profil başarıyla güncellendi.")

                                                // Profil güncelleme tamamlandı, şimdi veriyi tekrar oku
                                                mRef.child("users").child(mUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(p0: DataSnapshot) {
                                                        val userData = p0.getValue(Users::class.java)
                                                        val updatedProfilePicUrl = userData?.user_detail?.profile_picture
                                                        Log.e("ProfileScreen", "Profil resmi güncellendi: $updatedProfilePicUrl")

                                                        // Profil resmi güncelledikten sonra tekrar ayarla
                                                        if (!updatedProfilePicUrl.isNullOrEmpty()) {
                                                            selectedImageUri = Uri.parse(updatedProfilePicUrl)
                                                        } else {
                                                            Log.e("ProfileScreen", "Güncellenmiş profil resmi URL'si boş veya null.")
                                                        }
                                                    }

                                                    override fun onCancelled(p0: DatabaseError) {
                                                        Log.e("ProfileScreen", "Veri okuma başarısız: ${p0.message}")
                                                    }
                                                })
                                                navController.navigate(route = Screens.ProfileScreen.name)
                                            } else {
                                                updateTask.exception?.let {
                                                    Log.e("ProfileUpdate", "Profil güncelleme başarısız: ${it.message}")
                                                }
                                            }
                                        }
                                } else {
                                    task.exception?.let {
                                        Log.e("ProfileUpdate", "Resim yükleme başarısız: ${it.message}")
                                    }
                                }
                            }
                        }
                    }) {
                        Icon(painter = painterResource(id = R.drawable.done), contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.topbar),
                    titleContentColor = colorResource(id = R.color.black)
                )
            )
        },
        content = { contentPadding ->
            LazyColumn(
                modifier = Modifier.padding(contentPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    val galleryLauncher =
                        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                            uri?.let {
                                selectedImageUri = it
                            }
                        }


                    val painter = rememberAsyncImagePainter(
                        model = selectedImageUri ?: R.drawable.person,
                        onError = { Log.e("ProfileScreen", "Görüntü yükleme hatası: ${it.result.throwable.message}") }
                    )

                    ProfileImage(
                        painter = painter,
                        imageSize = 130,
                        borderColor = Color.Gray,
                        borderWidth = 4,
                        onClick = { galleryLauncher.launch("image/*") }
                    )

                    Spacer(modifier = Modifier.size(5.dp))
                    Text(text = "Fotoğrafı Değiştir", color = Color.Blue)
                }
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(all = 5.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = "Ad", fontSize = 20.sp)
                        Spacer(modifier = Modifier.size(5.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = adiSoyadi,
                            onValueChange = { adiSoyadi = it },
                            label = { Text(text = "Ad") }
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                }
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(all = 5.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = "Kullanıcı Adı", fontSize = 20.sp)
                        Spacer(modifier = Modifier.size(5.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text(text = "Kullanıcı Adı") }
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                }
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(all = 5.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = "Biyografi", fontSize = 20.sp)
                        Spacer(modifier = Modifier.size(5.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = biyografi,
                            onValueChange = { biyografi = it },
                            label = { Text(text = "Biyografi") }
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                }
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(all = 5.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = "İnternet Sitesi", fontSize = 20.sp)
                        Spacer(modifier = Modifier.size(5.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = webSite,
                            onValueChange = { webSite = it },
                            label = { Text(text = "İnternet Sitesi") }
                        )
                    }
                }
            }
        }
    )
}