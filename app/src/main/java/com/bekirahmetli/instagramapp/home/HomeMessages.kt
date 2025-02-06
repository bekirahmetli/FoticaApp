package com.bekirahmetli.instagramapp.home

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.bekirahmetli.instagramapp.R
import com.bekirahmetli.instagramapp.model.Messages
import com.bekirahmetli.instagramapp.model.SearchUser
import com.bekirahmetli.instagramapp.navigation.Screens
import com.bekirahmetli.instagramapp.profile.ProfileImage
import com.bekirahmetli.instagramapp.utils.PostTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

@Composable
fun HomeMessages(navController: NavController,postTime:PostTime){
    var searchQuery by remember { mutableStateOf("") } // Arama metni için
    var searchUsersList by remember { mutableStateOf(listOf<SearchUser>()) } // Kullanıcı bilgileri listesi


    val database = FirebaseDatabase.getInstance()
    val userRef = database.getReference("users")
    var mRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    var mAuth:FirebaseAuth = FirebaseAuth.getInstance()
    var mUser: FirebaseUser = mAuth.currentUser!!
    var allmessages by remember { mutableStateOf(listOf<Pair<Messages, SearchUser?>>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Firebase'den mesajları alma
    LaunchedEffect(Unit) {
        isLoading = true
        mRef.child("messages").child(mUser.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<Messages>()
                    for (message in snapshot.children) {
                        val readMessage = message.getValue(Messages::class.java)
                        if (readMessage != null) {
                            readMessage.user_id = message.key
                            messages.add(readMessage)
                        }
                    }

                    // Kullanıcı bilgilerini eşleştir
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val userMap = mutableMapOf<String, SearchUser>()
                            for (user in userSnapshot.children) {
                                val searchUser = user.getValue(SearchUser::class.java)
                                if (searchUser != null) {
                                    searchUser.user_id = user.key
                                    userMap[user.key!!] = searchUser
                                }
                            }

                            // Kullanıcı bilgilerini mesajlarla birleştir
                            val messagesWithUsers = messages.map { message ->
                                val user = userMap[message.user_id]
                                Pair(message, user)
                            }

                            // Listeyi güncelle
                            allmessages = messagesWithUsers
                            isLoading = false
                        }

                        override fun onCancelled(error: DatabaseError) {
                            isLoading = false
                            Log.e("Firebase", "Kullanıcı bilgilerini alma hatası: ${error.message}")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                    Log.e("Firebase", "Mesaj alma hatası: ${error.message}")
                }
            })
    }


    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Kullanıcı arama fonksiyonu
    fun searchUsersWithPosts(query: String) {
        userRef.orderByChild("user_name").startAt(query).endAt("$query\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userList = mutableListOf<SearchUser>()

                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(SearchUser::class.java)
                        if (user != null) {

                            user.user_id = userSnapshot.key


                            val profilePhotoURL = user.getProfilePhotoURL()
                            if (profilePhotoURL.isNullOrEmpty()) {
                                Log.e("MessageScreen", "Profil fotoğrafı yok")
                            } else {
                                Log.e("MessageScreen", "Profil fotoğrafı: $profilePhotoURL")
                            }

                            userList.add(user)
                        }
                    }

                    // Listeyi güncelle
                    searchUsersList = userList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MessageScreen", "Hata: ${error.message}")
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.navigate(Screens.HomeScreen.name)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Arama Çubuğu
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            searchUsersWithPosts(query) // Arama fonksiyonunu çağır
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 36.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.padding(start = 8.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(text = "Ara", color = Color.Gray)
                                    }
                                    innerTextField() // Kullanıcı arama metnini yazacak
                                }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Kullanıcı + Fotoğraf Listesi
            LazyColumn {
                items(searchUsersList) { user ->
                    UserWithPhotoItem(user, currentUserId, navController)
                }
            }

            LazyColumn {
                items(allmessages) { (message, user) -> // Pair yapısını parçala
                    if (user != null) { // Kullanıcı bilgisi varsa
                        MessageItem(
                            user = user,
                            message = message,
                            postTime = postTime,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(user: SearchUser,message: Messages, postTime: PostTime, navController: NavController) {
    val timeAgo = postTime.getTimeAgo(message.time ?: 0L) ?: "Bilinmeyen Zaman"
    var mRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    var mAuth:FirebaseAuth = FirebaseAuth.getInstance()
    var mUser: FirebaseUser = mAuth.currentUser!!

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // Sohbet ekranına yönlendirme
                if (message.user_id != null) {
                    mRef.child("messages")
                        .child(mUser.uid!!)
                        .child(message.user_id!!)
                        .child("seen")
                        .setValue(true)
                        .addOnSuccessListener {
                            navController.navigate("Chat/${message.user_id}")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "Mesaj görülme durumu ayarlanamadı: ${exception.message}")
                        }
                } else {
                    Log.e("Firebase", "Geçersiz user_id")
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileImage(
            painter = rememberAsyncImagePainter(
                model = user.user_detail!!.profile_picture,
                placeholder = painterResource(R.drawable.person)
            ),
            imageSize = 96,
            onClick = {  }
        )

        Spacer(modifier = Modifier.width(8.dp))


        Column(
            modifier = Modifier.weight(1f) // Sağdaki mavi nokta için yer bırak
        ) {
            Text(
                text = user.user_name ?: "Bilinmiyor",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = message.last_message ?: "Mesaj yok",
                color = if (message.seen == true) Color.Gray else Color.Black, // Okunmamış mesaj koyu renkte
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (message.seen == true) FontWeight.Normal else FontWeight.Bold // Okunmamış mesaj kalın yazı tipi
            )
            Text(
                text = timeAgo,
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Okunmamış mesajlar için mavi nokta
        if (message.seen == false) {
            Box(
                modifier = Modifier
                    .size(8.dp) // Küçük bir mavi nokta
                    .clip(CircleShape)
                    .background(Color.Blue)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun UserWithPhotoItem(
    user: SearchUser,
    currentUserId: String?,
    navController: NavController
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val profileImageUrl = user.getProfilePhotoURL()
        ProfileImage(
            painter = rememberAsyncImagePainter(
                model = profileImageUrl,
                placeholder = painterResource(R.drawable.person),
                error = painterResource(R.drawable.person)
            ),
            imageSize = 96,
            onClick = {
                Log.e("MessageScreen","Tıklanan user id : ${user.user_id}")
                if (user.user_id == currentUserId) {
                    // Eğer tıklanan kullanıcı mevcut oturum açmış kullanıcıysa profil ekranına yönlendir
                } else {
                    navController.navigate("Chat/${user.user_id}")
                    Log.e("MessageScreen", "Başka bir kullanıcıya tıklandı")
                }
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(text = user.user_name ?: "Kullanıcı Adı Yok")
            Text(text = user.adi_soyadi ?: "Adı Soyadı Yok")
        }
    }
}