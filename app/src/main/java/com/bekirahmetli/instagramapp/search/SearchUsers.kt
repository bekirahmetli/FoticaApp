package com.bekirahmetli.instagramapp.search

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.bekirahmetli.instagramapp.R
import com.bekirahmetli.instagramapp.model.SearchUser
import com.bekirahmetli.instagramapp.navigation.Screens
import com.bekirahmetli.instagramapp.profile.ProfileImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun SearchUsers(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var searchUsersList by remember { mutableStateOf(listOf<SearchUser>()) }


    val database = FirebaseDatabase.getInstance()
    val userRef = database.getReference("users")


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
                            // Firebase'deki düğüm adını user_id olarak alıyoruz
                            user.user_id = userSnapshot.key


                            val profilePhotoURL = user.getProfilePhotoURL()
                            if (profilePhotoURL.isNullOrEmpty()) {
                                Log.e("SearchScreen", "Profil fotoğrafı yok")
                            } else {
                                Log.e("SearchScreen", "Profil fotoğrafı: $profilePhotoURL")
                            }

                            userList.add(user)
                        }
                    }

                    // Listeyi güncelle
                    searchUsersList = userList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SearchScreen", "Hata: ${error.message}")
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {
                    navController.navigate(Screens.SearchScreen.name)
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))


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
                        searchUsersWithPosts(query)
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
                                contentDescription = "Ara",
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
                                innerTextField()
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
                Log.e("SearchScreen","Tıklanan user id : ${user.user_id}")
                if (user.user_id == currentUserId) {

                    navController.navigate(route = Screens.ProfileScreen.name)
                } else {
                    navController.navigate("OtherUser/${user.user_id}")
                    Log.e("SearchScreen", "Başka bir kullanıcıya tıklandı")
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