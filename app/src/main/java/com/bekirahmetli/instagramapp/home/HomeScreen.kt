package com.bekirahmetli.instagramapp.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.bekirahmetli.instagramapp.R
import com.bekirahmetli.instagramapp.model.Posts
import com.bekirahmetli.instagramapp.model.UserPosts
import com.bekirahmetli.instagramapp.model.Users
import com.bekirahmetli.instagramapp.profile.ProfileImage
import com.bekirahmetli.instagramapp.utils.CommentSheet
import com.bekirahmetli.instagramapp.utils.Notifications
import com.bekirahmetli.instagramapp.utils.PostTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val tumGonderiler = remember { mutableStateListOf<UserPosts>() }
    val mAuth = FirebaseAuth.getInstance()
    val mUser = mAuth.currentUser
    val coroutineScope = rememberCoroutineScope()
    // BottomSheet için state
    val sheetState = rememberBottomSheetScaffoldState()
    var selectedPost by remember { mutableStateOf<UserPosts?>(null) }
    var showCommentSheet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Bildirim izni isteme
    LaunchedEffect(Unit) {
        askNotificationPermission(context)
    }

    // Kullanıcı postlarını çekme işlemi
    mUser?.uid?.let { kullaniciID ->
        isLoading = true
        kullaniciPostlariniGetir(kullaniciID) { gonderiler ->
            tumGonderiler.clear()
            tumGonderiler.addAll(gonderiler) // Yeni verileri ekle
            isLoading = false
        }

        // Takip edilen kullanıcıların postlarını ekleme
        val mRef = FirebaseDatabase.getInstance().reference
        mRef.child("following").child(kullaniciID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val takipEdilenID = ds.key ?: continue
                    kullaniciPostlariniGetir(takipEdilenID) { takipEdilenGonderiler ->
                        // Gönderilerin tekrar eklenmemesi için mevcut postları kontrol et
                        val yeniGonderiler = takipEdilenGonderiler.filter { yeniGonderi ->
                            tumGonderiler.none { mevcutGonderi -> mevcutGonderi.postID == yeniGonderi.postID }
                        }
                        tumGonderiler.addAll(yeniGonderiler)
                        tumGonderiler.sortByDescending { it.postYuklenmeTarihi }
                    }
                }
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                Log.e("Hata", "Takip edilen kullanıcıların postları alınamadı: ${error.message}")
            }
        })
    }

    // Scaffold ile BottomSheet birleşimi
    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetContent = {
            if (showCommentSheet) {
                selectedPost?.let { post ->
                    CommentSheet(post = post)
                }
            }
        },
        sheetPeekHeight = 0.dp,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.height(40.dp),
                title = {
                    Text(
                        text = "Fotica",
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Cursive,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("HomeCamera")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.fotograf),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("HomeMessages")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.gonder),
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.topbar),
                    titleContentColor = colorResource(id = R.color.black)
                )
            )
        },
        content = { paddingValues ->
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
                LazyColumn(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    items(tumGonderiler) { gonderi ->

                        var yorumSayisi by remember { mutableStateOf(0) }

                        // Yorum sayısını Firebase'den çekiyoruz
                        FirebaseDatabase.getInstance().reference.child("comments")
                            .child(gonderi.postID!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    yorumSayisi =
                                        snapshot.childrenCount.toInt() // Yorum sayısını güncelle
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Hata", "Yorum sayısı alınamadı: ${error.message}")
                                }
                            })

                        var begenmeSayisi by remember { mutableStateOf(0) }


                        var isLiked by remember { mutableStateOf(false) }

                        // Beğenme durumunu Firebase'den kontrol et
                        FirebaseAuth.getInstance().currentUser?.let { user ->
                            checkIsLiked(gonderi.postID!!, user.uid) { liked ->
                                isLiked =
                                    liked
                            }
                        }

                        // Beğeni sayısını her seferinde güncelleme
                        FirebaseDatabase.getInstance().reference.child("likes")
                            .child(gonderi.postID!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    begenmeSayisi = snapshot.childrenCount.toInt()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Hata", "Beğeni sayısı alınamadı: ${error.message}")
                                }
                            })

                        Card(
                            modifier = Modifier
                                .padding(all = 5.dp)
                                .fillMaxWidth()
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    ProfileImage(
                                        painter = rememberImagePainter(data = gonderi.userPhotoURL),
                                        imageSize = 36,
                                        onClick = {  }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    gonderi.userName?.let { Text(text = it, fontSize = 20.sp) }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        painter = painterResource(id = R.drawable.ucnokta),
                                        contentDescription = ""
                                    )
                                }

                                Image(
                                    painter = rememberImagePainter(data = gonderi.postURL),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(270.dp),
                                    contentScale = ContentScale.Crop
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    IconButton(onClick = {

                                        var mRef = FirebaseDatabase.getInstance().reference
                                        var userID = FirebaseAuth.getInstance().currentUser!!.uid

                                        mRef.child("likes").child(gonderi.postID!!)
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(p0: DataSnapshot) {
                                                    try {
                                                        if (p0.hasChild(userID)) {
                                                            mRef.child("likes")
                                                                .child(gonderi.postID!!)
                                                                .child(userID).removeValue()
                                                            isLiked = false // İkonu güncelle
                                                            begenmeSayisi =
                                                                (p0.childrenCount - 1).toInt()// Beğenme sayısını güncelliyoruz
                                                            Notifications.notificationSave(
                                                                gonderi.userID!!,
                                                                Notifications.post_likes_deleted,
                                                                gonderi.postID!!
                                                            )
                                                        } else {
                                                            mRef.child("likes")
                                                                .child(gonderi.postID!!)
                                                                .child(userID).setValue(userID)
                                                            isLiked = true // İkonu güncelle
                                                            begenmeSayisi =
                                                                (p0.childrenCount + 1).toInt()// Beğenme sayısını güncelliyoruz
                                                            Notifications.notificationSave(
                                                                gonderi.userID!!,
                                                                Notifications.post_liked,
                                                                gonderi.postID!!
                                                            )
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("Hata", "Hata: ${e.message}")
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.e("Hata", "İstek iptal edildi: ${error.message}")
                                                }
                                            })
                                    }) {
                                        Icon(
                                            painter = if (isLiked) {
                                                painterResource(id = R.drawable.heartkirmizi)
                                            } else {
                                                painterResource(id = R.drawable.heart)
                                            },
                                            contentDescription = ""
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(onClick = {
                                        try {
                                            selectedPost = gonderi
                                            showCommentSheet = true
                                            Log.e(
                                                "CommentSheet",
                                                "BottomSheet açıldı. Post ID: ${gonderi.postID}"
                                            )

                                            // BottomSheet'in açılmasını sağlamak için
                                            coroutineScope.launch {
                                                sheetState.bottomSheetState.expand()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("CommentSheetError", "Hata: ${e.message}")
                                        }
                                    }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.chat),
                                            contentDescription = ""
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(onClick = {
                                    }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.send),
                                            contentDescription = ""
                                        )
                                    }
                                }
                                Text(
                                    text = "$begenmeSayisi beğenme",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                                Text(
                                    text = "$yorumSayisi yorum",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                )
                                Text(
                                    text = "${gonderi.userName} ${gonderi.postAciklama}",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                                gonderi.postYuklenmeTarihi?.let { timeStamp ->
                                    val timeAgo =
                                        PostTime().getTimeAgo(timeStamp) // getTimeAgo fonksiyonunu çağırıyoruz
                                    Text(
                                        text = timeAgo ?: "Bilinmiyor",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

fun askNotificationPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // İzin zaten verilmiş
            Log.d("Permission", "Bildirim izni zaten verilmiş.")
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            // Kullanıcıya bilgi ver ve izin iste
            AlertDialog.Builder(context)
                .setTitle("Bildirim İzni Gerekli")
                .setMessage("Uygulamanın bildirim gönderebilmesi için izninize ihtiyaç vardır.")
                .setPositiveButton("Tamam") { _, _ ->
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        1
                    )
                }
                .setNegativeButton("Hayır", null)
                .show()
        } else {
            // İzni doğrudan iste
            ActivityCompat.requestPermissions(
                context,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }
}


fun checkIsLiked(postID: String, userID: String, onResult: (Boolean) -> Unit) {
    val mRef = FirebaseDatabase.getInstance().reference
    mRef.child("likes").child(postID).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.hasChild(userID)) {
                onResult(true) // Kullanıcı beğenmiş
            } else {
                onResult(false) // Kullanıcı beğenmemiş
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Hata", "İstek iptal edildi: ${error.message}")
        }
    })
}


fun kullaniciPostlariniGetir(kullaniciID: String, onComplete: (List<UserPosts>) -> Unit) {
    val mRef = FirebaseDatabase.getInstance().reference
    val tumGonderiler = ArrayList<UserPosts>()

    mRef.child("users").child(kullaniciID).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {}

        override fun onDataChange(p0: DataSnapshot) {
            val kullaniciAdi = p0.getValue(Users::class.java)?.user_name ?: ""
            val kullaniciFotoURL = p0.getValue(Users::class.java)?.user_detail?.profile_picture ?: ""

            mRef.child("posts").child(kullaniciID).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChildren()) {
                        for (ds in p0.children) {
                            val eklenecekUserPosts = UserPosts().apply {
                                userID = kullaniciID
                                userName = kullaniciAdi
                                userPhotoURL = kullaniciFotoURL
                                postID = ds.getValue(Posts::class.java)?.post_id
                                postURL = ds.getValue(Posts::class.java)?.photo_url
                                postAciklama = ds.getValue(Posts::class.java)?.aciklama
                                postYuklenmeTarihi = ds.getValue(Posts::class.java)?.yuklenme_tarih
                            }
                            tumGonderiler.add(eklenecekUserPosts)
                        }
                    }
                    // Listeyi ters çevir
                    tumGonderiler.sortByDescending { it.postYuklenmeTarihi }
                    onComplete(tumGonderiler)
                }
            })
        }
    })
}