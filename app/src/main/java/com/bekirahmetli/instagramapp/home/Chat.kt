package com.bekirahmetli.instagramapp.home

import android.util.Log
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.bekirahmetli.instagramapp.R
import com.bekirahmetli.instagramapp.model.Message
import com.bekirahmetli.instagramapp.model.Users
import com.bekirahmetli.instagramapp.profile.ProfileImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(userId: String?,message: Message,navController: NavController) {
    Log.e("ChatUserID :","${userId}")
    var messageText by remember { mutableStateOf("") }
    val tumMesajlar = remember { mutableStateListOf<Message>() }
    var isExpanded by remember { mutableStateOf(false) }

    var mRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    var mAuth:FirebaseAuth = FirebaseAuth.getInstance()
    var mUser:FirebaseUser = mAuth.currentUser!!

    val listState = rememberLazyListState()
    var isTyping by remember { mutableStateOf(false) }
    var otherIsTyping by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = mUser.uid) {
        // Mesaj sahibi ve mevcut kullanıcı kontrolü
        if (mUser.uid != null && message.user_id != null && mUser.uid != message.user_id) {

            mRef.child("messages")
                .child(mUser.uid!!)
                .child(message.user_id!!)
                .child("seen")
                .setValue(true)
                .addOnSuccessListener {
                    Log.e("Firebase", "Görüldü durumu başarıyla güncellendi.")
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Mesaj görülme durumu ayarlanamadı: ${exception.message}")
                }
        } else {
            Log.e("Firebase", "Güncelleme için geçerli user_id'ler bulunamadı.")
        }
    }
    // Firebase'de typing durumunu dinleme
    LaunchedEffect(userId) {
        val currentUserId = mUser.uid
        val otherUserId = userId

        mRef.child("messages")
            .child(otherUserId!!)
            .child(currentUserId)
            .child("typing")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    otherIsTyping = snapshot.getValue(Boolean::class.java) ?: false
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Typing", "Error: ${error.message}")
                }
            })
    }


    LaunchedEffect(tumMesajlar.size) { // Mesaj sayısı değiştiğinde çalışır
        if (tumMesajlar.isNotEmpty()) {
            listState.animateScrollToItem(tumMesajlar.size - 1)
        }
    }



    var otheruserName by remember { mutableStateOf("") }
    var otherprofileImageUrl by remember { mutableStateOf<String?>(null) }
    var currentprofileImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        try {
            mRef.child("users").child(userId!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    try {
                        if (p0.getValue() != null) {
                            var okunankullaniciBilgileri = p0.getValue(Users::class.java)
                            otheruserName = okunankullaniciBilgileri!!.user_name.orEmpty()
                            otherprofileImageUrl = okunankullaniciBilgileri!!.user_detail!!.profile_picture.orEmpty()
                        }
                    } catch (e: Exception) {
                        Log.e("Chat", "Error: ${e.message}")
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })

            mRef.child("users").child(mUser!!.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    try {
                        if (p0.getValue() != null) {
                            var okunankullaniciBilgileri = p0.getValue(Users::class.java)
                            currentprofileImageUrl = okunankullaniciBilgileri!!.user_detail!!.profile_picture.orEmpty()
                        }
                    } catch (e: Exception) {
                        Log.e("Chat", "Error: ${e.message}")
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })
        } catch (e: Exception) {
            Log.e("Chat", "Error: ${e.message}")
        }
    }

    LaunchedEffect(userId) {
        mRef.child("chats").child(mUser.uid).child(userId!!).addChildEventListener(object :
            ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val yeniMesaj = snapshot.getValue(Message::class.java)
                yeniMesaj?.let {
                    tumMesajlar.add(it)
                    Log.e("ChatMessage", "Gelen mesaj: ${it.chat}")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Silinen mesajları listeden çıkarma işlemi
                val silinenMesaj = snapshot.getValue(Message::class.java)
                silinenMesaj?.let {
                    tumMesajlar.remove(it)
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatMessage", "Error: ${error.message}")
            }
        })
    }

    Scaffold (
        topBar = {
            TopAppBar(
                modifier = Modifier.height(40.dp),
                title = { Text(text = otheruserName, fontStyle = FontStyle.Normal, fontSize = 20.sp)},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("HomeMessages")
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.topbar),
                    titleContentColor = Color.Black
                )
            )
        },
        content = { contentPadding ->// Ekran içeriği

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.padding(contentPadding)
                ) {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 85.dp),
                            state = listState
                    ) {
                        items(tumMesajlar) { mesaj ->
                            if (mesaj.user_id == mUser.uid) {
                                // Kullanıcının mesajı (Sağ tarafta)
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .wrapContentWidth() //genişlik mesajın boyutuna göre ayarlanacak
                                            .wrapContentHeight() //yükseklik mesajın boyutuna göre ayarlanacak
                                            .padding(3.dp),
                                        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.teal_200))
                                    ) {
                                        Text(
                                            text = mesaj.chat.orEmpty(),
                                            modifier = Modifier.padding(8.dp),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.White,
                                            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                            overflow = TextOverflow.Ellipsis // Taşan metin '...' ile gösterilir
                                        )

                                        if (mesaj.chat.orEmpty().length > 100) { // Metin çok uzunsa
                                            TextButton(onClick = { isExpanded = !isExpanded }) {
                                                Text(
                                                    text = if (isExpanded) "Daha az" else "Daha fazla",
                                                    color = Color.Blue,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Karşı tarafın mesajı (Sol tarafta)
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    ProfileImage(
                                        painter = rememberImagePainter(data = otherprofileImageUrl),
                                        imageSize = 36,
                                        onClick = {  }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Card(
                                        modifier = Modifier
                                            .wrapContentWidth() // Card, içerik kadar genişlesin
                                            .padding(3.dp),
                                        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.purple_200))
                                    ) {
                                        Text(
                                            text = mesaj.chat.orEmpty(),
                                            modifier = Modifier.padding(8.dp),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Black,
                                            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                            overflow = TextOverflow.Ellipsis // Taşan metin '...' ile gösterilir
                                        )

                                        if (mesaj.chat.orEmpty().length > 100) { // Metin çok uzunsa
                                            TextButton(onClick = { isExpanded = !isExpanded }) {
                                                Text(
                                                    text = if (isExpanded) "Daha az" else "Daha fazla",
                                                    color = Color.Blue,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // "Yazıyor..." Text
                if (otherIsTyping) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(bottom = 86.dp)
                            .zIndex(1f)
                        ,
                        colors = CardDefaults.cardColors(containerColor = Color.Gray)
                    ) {
                        Text(
                            text = "Yazıyor...",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp)
                    ) {
                        ProfileImage(
                            painter = rememberImagePainter(data = currentprofileImageUrl),
                            imageSize = 36,
                            onClick = {  }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = {
                                messageText = it
                                val currentUserId = mUser.uid
                                val otherUserId = userId

                                if (!isTyping && it.isNotEmpty()) {
                                    isTyping = true
                                    mRef.child("messages")
                                        .child(currentUserId)
                                        .child(otherUserId!!)
                                        .child("typing")
                                        .setValue(true)
                                }
                                if (it.isEmpty() && isTyping) {
                                    isTyping = false
                                    mRef.child("messages")
                                        .child(currentUserId)
                                        .child(otherUserId!!)
                                        .child("typing")
                                        .setValue(false)
                                }
                            },
                            label = { Text("Mesaj...") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp),
                            singleLine = true, // Tek satır olacak
                            maxLines = 1,     // Maksimum satır sayısı 1
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
                        )
                        Button(
                            onClick = {
                                if (messageText.trim().isNotEmpty()) {
                                    val currentUserId = mUser.uid
                                    val otherUserId = userId

                                    // Mesaj gönderme işlemleri...
                                    isTyping = false
                                    mRef.child("messages")
                                        .child(currentUserId)
                                        .child(otherUserId!!)
                                        .child("typing")
                                        .setValue(false)

                                    var mesajAtan = HashMap<String, Any>()
                                    mesajAtan.put("chat", messageText)
                                    mesajAtan.put("seen", true)
                                    mesajAtan.put("time", ServerValue.TIMESTAMP)
                                    mesajAtan.put("type", "text")
                                    mesajAtan.put("user_id", mUser.uid)

                                    var newMessageKey =
                                        mRef.child("chats").child(mUser.uid).child(userId!!)
                                            .push().key
                                    mRef.child("chats").child(mUser.uid).child(userId!!)
                                        .child(newMessageKey!!).setValue(mesajAtan)

                                    var mesajAlan = HashMap<String, Any>()
                                    mesajAlan.put("chat", messageText)
                                    mesajAlan.put("seen", false)
                                    mesajAlan.put("time", ServerValue.TIMESTAMP)
                                    mesajAlan.put("type", "text")
                                    mesajAlan.put("user_id", mUser.uid)
                                    mRef.child("chats").child(userId).child(mUser.uid)
                                        .child(newMessageKey).setValue(mesajAlan)


                                    var konusmamesajAtan = HashMap<String, Any>()
                                    konusmamesajAtan.put("time", ServerValue.TIMESTAMP)
                                    konusmamesajAtan.put("seen", true)
                                    konusmamesajAtan.put("last_message", messageText)
                                    konusmamesajAtan.put("typing", false)

                                    mRef.child("messages").child(mUser.uid).child(userId!!)
                                        .setValue(konusmamesajAtan)

                                    var konusmamesajAlan = HashMap<String, Any>()
                                    konusmamesajAlan.put("time", ServerValue.TIMESTAMP)
                                    konusmamesajAlan.put("seen", false)
                                    konusmamesajAlan.put("last_message", messageText)
                                    konusmamesajAlan.put("typing", false)

                                    mRef.child("messages").child(userId).child(mUser.uid)
                                        .setValue(konusmamesajAlan)

                                    messageText = ""
                                }
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(text = "Gönder")
                        }
                    }
                }
            }
        }
    )
}