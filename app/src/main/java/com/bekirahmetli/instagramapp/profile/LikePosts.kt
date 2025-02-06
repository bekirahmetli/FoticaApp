package com.bekirahmetli.instagramapp.profile

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bekirahmetli.instagramapp.model.Posts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun LikePosts() {
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    if (currentUserID == null) {
        Text("Lütfen oturum açın.")
        return
    }

    val mRef = FirebaseDatabase.getInstance().reference

    val likedPostsUrls = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(currentUserID) {
        mRef.child("likes").orderByChild(currentUserID).equalTo(currentUserID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue() != null) {
                        for (likesPost in snapshot.children) {
                            var currentUserLikesPostID = likesPost.key

                            mRef.child("posts").orderByChild(currentUserLikesPostID!!).limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.getValue() != null) {
                                            for (post in snapshot.children) {
                                                var receivePost =
                                                    post.child(currentUserLikesPostID)
                                                        .getValue(Posts::class.java)

                                                receivePost!!.photo_url

                                                Log.e("LikePost", receivePost.photo_url!!)
                                                likedPostsUrls.add(receivePost.photo_url!!)
                                            }
                                        }
                                        isLoading = false
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        isLoading = false
                                    }
                                })
                        }
                    }else{
                        isLoading = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })
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
    } else if (likedPostsUrls.isEmpty()) {
        Text("Beğenilen gönderi bulunamadı.", modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(likedPostsUrls) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}
