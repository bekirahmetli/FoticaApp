package com.bekirahmetli.instagramapp.utils


import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.bekirahmetli.instagramapp.R
import com.bekirahmetli.instagramapp.model.Comments
import com.bekirahmetli.instagramapp.model.UserPosts
import com.bekirahmetli.instagramapp.model.Users
import com.bekirahmetli.instagramapp.profile.ProfileImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun CommentSheet(post: UserPosts) {
    var commentText by remember { mutableStateOf("") }
    val comments = remember { mutableStateListOf<Comments>() }
    var mentionList by remember { mutableStateOf(emptyList<Users>()) }
    var isMentioning by remember { mutableStateOf(false) }


    Log.e("CommentSheet", "CommentSheet açıldı. Post ID: ${post.postID}")

    LaunchedEffect(post) {
        try {
            if (post.postID != null) {
                yorumlariGetir(post.postID!!) { yorumlar ->
                    comments.clear()
                    comments.addAll(yorumlar)
                }
            } else {
                Log.e("CommentSheet", "Post ID null, yorumlar alınamıyor.")
            }
        } catch (e: Exception) {
            Log.e("CommentSheetError", "Yorumları getirirken hata: ${e.message}")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Text(text = "Yorumlar", style = MaterialTheme.typography.labelMedium)

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(comments) { yorum ->

                    var kullanici by remember { mutableStateOf<Users?>(null) }
                    LaunchedEffect(yorum.user_id) {
                        yorum.user_id?.let { userId ->
                            kullaniciBilgisiGetir(userId) { user ->
                                kullanici = user
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .padding(vertical = 3.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            // Kullanıcı adı ve yorum kısmı
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                ProfileImage(
                                    painter = rememberImagePainter(data = kullanici?.user_detail?.profile_picture),
                                    imageSize = 36,
                                    onClick = { }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = kullanici?.user_name ?: "Bilinmeyen",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = buildAnnotatedString {
                                            yorum.yorum?.split(" ")?.forEach { kelime ->
                                                when {
                                                    kelime.startsWith("@") -> {
                                                        withStyle(style = SpanStyle(colorResource(R.color.hashtag))) {
                                                            append("$kelime ")
                                                        }
                                                    }
                                                    kelime.startsWith("#") -> {
                                                        withStyle(style = SpanStyle(colorResource(R.color.hashtag))) {
                                                            append("$kelime ")
                                                        }
                                                    }
                                                    else -> {
                                                        append("$kelime ")
                                                    }
                                                }
                                            }
                                        } ?: AnnotatedString(""),
                                        style = TextStyle(fontSize = 14.sp),
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Tarih ve beğeni kısmı
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = yorum.yorum_tarih?.let {
                                        val timestamp = it.toLongOrNull() ?: 0L  // `yorum_tarih`i Long'a dönüştür
                                        PostTime().getTimeAgo(timestamp)  // Tarihi istediğin formatta yazdır
                                    } ?: "Bilinmiyor",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        var aktifKullanici by remember { mutableStateOf<Users?>(null) }
        val mAuth = FirebaseAuth.getInstance()
        val mUser = mAuth.currentUser
        LaunchedEffect(Unit) {
            // Burada giriş yapan kullanıcının ID'sini kullanarak bilgileri çekiyoruz
            kullaniciBilgisiGetir(mUser!!.uid) { user ->
                aktifKullanici = user
            }
        }


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(8.dp)
        ) {
            ProfileImage(
                painter = rememberImagePainter(data = aktifKullanici?.user_detail?.profile_picture),
                imageSize = 36,
                onClick = {  }
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = commentText,
                //onValueChange = { commentText = it },
                onValueChange = { text ->
                    commentText = text
                    if (text.endsWith("@")) {
                        // Kullanıcıları getir ve mention listesini güncelle
                        kullanicilarinListesiniGetir { users ->
                            mentionList = users
                            isMentioning = true
                        }
                    } else if (!text.contains("@")) {
                        isMentioning = false
                        mentionList = emptyList()
                    }
                },
                label = { Text("Yorum ekle...") },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(horizontal = 8.dp),
                singleLine = true,  // Tek satır olacak
                maxLines = 1,       // Maksimum satır sayısı 1
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
            )
            if (isMentioning && mentionList.isNotEmpty()) {
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(mentionList) { kullanici ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Kullanıcı adı yorum kutusuna ekle
                                        commentText += kullanici.user_name ?: ""
                                        isMentioning = false
                                        mentionList = emptyList()
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Profil resmi ve kullanıcı adı
                                ProfileImage(
                                    painter = rememberImagePainter(data = kullanici.user_detail?.profile_picture),
                                    imageSize = 36,
                                    onClick = {  }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = kullanici.user_name ?: "Bilinmeyen",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Blue
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    Log.e("CommentSheet", "Gönder butonuna tıklandı. Yorum: $commentText")
                    try {
                        yorumEkle(post.postID!!, commentText) {
                            yorumlariGetir(post.postID!!) { yeniYorumlar ->
                                comments.clear()
                                comments.addAll(yeniYorumlar)
                            }
                            commentText = ""
                        }
                    } catch (e: Exception) {
                        Log.e("CommentSendError", "Yorum gönderirken hata: ${e.message}")
                    }
                }
            ) {
                Text(text = "Paylaş")
            }
        }
    }
}

fun yorumlariGetir(postID: String, onComplete: (List<Comments>) -> Unit) {
    val mRef = FirebaseDatabase.getInstance().reference
    val yorumlar = ArrayList<Comments>()

    Log.e("CommentSheet", "PostID: $postID için yorumlar alınıyor.")
    mRef.child("comments").child(postID).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Log.e("CommentSheet", "Yorumlar alma işlemi iptal edildi: ${p0.message}")
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.hasChildren()) {
                for (ds in p0.children) {
                    val yorum = ds.getValue(Comments::class.java)
                    yorum?.let {
                        yorumlar.add(it)
                        Log.d("CommentSheet", "Yorum alındı: ${it.yorum}")
                    }
                }
            } else {
                Log.e("CommentSheet", "Yorum bulunamadı. Post ID: $postID")
            }
            Log.e("CommentSheet", "Toplam alınan yorum sayısı: ${yorumlar.size}")
            onComplete(yorumlar)
        }
    })
}

fun yorumEkle(postID: String, yorum: String, onComplete: () -> Unit) {
    val mRef = FirebaseDatabase.getInstance().reference
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Eski yorumları silmeden yeni yorumları eklemek için bir push ID oluşturuyoruz
    val yeniYorumID = mRef.child("comments").child(postID).push().key

    // Yorum nesnesini oluştur
    val yeniYorum = Comments(
        user_id = userId,
        yorum = yorum,
        yorum_begeni = "0",
        yorum_tarih = System.currentTimeMillis().toString()
    )

    // Yorum ekleme işlemi
    if (yeniYorumID != null) {
        // Aynı postID'nin altına, yeni bir yorum ekliyoruz
        mRef.child("comments").child(postID).child(yeniYorumID).setValue(yeniYorum).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.e("CommentSheet", "Yorum başarıyla eklendi.")
                onComplete()
            } else {
                Log.e("CommentSheet", "Yorum ekleme hatası: ${task.exception?.message}")
            }
        }
    }
}

fun kullaniciBilgisiGetir(userId: String, onComplete: (Users?) -> Unit) {
    val mRef = FirebaseDatabase.getInstance().reference
    mRef.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            Log.e("KullaniciBilgi", "Hata: ${error.message}")
            onComplete(null)
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            val kullanici = snapshot.getValue(Users::class.java)
            onComplete(kullanici)
        }
    })
}

fun kullanicilarinListesiniGetir(onComplete: (List<Users>) -> Unit) {
    val mRef = FirebaseDatabase.getInstance().reference
    val kullaniciListesi = ArrayList<Users>()

    mRef.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            Log.e("KullaniciGetir", "Hata: ${error.message}")
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                for (ds in snapshot.children) {
                    val kullanici = ds.getValue(Users::class.java)
                    kullanici?.let { kullaniciListesi.add(it) }
                }
            }
            onComplete(kullaniciListesi)
        }
    })
}