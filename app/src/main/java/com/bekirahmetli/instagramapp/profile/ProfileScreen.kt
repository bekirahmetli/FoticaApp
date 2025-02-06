package com.bekirahmetli.instagramapp.profile

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import com.bekirahmetli.instagramapp.R
import com.bekirahmetli.instagramapp.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import com.bekirahmetli.instagramapp.model.Posts
import com.bekirahmetli.instagramapp.model.UserPosts
import com.bekirahmetli.instagramapp.utils.CommentSheet
import com.bekirahmetli.instagramapp.utils.GridPost
import com.bekirahmetli.instagramapp.utils.PostTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(navController: NavHostController) {
    val menuAcilisKontrol = remember { mutableStateOf(false) }
    var mRef:DatabaseReference
    mRef = FirebaseDatabase.getInstance().reference
    var mAuth:FirebaseAuth
    mAuth = FirebaseAuth.getInstance()
    var mUser:FirebaseUser
    mUser = mAuth.currentUser!!
    var isLazyColumnVisible by remember { mutableStateOf(false) }
    var isLazyColumnVisible2 by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }


    var userName by remember { mutableStateOf("") }
    var adiSoyadi by remember { mutableStateOf("") }
    var followerSayisi by remember { mutableStateOf("") }
    var followingSayisi by remember { mutableStateOf("") }
    var postSayisi by remember { mutableStateOf("") }
    var biyografi by remember { mutableStateOf("") }
    var webSite by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    val tumGonderiler = remember { mutableStateListOf<UserPosts>() }
    LaunchedEffect(Unit) {
        try {
            mRef.child("users").child(mUser!!.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    try {
                        if (p0.getValue() != null) {
                            var okunankullaniciBilgileri = p0.getValue(Users::class.java)
                            userName = okunankullaniciBilgileri!!.user_name.orEmpty()
                            adiSoyadi = okunankullaniciBilgileri!!.adi_soyadi.orEmpty()
                            followerSayisi = okunankullaniciBilgileri!!.user_detail!!.follower.orEmpty()
                            followingSayisi = okunankullaniciBilgileri!!.user_detail!!.followed.orEmpty()
                            postSayisi = okunankullaniciBilgileri!!.user_detail!!.post.orEmpty()
                            biyografi = okunankullaniciBilgileri!!.user_detail!!.biography.orEmpty()
                            webSite = okunankullaniciBilgileri!!.user_detail!!.web_site.orEmpty()
                            profileImageUrl = okunankullaniciBilgileri!!.user_detail!!.profile_picture.orEmpty()

                            isLoading = false
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileScreen", "Error: ${e.message}")
                        isLoading = false
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.e("ProfileScreen", "DatabaseError: ${p0.message}")
                    isLoading = false
                }
            })
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Error: ${e.message}")
            isLoading = false
        }
    }


    Scaffold (
        topBar = {
            TopAppBar(
                modifier = Modifier.height(40.dp),
                title = { Text(text = userName, fontStyle = FontStyle.Normal, fontSize = 20.sp)},
                actions = {
                    IconButton(onClick = { menuAcilisKontrol.value = true }) {
                        Icon(painter = painterResource(id = R.drawable.ucnokta), contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuAcilisKontrol.value,
                        onDismissRequest = { menuAcilisKontrol.value = false }) {
                        DropdownMenuItem(
                            onClick = {
                                menuAcilisKontrol.value = false
                                navController.navigate("Ayarlar")
                                      },
                            text = { Text( "Ayarlar") },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.topbar),
                    titleContentColor = Color.Black
                )
            )
        },
        content = {contentPadding ->// Ekran içeriği
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
                    modifier = Modifier.padding(contentPadding),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        val painter = rememberImagePainter(
                            data = profileImageUrl,
                            builder = {
                                crossfade(true)
                                placeholder(R.drawable.person)
                                error(R.drawable.person)
                            }
                        )

                        ProfileImage(
                            painter = painter,
                            imageSize = 96,
                            onClick = {  }
                        )
                        Spacer(modifier = Modifier.size(35.dp))
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = postSayisi)
                                Spacer(modifier = Modifier.size(5.dp))
                                Text(
                                    text = "gönderi"
                                )
                            }
                            Spacer(modifier = Modifier.size(25.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = followerSayisi)
                                Spacer(modifier = Modifier.size(5.dp))
                                Text(text = "takipçi")
                            }
                            Spacer(modifier = Modifier.size(25.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = followingSayisi)
                                Spacer(modifier = Modifier.size(5.dp))
                                Text(text = "takip")
                            }
                        }
                    }
                    Button(
                        onClick = {
                            navController.navigate("ProfilEdit")
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(5.dp)
                            .fillMaxWidth(),

                    ) {
                        Text(text = "Profili Düzenle")
                    }
                    Column(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = adiSoyadi)
                        Spacer(modifier = Modifier.size(5.dp))
                        Text(text = biyografi)
                        Spacer(modifier = Modifier.size(5.dp))
                        Text(text = webSite)


                        Divider(
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                isLazyColumnVisible2 = !isLazyColumnVisible2
                                if (isLazyColumnVisible2) isLazyColumnVisible = false
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.grid),
                                    contentDescription = "Icon 1"
                                )
                            }
                            IconButton(onClick = {
                                isLazyColumnVisible = !isLazyColumnVisible
                                if (isLazyColumnVisible) isLazyColumnVisible2 = false
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.kaydirmali),
                                    contentDescription = "Icon 2"
                                )
                            }
                            IconButton(onClick = {  }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.location),
                                    contentDescription = "Icon 3"
                                )
                            }
                            IconButton(onClick = {  }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.person),
                                    contentDescription = "Icon 4"
                                )
                            }
                        }

                        if (isLazyColumnVisible) {
                            Log.e("VeriGetir", "post ${Post()}")
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(tumGonderiler) { gonderi ->
                                    Post() // Gönderi içeriği burada yer alacak
                                }
                            }
                        }

                        if (isLazyColumnVisible2) {
                            Log.e("VeriGetir", "post2 ${GridPost()}")
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(tumGonderiler) { gonderi ->
                                    GridPost() // Gönderi içeriği burada yer alacak
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Post() {
    val tumGonderiler = remember { mutableStateListOf<UserPosts>() }
    val mAuth = FirebaseAuth.getInstance()
    val mUser = mAuth.currentUser
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberBottomSheetScaffoldState()
    var selectedPost by remember { mutableStateOf<UserPosts?>(null) }
    var showCommentSheet by remember { mutableStateOf(false) }


    // Kullanıcı postlarını çekme işlemi
    mUser?.uid?.let { kullaniciID ->
        PostGetir(kullaniciID) { gonderiler ->
            tumGonderiler.clear()
            tumGonderiler.addAll(gonderiler)
        }
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
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues)
            ) {
                items(tumGonderiler) { gonderi ->


                    var begenmeSayisi by remember { mutableStateOf(0) }


                    var isLiked by remember { mutableStateOf(false) }

                    // Beğenme durumunu Firebase'den kontrol et
                    FirebaseAuth.getInstance().currentUser?.let { user ->
                        Like(gonderi.postID!!, user.uid) { liked ->
                            isLiked = liked
                        }
                    }

                    // Beğeni sayısını her seferinde güncelleme
                    FirebaseDatabase.getInstance().reference.child("likes").child(gonderi.postID!!)
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
                            // Kullanıcı bilgileri ve gönderi resmi
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
                                contentScale = ContentScale.Crop // Görselleri hücrelere tam oturtur
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                IconButton(onClick = {
                                    Log.e("ID", "postid: ${gonderi.postID}")
                                    Log.e("ID", "userid: ${gonderi.userID}")
                                    var mRef = FirebaseDatabase.getInstance().reference
                                    var userID = FirebaseAuth.getInstance().currentUser!!.uid

                                    mRef.child("likes").child(gonderi.postID!!).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(p0: DataSnapshot) {
                                            try {
                                                if (p0.hasChild(userID)) {
                                                    mRef.child("likes").child(gonderi.postID!!).child(userID).removeValue()
                                                    isLiked = false // İkonu güncelle
                                                    begenmeSayisi = (p0.childrenCount - 1).toInt()// Beğenme sayısını güncelliyoruz
                                                } else {
                                                    mRef.child("likes").child(gonderi.postID!!).child(userID).setValue(userID)
                                                    isLiked = true // İkonu güncelle
                                                    begenmeSayisi = (p0.childrenCount + 1).toInt()// Beğenme sayısını güncelliyoruz
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
                                        Log.e("CommentSheet", "BottomSheet açıldı. Post ID: ${gonderi.postID}")

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
                                text = "${gonderi.userName} ${gonderi.postAciklama}",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                            gonderi.postYuklenmeTarihi?.let { timeStamp ->
                                val timeAgo = PostTime().getTimeAgo(timeStamp) // getTimeAgo fonksiyonunu çağırıyoruz
                                Text(
                                    text = timeAgo ?: "Bilinmiyor",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}


fun Like(postID: String, userID: String, onResult: (Boolean) -> Unit) {
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


fun PostGetir(kullaniciID: String, onComplete: (List<UserPosts>) -> Unit) {
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