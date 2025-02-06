package com.bekirahmetli.instagramapp.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bekirahmetli.instagramapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileDetay(navController: NavController, onNavigateToEdit: () -> Unit){
    var mAuth: FirebaseAuth
    mAuth = FirebaseAuth.getInstance()
    val isPrivateAccount = remember { mutableStateOf(false) }
    var mUser: FirebaseUser = mAuth.currentUser!!
    var mRef: DatabaseReference = FirebaseDatabase.getInstance().reference

    // Firebase'den veri okuma
    LaunchedEffect(Unit) {
        mUser?.let { user ->
            mRef.child("users").child(mUser.uid).child("hidden_profile").get()
                .addOnSuccessListener { dataSnapshot ->
                    isPrivateAccount.value = dataSnapshot.getValue(Boolean::class.java) ?: false
                }
                .addOnFailureListener {
                    isPrivateAccount.value = false
                }
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(text = "Ayarlar",Modifier.padding(10.dp))},
                modifier = Modifier.height(60.dp),
                navigationIcon = {
                    Button(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(id = R.drawable.geri),
                            contentDescription = null,Modifier.padding(15.dp))
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.topbar),
                    titleContentColor = Color.Black
                )
            )
        },
        content = {contentPadding ->
            Column (
                modifier = Modifier.padding(contentPadding),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ){
                Text(text = "Hesap Ayarları",
                    Modifier.padding(15.dp),
                    fontWeight = FontWeight.Bold)
                TextButton(onClick = {
                    onNavigateToEdit()
                    navController.navigate("ProfilEdit")
                },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Profili Düzenle")
                }
                Spacer(modifier = Modifier.size(5.dp))
                TextButton(onClick = {
                    navController.navigate("LikePosts")
                },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Beğendiğin Gönderiler")
                }
                Spacer(modifier = Modifier.size(5.dp))
                TextButton(onClick = {
                    navController.navigate("ChangePassword")
                },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Şifreni Değiştir")
                }
                Spacer(modifier = Modifier.size(5.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Gizli Hesap")
                    Switch(
                        checked = isPrivateAccount.value,
                        onCheckedChange = { isChecked ->
                            isPrivateAccount.value = isChecked
                            mUser?.let { user ->
                                mRef.child("users").child(mUser.uid).child("hidden_profile")
                                    .setValue(isChecked)
                                    .addOnSuccessListener {
                                    }
                                    .addOnFailureListener {
                                    }
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.size(25.dp))
                val acilisKontrol = remember { mutableStateOf(false) }//alert dialog için kullanıcaz
                TextButton(onClick = {
                    acilisKontrol.value = true
                },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Çıkış Yap")
                }

                if(acilisKontrol.value){
                    AlertDialog(
                        onDismissRequest = { acilisKontrol.value = false },
                        title = { Text(text = "Çıkış Yap")},
                        text = { Text(text = "Instagram'dan çıkış yapmak istediğinize emin misiniz?")},
                        confirmButton = {
                            Text(
                                text = "Çıkış Yap",
                                color = Color.Blue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    acilisKontrol.value = false
                                    mAuth.signOut()
                                })
                        },
                        dismissButton = {
                            Text(
                                text = "İptal",
                                color = Color.Blue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    acilisKontrol.value = false
                                })
                        }
                    )
                }
            }
        }
    )
}