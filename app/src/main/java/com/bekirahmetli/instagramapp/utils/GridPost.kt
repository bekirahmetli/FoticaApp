package com.bekirahmetli.instagramapp.utils


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.bekirahmetli.instagramapp.model.UserPosts
import com.google.firebase.auth.FirebaseAuth


@Composable
fun GridPost() {
    val tumGonderiler = remember { mutableStateListOf<UserPosts>() }
    val mAuth = FirebaseAuth.getInstance()
    val mUser = mAuth.currentUser

    // Kullanıcı postlarını çekme işlemi
    mUser?.uid?.let { kullaniciID ->
        PostGetir(kullaniciID) { gonderiler ->
            tumGonderiler.clear()
            tumGonderiler.addAll(gonderiler) // Yeni verileri ekle
        }
    }

    // LazyVerticalGrid ile gönderi fotoğraflarını grid görünümünde göster
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(tumGonderiler.size) { index ->
            val gonderi = tumGonderiler[index]
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Image(
                    painter = rememberImagePainter(data = gonderi.postURL),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop // Görselleri hücrelere tam oturtur
                )
            }
        }
    }
}