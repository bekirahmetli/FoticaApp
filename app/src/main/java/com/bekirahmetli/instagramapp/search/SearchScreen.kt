package com.bekirahmetli.instagramapp.search

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.bekirahmetli.instagramapp.model.Posts
import com.bekirahmetli.instagramapp.model.Users
import com.google.firebase.database.FirebaseDatabase

@Composable
fun SearchScreen(navController: NavController) {
    val users = remember { mutableStateOf<List<Users>>(emptyList()) }
    val posts = remember { mutableStateOf<List<Posts>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        getUsersAndPosts(users, posts)
        isLoading = false
    }

    if (isLoading) {

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Arama kutusu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                    .clickable {
                        navController.navigate("SearchUsers")
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = Color.Gray,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Ara", color = Color.Gray)
                }
            }

            // Gönderileri Grid şeklinde gösteriyoruz
            if (posts.value.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts.value) { post ->
                        PostCard(post)
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(post: Posts) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        AsyncImage(
            model = post.photo_url ?: "",
            contentDescription = post.aciklama,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
    }
}

fun getUsersAndPosts(usersState: MutableState<List<Users>>, postsState: MutableState<List<Posts>>) {
    val db = FirebaseDatabase.getInstance().reference
    var usersLoaded = false
    var postsLoaded = false

    fun checkLoadingComplete() {
        if (usersLoaded && postsLoaded) {
            Log.e("Firebase", "Tüm veriler yüklendi.")
        }
    }

    // Kullanıcıları çek
    db.child("users").get().addOnSuccessListener { dataSnapshot ->
        Log.e("Firebase", "Users veri çekildi.")

        val userList = dataSnapshot.children.mapNotNull { snapshot ->
            val userId = snapshot.key
            val adiSoyadi = snapshot.child("adi_soyadi").getValue(String::class.java)
            val email = snapshot.child("email").getValue(String::class.java)
            val password = snapshot.child("password").getValue(String::class.java)
            val userName = snapshot.child("user_detail").child("user_name").getValue(String::class.java)
            val hiddenProfile = snapshot.child("hidden_profile").getValue(Boolean::class.java) ?: true

            if (!hiddenProfile && userId != null) {
                Users(email, password, userName, adiSoyadi, userId, null)
            } else {
                null
            }
        }

        Log.d("Firebase", "Toplam kullanıcı sayısı: ${userList.size}")
        usersState.value = userList
        usersLoaded = true
        checkLoadingComplete()

    }.addOnFailureListener { exception ->
        Log.e("Firebase", "Users veri çekilirken hata oluştu: ${exception.message}")
        usersLoaded = true
        checkLoadingComplete()
    }

    // Postları çek
    db.child("posts").get().addOnSuccessListener { postSnapshot ->
        Log.d("Firebase", "Posts veri çekildi.")

        val postList = postSnapshot.children.flatMap { userPosts ->
            userPosts.children.mapNotNull { snapshot ->
                val userId = snapshot.child("user_id").getValue(String::class.java)
                val postId = snapshot.child("post_id").getValue(String::class.java)
                val yuklenmeTarih = snapshot.child("yuklenme_tarih").getValue(Long::class.java)
                val aciklama = snapshot.child("aciklama").getValue(String::class.java)
                val photoUrl = snapshot.child("photo_url").getValue(String::class.java)

                if (userId != null && postId != null) {
                    Posts(userId, postId, yuklenmeTarih, aciklama, photoUrl)
                } else {
                    null
                }
            }
        }

        Log.d("Firebase", "Toplam post sayısı: ${postList.size}")
        postsState.value = postList
        postsLoaded = true
        checkLoadingComplete()

    }.addOnFailureListener { exception ->
        Log.e("Firebase", "Posts veri çekilirken hata oluştu: ${exception.message}")
        postsLoaded = true
        checkLoadingComplete()
    }
}