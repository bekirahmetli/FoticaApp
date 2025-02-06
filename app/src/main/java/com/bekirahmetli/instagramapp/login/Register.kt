package com.bekirahmetli.instagramapp.login

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bekirahmetli.instagramapp.R
import com.bekirahmetli.instagramapp.model.Users
import com.bekirahmetli.instagramapp.model.UsersDetails
import com.bekirahmetli.instagramapp.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterUp(navController: NavHostController,users: Users) {
    var textfEmail by remember { mutableStateOf("") }
    var adsoyad by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }
    var kadi by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()//snack barın daha performanslı çalışabilmesi için
    var progressDurum = remember { mutableStateOf(false) }
    // MutableState'ler, Composable'ın state'inin güncellenmesini sağlar.

    var mAuth: FirebaseAuth
    var mRef: DatabaseReference
    mAuth = FirebaseAuth.getInstance()
    mRef = FirebaseDatabase.getInstance().reference

    var userID:String? = mAuth.currentUser?.uid

    Scaffold (
        snackbarHost = {
            SnackbarHost (hostState = snackbarHostState){
                Snackbar (
                    snackbarData = it,
                    containerColor = Color.White,//arka plan rengi
                    contentColor = Color.Blue,//yazı rengi
                    actionColor = Color.Red//tekrar dene yazı rengi
                )
            }
        },
        content = {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.account_circle),
                    contentDescription = null,
                    Modifier.size(200.dp)
                )
                Row {
                    TextButton(
                        onClick = {},
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White)
                    ) {
                        Text(text = "Kullanıcı Bilgileri")
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))

                EmailTextField(textfEmail) { textfEmail = it }

                TextField(
                    value = adsoyad,
                    onValueChange = { newValue ->
                        adsoyad = newValue
                    },
                    label = { Text(text = "Adın ve Soyadın") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                )
                Spacer(modifier = Modifier.size(5.dp))
                TextField(
                    value = sifre,
                    onValueChange = { newValue ->
                        sifre = newValue
                    },
                    label = { Text(text = "Şifre") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                )
                Spacer(modifier = Modifier.size(5.dp))
                TextField(
                    value = kadi,
                    onValueChange = { newValue ->
                        kadi = newValue
                    },
                    label = { Text(text = "Kullanıcı adı") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                )
                OutlinedButton(
                    onClick = {
                        progressDurum.value = true

                        mRef.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var emailVarMi = false
                                var kadiVarMi = false

                                // Tüm kullanıcıları kontrol et
                                for (userSnapshot in snapshot.children) {
                                    val user = userSnapshot.getValue(Users::class.java)

                                    if (user?.email == textfEmail) {
                                        emailVarMi = true
                                    }

                                    if (user?.user_name == kadi) {
                                        kadiVarMi = true
                                    }
                                }

                                if (emailVarMi) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Bu email zaten kullanılıyor!")
                                        progressDurum.value = false
                                    }
                                } else if (kadiVarMi) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Bu kullanıcı adı zaten kullanılıyor!")
                                        progressDurum.value = false
                                    }
                                } else {
                                    // Eğer email ve kullanıcı adı kullanılmıyorsa kayıt işlemine devam et
                                    mAuth.createUserWithEmailAndPassword(textfEmail, sifre)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val currentUser = mAuth.currentUser
                                                val kullaniciDetaylari = UsersDetails("0", "0", "0", "", "", "")
                                                val kullaniciBilgileri = Users(
                                                    email = textfEmail,
                                                    password = sifre,
                                                    user_name = kadi,
                                                    adi_soyadi = adsoyad,
                                                    user_id = userID,
                                                    kullaniciDetaylari
                                                )

                                                mRef.child("users").child(currentUser?.uid ?: "")
                                                    .setValue(kullaniciBilgileri)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            // hidden_profile değerini false olarak ekle
                                                            mRef.child("users").child(currentUser?.uid ?: "").child("hidden_profile")
                                                                .setValue(false)
                                                                .addOnCompleteListener { hiddenTask ->
                                                                    if (hiddenTask.isSuccessful) {
                                                                        scope.launch {
                                                                            snackbarHostState.showSnackbar("Kayıt başarılı!")
                                                                            progressDurum.value = false
                                                                            navController.navigate(route = Screens.HomeScreen.name)
                                                                        }
                                                                    } else {
                                                                        scope.launch {
                                                                            snackbarHostState.showSnackbar("hidden_profile eklenemedi!")
                                                                        }
                                                                    }
                                                                }
                                                        } else {
                                                            mAuth.currentUser!!.delete()
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Kayıt başarısız!")
                                                                progressDurum.value = false
                                                            }
                                                        }
                                                    }
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Kayıt sırasında hata oluştu!")
                                                    progressDurum.value = false
                                                }
                                            }
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Veritabanı hatası: ${error.message}")
                                    progressDurum.value = false
                                }
                            }
                        })
                    },
                    enabled = textfEmail.contains("@") && kadi.length >= 6 && adsoyad.length >= 6 && sifre.length >= 6,
                    colors = ButtonDefaults.buttonColors(//olması veya eposta içinde @ olması
                        containerColor = Color.White,
                        contentColor = Color.Blue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp),
                    border = BorderStroke(1.dp, Color.Blue),
                    shape = RoundedCornerShape(30)
                ) {
                    Text(text = "Kayıt Ol")
                }
                Spacer(modifier = Modifier.size(5.dp))
                if (progressDurum.value) {
                    CircularProgressIndicator(color = Color.Blue)
                }
                Spacer(modifier = Modifier.size(60.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.Gray)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hesabın zaten var mı?",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    TextButton(
                        onClick = {
                            navController.navigate("Login")
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(text = "Giriş Yap")
                    }
                }

            }
        }
    )
}

@Composable
fun EmailTextField(textfEmail: String, onTextChange: (String) -> Unit) {
    TextField(
        value = textfEmail,
        onValueChange = { newEmail ->
            val isValid = isValidEmail(newEmail)
            onTextChange(newEmail)
            if (!isValid) {
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Email
        ),
        keyboardActions = KeyboardActions(
            onDone = {
            }
        ),
        label = { Text(text = "E-posta") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(13.dp)
    )
}

fun isValidEmail(textfEmail: String): Boolean {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$"
    return textfEmail.matches(emailRegex.toRegex())
}