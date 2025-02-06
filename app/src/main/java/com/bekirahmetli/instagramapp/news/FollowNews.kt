package com.bekirahmetli.instagramapp.news


import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.database.FirebaseDatabase

@Composable
fun FollowNews() {
    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance().reference
    val postsList = remember { mutableStateListOf<List<Any>>() }
    // List: [userName, profilePictureUrl, photoUrl, likeCount]
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        Log.e("FollowNews", "Beğeni verilerini getirme...")
        db.child("likes").get().addOnSuccessListener { likesSnapshot ->
            Log.e("FollowNews", "Beğenilen veriler başarıyla alındı")
            val postLikesMap = mutableMapOf<String, Int>()
            for (postSnapshot in likesSnapshot.children) {
                val postId = postSnapshot.key ?: continue
                val likeCount = postSnapshot.childrenCount.toInt()
                Log.e("FollowNews", "Post ID: $postId, Like Sayısı: $likeCount")
                if (likeCount >= 2) {
                    postLikesMap[postId] = likeCount
                }
            }

            val sortedPosts = postLikesMap.toList().sortedByDescending { it.second }.map { it.first }


            db.child("posts").get().addOnSuccessListener { postsSnapshot ->

                for (postSnapshot in postsSnapshot.children) {
                    for (postId in sortedPosts) {
                        val post = postSnapshot.child(postId)
                        val userId = post.child("user_id").value as? String ?: continue
                        val photoUrl = post.child("photo_url").value as? String ?: continue
                        val likeCount = postLikesMap[postId] ?: 0
                        Log.e("FollowNews", "Post ID: $postId, User ID: $userId, Photo URL: $photoUrl")

                        db.child("users").child(userId).get().addOnSuccessListener { userSnapshot ->
                            val hiddenProfile = userSnapshot.child("hidden_profile").value as? Boolean ?: true
                            val userName = userSnapshot.child("user_name").value as? String ?: ""
                            val profilePicture = userSnapshot.child("user_detail").child("profile_picture").value as? String ?: ""

                            Log.e("FollowNews", "User ID: $userId, UserName: $userName, HiddenProfile: $hiddenProfile, Profile Picture: $profilePicture")

                            if (!hiddenProfile) {
                                postsList.add(listOf(userName, profilePicture, photoUrl, likeCount))

                            }

                            // Veri başarıyla çekildiyse yükleme durumunu kapat
                            if (postsList.size == sortedPosts.size) {
                                isLoading = false
                            }

                        }.addOnFailureListener { e ->
                            Log.e("FollowNews", "Error: ${e.message}")
                        }
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("FollowNews", "Error: ${e.message}")
                isLoading = false
            }
        }.addOnFailureListener { e ->
            Log.e("FollowNews", "Error: ${e.message}")
            isLoading = false
        }
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
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(postsList) { post ->
                val userName = post[0] as String
                val profilePictureUrl = post[1] as String
                val photoUrl = post[2] as String
                val likeCount = post[3] as Int

                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = profilePictureUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = userName, style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$likeCount Beğenme", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
