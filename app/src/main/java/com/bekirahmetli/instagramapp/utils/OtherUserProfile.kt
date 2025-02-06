package com.bekirahmetli.instagramapp.utils

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.bekirahmetli.instagramapp.R
import com.bekirahmetli.instagramapp.model.Posts
import com.bekirahmetli.instagramapp.model.UserPosts
import com.bekirahmetli.instagramapp.model.Users
import com.bekirahmetli.instagramapp.profile.ProfileImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfile(userId: String?){
   Log.e("otheruser","gelen user id : ${userId}")

   val menuAcilisKontrol = remember { mutableStateOf(false) }
   var mRef: DatabaseReference
   mRef = FirebaseDatabase.getInstance().reference
   var mAuth:FirebaseAuth
   mAuth = FirebaseAuth.getInstance()
   var mUser:FirebaseUser
   mUser = mAuth.currentUser!!
   var isLazyColumnVisible by remember { mutableStateOf(false) }
   var isLazyColumnVisible2 by remember { mutableStateOf(true) }

   // Kullanıcı bilgileri için state'ler
   var userName by remember { mutableStateOf("") }
   var adiSoyadi by remember { mutableStateOf("") }
   var followerSayisi by remember { mutableStateOf("") }
   var followingSayisi by remember { mutableStateOf("") }
   var postSayisi by remember { mutableStateOf("") }
   var biyografi by remember { mutableStateOf("") }
   var webSite by remember { mutableStateOf("") }
   var profileImageUrl by remember { mutableStateOf<String?>(null) }
   val tumGonderiler = remember { mutableStateListOf<UserPosts>() }

   LaunchedEffect(userId) {
      try {
         mRef.child("users").child(userId!!).addValueEventListener(object : ValueEventListener {
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

   val isFollowing = remember { mutableStateOf(false) }

   // Kullanıcı profili açıldığında takip durumunu kontrol et
   LaunchedEffect(Unit) {
      mRef.child("following").child(mUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
         override fun onDataChange(snapshot: DataSnapshot) {
            isFollowing.value = snapshot.hasChild(userId!!)
         }
         override fun onCancelled(error: DatabaseError) {}
      })
   }

   // Buton rengi, kenarlık ve yazı rengi duruma göre değişiyor
   val buttonColor = if (isFollowing.value) Color.White else Color.Blue
   val borderColor = if (isFollowing.value) Color.Black else Color.Transparent
   val textColor = if (isFollowing.value) Color.Black else Color.White
   val buttonText = if (isFollowing.value) "Takibi Bırak" else "Takip Et"


   // Divider ve altındaki içeriği gizlemek için bir state oluşturuyoruz
   var isHiddenProfile by remember { mutableStateOf(false) }
   var isFollowRequestSent by remember { mutableStateOf(false) } // Takip isteği gönderildi mi kontrolü için

   LaunchedEffect(Unit) {
      mRef.child("users").child(userId!!).child("hidden_profile")
         .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               isHiddenProfile = snapshot.getValue(Boolean::class.java) ?: false
            }

            override fun onCancelled(error: DatabaseError) {
               Log.e("ProfileScreen", "Error: ${error.message}")
            }
         })
      mRef.child("follow_requests").child(userId).child(mUser.uid)
         .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               isFollowRequestSent = snapshot.exists()
            }
            override fun onCancelled(error: DatabaseError) {}
         })
   }

   // Gizli profil kontrolü ve takip durumu
   LaunchedEffect(userId) {
      mRef.child("users").child(userId!!).child("hidden_profile")
         .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               val hiddenProfile = snapshot.getValue(Boolean::class.java) ?: false

               if (!hiddenProfile) {
                  // Profil artık gizli değil, bekleyen takip isteklerini işleme al
                  mRef.child("follow_requests").child(userId)
                     .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(requestSnapshot: DataSnapshot) {
                           // Bekleyen takip isteği varsa
                           if (requestSnapshot.exists()) {
                              requestSnapshot.children.forEach { request ->
                                 val followerId = request.key
                                 // Takip işlemini gerçekleştir
                                 mRef.child("following").child(followerId!!).child(userId)
                                    .setValue(userId)
                                 mRef.child("follower").child(userId).child(followerId)
                                    .setValue(followerId)
                                 // Takip isteğini kaldır
                                 mRef.child("follow_requests").child(userId).child(followerId).removeValue()
                                 Notifications.notificationSave(userId,Notifications.delete_follow_requests)
                                 Notifications.notificationSave(userId,Notifications.started_following)

                                 // Eğer takip edilen kullanıcı mevcut kullanıcıysa UI güncelle
                                 if (followerId == mUser.uid) {
                                    isFollowing.value = true
                                    isFollowRequestSent = false
                                 }

                                 followedfollowercount(userId)
                              }
                           }
                        }

                        override fun onCancelled(error: DatabaseError) {
                           Log.e("ProfileScreen", "Takip isteği error: ${error.message}")
                        }
                     })
               }
            }

            override fun onCancelled(error: DatabaseError) {
               Log.e("ProfileScreen", "Gizli profil error: ${error.message}")
            }
         })
   }



   Scaffold (
      topBar = {
         TopAppBar(
            modifier = Modifier.height(40.dp),
            title = { Text(text = userName, fontStyle = FontStyle.Normal, fontSize = 20.sp) },
            colors = TopAppBarDefaults.topAppBarColors(
               containerColor = colorResource(id = R.color.topbar),
               titleContentColor = Color.Black
            )
         )
      },
      content = {contentPadding ->// Ekran içeriği

         Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
         ){
            Row (
               verticalAlignment = Alignment.Top,
               horizontalArrangement = Arrangement.Start
            ){
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
               Row (
                  verticalAlignment = Alignment.Top,
                  horizontalArrangement = Arrangement.Start
               ){
                  Column(
                     horizontalAlignment = Alignment.CenterHorizontally
                  ){
                     Text(text = postSayisi)
                     Spacer(modifier = Modifier.size(5.dp))
                     Text(text = "gönderi")
                  }
                  Spacer(modifier = Modifier.size(25.dp))
                  Column(
                     horizontalAlignment = Alignment.CenterHorizontally
                  ){
                     Text(text = followerSayisi)
                     Spacer(modifier = Modifier.size(5.dp))
                     Text(text = "takipçi")
                  }
                  Spacer(modifier = Modifier.size(25.dp))
                  Column(
                     horizontalAlignment = Alignment.CenterHorizontally
                  ){
                     Text(text = followingSayisi)
                     Spacer(modifier = Modifier.size(5.dp))
                     Text(text = "takip")
                  }
               }
            }
            Button(onClick = {
               mRef.child("following").child(mUser.uid).addListenerForSingleValueEvent(object : ValueEventListener{
                  override fun onDataChange(p0: DataSnapshot) {
                     if(p0!!.hasChild(userId!!)){
                        // Kullanıcı zaten takip ediyorsa, takipten çıkar
                        mRef.child("following").child(mUser.uid).child(userId).removeValue()
                        mRef.child("follower").child(userId).child(mUser.uid).removeValue()
                        isFollowing.value = false// Artık takip edilmiyor
                        followedfollowercount(userId)// Takipçi sayısını güncelle
                        isFollowRequestSent = false// Takip isteği gönderilmiş değil
                        Notifications.notificationSave(userId,Notifications.stopped_following)
                     }else {
                        if(isFollowRequestSent){
                           // Eğer takip isteği gönderildiyse, isteği iptal et
                           mRef.child("follow_requests").child(userId).child(mUser.uid).removeValue()
                           isFollowRequestSent = false // İstek iptal edildiğinde, buton "Takip Et" yazısına döner
                           Notifications.notificationSave(userId,Notifications.delete_follow_requests)
                        } else {
                           if (isHiddenProfile == true) {
                              // Kullanıcının profili gizliyse, takip isteği gönder
                              mRef.child("follow_requests").child(userId).child(mUser.uid)
                                 .setValue(mUser.uid)
                              isFollowRequestSent = true// Takip isteği gönderildi
                              Notifications.notificationSave(userId,Notifications.follow_request)
                           } else {
                              // Kullanıcının profili açık ve doğrudan takip edilebilir
                              mRef.child("following").child(mUser.uid).child(userId)
                                 .setValue(userId)
                              mRef.child("follower").child(userId).child(mUser.uid)
                                 .setValue(mUser.uid)
                              isFollowing.value = true // Artık takip ediliyor
                              followedfollowercount(userId)// Takipçi sayısını güncelle
                              isFollowRequestSent = false// Takip isteği durumu yok
                              Notifications.notificationSave(userId,Notifications.started_following)
                           }
                        }

                     }
                  }

                  override fun onCancelled(p0: DatabaseError) {

                  }

               })
            },
               modifier = Modifier
                  .align(Alignment.End)
                  .padding(5.dp)
                  .fillMaxWidth()
                  .border(BorderStroke(1.dp, borderColor)),
            colors = ButtonDefaults.buttonColors(containerColor  = buttonColor)

            ) {
               Text(text = if (isFollowRequestSent) "İstek Gönderildi" else buttonText, color = textColor)
            }
            Column (
               modifier = Modifier
                  .padding(4.dp)
                  .fillMaxSize(),
               verticalArrangement = Arrangement.Top,
               horizontalAlignment = Alignment.Start
            ){
               Text(text = adiSoyadi)
               Spacer(modifier = Modifier.size(5.dp))
               Text(text = biyografi)
               Spacer(modifier = Modifier.size(5.dp))
               Text(text = webSite)

               if (isHiddenProfile && !isFollowing.value) {
                  // Gizli profil durumu
                  Column(
                     modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                     verticalArrangement = Arrangement.Top,
                     horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                     Icon(
                        painter = painterResource(id = R.drawable.baseline_lock_outline_24),
                        contentDescription = "Gizli Hesap",
                        tint = Color.Gray,
                        modifier = Modifier.size(96.dp)
                     )
                     Spacer(modifier = Modifier.height(32.dp))
                     Text(
                        text = "Bu hesap gizli",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                     )
                     Spacer(modifier = Modifier.height(8.dp))
                     Text(
                        text = "Fotoğraf ve videoları görmek için bu hesabı takip et.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium
                     )
                  }
               }else {

                  // İnce çizgi
                  Divider(
                     color = Color.Gray,
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
                     IconButton(onClick = { }) {
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
                     Log.e("VeriGetir", "post ${Post(userId)}")
                     LazyColumn(
                        modifier = Modifier.fillMaxSize()
                     ) {
                        items(tumGonderiler) { gonderi ->
                           Post(userId) // Gönderi içeriği burada yer alacak
                        }
                     }
                  }

                  if (isLazyColumnVisible2) {
                     Log.e("VeriGetir", "post2 ${GridPost(userId)}")
                     LazyColumn(
                        modifier = Modifier.fillMaxSize()
                     ) {
                        items(tumGonderiler) { gonderi ->
                           GridPost(userId) // Gönderi içeriği burada yer alacak
                        }
                     }
                  }
               }
            }
         }
      }
   )
}

fun followedfollowercount(userId: String?){
   var mRef: DatabaseReference = FirebaseDatabase.getInstance().reference
   var mAuth:FirebaseAuth = FirebaseAuth.getInstance()
   var mUser:FirebaseUser = mAuth.currentUser!!

   mRef.child("following").child(mUser.uid).addListenerForSingleValueEvent(object : ValueEventListener{
      override fun onDataChange(p0: DataSnapshot) {
         var followednumber = p0.childrenCount.toString()

         mRef.child("follower").child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
               var followernumber = p0.childrenCount.toString()
               mRef.child("users").child(mUser.uid).child("user_detail").child("followed").setValue(followednumber)
               mRef.child("users").child(userId).child("user_detail").child("follower").setValue(followernumber)
            }

            override fun onCancelled(error: DatabaseError) {

            }

         })
      }

      override fun onCancelled(error: DatabaseError) {

      }

   })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Post(userId: String?) {

   val tumGonderiler = remember { mutableStateListOf<UserPosts>() }
   val coroutineScope = rememberCoroutineScope()

   val sheetState = rememberBottomSheetScaffoldState()
   var selectedPost by remember { mutableStateOf<UserPosts?>(null) }
   var showCommentSheet by remember { mutableStateOf(false) }


   // Kullanıcı postlarını çekme işlemi
   LaunchedEffect (userId){
      PostGetir(userId!!) { gonderiler ->
         tumGonderiler.clear()
         tumGonderiler.addAll(gonderiler) // Yeni verileri ekle
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
                                       Notifications.notificationSave(gonderi.userID!!,
                                          Notifications.post_likes_deleted,gonderi.postID!!)
                                    } else {
                                       mRef.child("likes").child(gonderi.postID!!).child(userID).setValue(userID)
                                       isLiked = true // İkonu güncelle
                                       begenmeSayisi = (p0.childrenCount + 1).toInt()// Beğenme sayısını güncelliyoruz
                                       Notifications.notificationSave(gonderi.userID!!,
                                          Notifications.post_liked,gonderi.postID!!)
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
                        val timeAgo = PostTime().getTimeAgo(timeStamp)
                        Text(
                           text = timeAgo ?: "Bilinmiyor",
                           fontSize = 14.sp,
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


fun PostGetir(userId: String?, onComplete: (List<UserPosts>) -> Unit) {
   val mRef = FirebaseDatabase.getInstance().reference
   val tumGonderiler = ArrayList<UserPosts>()

   mRef.child("users").child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener {
      override fun onCancelled(p0: DatabaseError) {}

      override fun onDataChange(p0: DataSnapshot) {
         val kullaniciAdi = p0.getValue(Users::class.java)?.user_name ?: ""
         val kullaniciFotoURL = p0.getValue(Users::class.java)?.user_detail?.profile_picture ?: ""

         mRef.child("posts").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
               if (p0.hasChildren()) {
                  for (ds in p0.children) {
                     val eklenecekUserPosts = UserPosts().apply {
                        userID = userId
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

@Composable
fun GridPost(userId: String?) {
   val tumGonderiler = remember { mutableStateListOf<UserPosts>() }

   // Kullanıcı postlarını çekme işlemi
   LaunchedEffect (userId){
      PostGetir(userId) { gonderiler ->
         tumGonderiler.clear()
         tumGonderiler.addAll(gonderiler) // Yeni verileri ekle
      }
   }

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
               contentScale = ContentScale.Crop
            )
         }
      }
   }
}