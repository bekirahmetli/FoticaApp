package com.bekirahmetli.instagramapp.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bekirahmetli.instagramapp.R
import com.bekirahmetli.instagramapp.model.Users
import com.bekirahmetli.instagramapp.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePassword(onNavigateBack: () -> Unit, navController: NavController){
    var availablePassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordAgain by remember { mutableStateOf("") }


    val context = LocalContext.current
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    val mRef = FirebaseDatabase.getInstance().reference

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordAgainVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(50.dp),
                title = { Text(text = "Şifreyi Değiştir", fontStyle = FontStyle.Italic, fontSize = 27.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(route = "Ayarlar")
                    }) {
                        Icon(painter = painterResource(id = R.drawable.close), contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        when {
                            availablePassword.length < 6 -> {
                                Toast.makeText(context, "Mevcut şifre en az 6 karakter olmalı", Toast.LENGTH_SHORT).show()
                            }
                            newPassword.isEmpty() || newPasswordAgain.isEmpty() -> {
                                Toast.makeText(context, "Yeni şifre alanları boş bırakılamaz", Toast.LENGTH_SHORT).show()
                            }
                            newPassword.length < 6 || newPasswordAgain.length < 6 -> {
                                Toast.makeText(context, "Yeni şifre en az 6 karakter olmalı", Toast.LENGTH_SHORT).show()
                            }
                            newPassword != newPasswordAgain -> {
                                Toast.makeText(context, "Şifreler eşleşmiyor", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                currentUserID?.let { userID ->
                                    mRef.child("users").child(userID).get().addOnSuccessListener { snapshot ->
                                        val user = snapshot.getValue(Users::class.java)
                                        if (user?.password == availablePassword) {
                                            mRef.child("users").child(userID).child("password").setValue(newPassword)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Şifre başarıyla değiştirildi", Toast.LENGTH_SHORT).show()
                                                    navController.navigate(route = Screens.ProfileScreen.name) {
                                                        popUpTo("ShareNext") { inclusive = true }
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Şifre değiştirilemedi. Tekrar deneyin.", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            Toast.makeText(context, "Mevcut şifre yanlış", Toast.LENGTH_SHORT).show()
                                        }
                                    }.addOnFailureListener {
                                        Toast.makeText(context, "Bir hata oluştu. Tekrar deneyin.", Toast.LENGTH_SHORT).show()
                                    }
                                } ?: Toast.makeText(context, "Kullanıcı oturumu açık değil", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(painter = painterResource(id = R.drawable.done), contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.topbar),
                    titleContentColor = colorResource(id = R.color.black)
                )
            )
        },
        content = { contentPadding ->
            LazyColumn(
                modifier = Modifier.padding(contentPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(all = 5.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.Start
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = availablePassword,
                            onValueChange = { availablePassword = it },
                            label = { Text(text = "Mevcut Şifre") },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (isPasswordVisible) R.drawable.visibility else R.drawable.visibility_off
                                        ),
                                        contentDescription = if (isPasswordVisible) "Şifreyi Gizle" else "Şifreyi Göster"
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                }
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(all = 5.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.Start
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text(text = "Yeni Şifre") },
                            visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (isNewPasswordVisible) R.drawable.visibility else R.drawable.visibility_off
                                        ),
                                        contentDescription = if (isNewPasswordVisible) "Şifreyi Gizle" else "Şifreyi Göster"
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                }
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(all = 5.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.Start
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = newPasswordAgain,
                            onValueChange = { newPasswordAgain = it },
                            label = { Text(text = "Yeni Şifre Tekrar") },
                            visualTransformation = if (isNewPasswordAgainVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isNewPasswordAgainVisible = !isNewPasswordAgainVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (isNewPasswordAgainVisible) R.drawable.visibility else R.drawable.visibility_off
                                        ),
                                        contentDescription = if (isNewPasswordAgainVisible) "Şifreyi Gizle" else "Şifreyi Göster"
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                }
            }
        }
    )
}