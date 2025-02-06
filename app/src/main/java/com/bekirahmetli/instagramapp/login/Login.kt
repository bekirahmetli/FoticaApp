package com.bekirahmetli.instagramapp.login

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bekirahmetli.instagramapp.navigation.Screens
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Login(navController: NavHostController){
    var eposta by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()//snack barın daha performanslı çalışabilmesi için

    var mAuth: FirebaseAuth
    var mRef: DatabaseReference
    mAuth = FirebaseAuth.getInstance()
    mRef = FirebaseDatabase.getInstance().reference


    val currentUser = mAuth.currentUser
    val isLoading = remember { mutableStateOf(true) }

    // Kullanıcı durumunu kontrol etmek için
    LaunchedEffect(key1 = currentUser) {
        if (currentUser != null) {
            navController.navigate(route = Screens.HomeScreen.name) {
                popUpTo("Login") { inclusive = true }
            }
        }
        isLoading.value = false
    }


    val authStateListener = remember {
        FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                scope.launch {
                    snackbarHostState.showSnackbar("Kullanıcı oturum açtı: ${user.uid}")
                }
                navController.navigate(route = Screens.HomeScreen.name) {
                    popUpTo("Login") { inclusive = true }
                }
                fcmTokenSave()
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Kullanıcı oturum kapattı")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        mAuth.addAuthStateListener(authStateListener)
        onDispose {
            mAuth.removeAuthStateListener(authStateListener)
        }
    }

    if (isLoading.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            isLoading.value = true
        }
    } else {
        Scaffold (
            snackbarHost = {
                SnackbarHost (hostState = snackbarHostState){
                    Snackbar (//özelleştirmeleri burada yapıcaz
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
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.size(100.dp))
                    Text(
                        text = "Fotica",
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Cursive,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Spacer(modifier = Modifier.size(15.dp))
                    TextField(
                        value = eposta,
                        onValueChange = { newValue ->
                            eposta = newValue
                        },
                        label = { Text(text = "Eposta") },
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
                            .padding(13.dp),
                        visualTransformation = PasswordVisualTransformation(), // Şifreyi gizlemek için
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password) // Şifre klavyesi açılır
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    OutlinedButton(
                        onClick = {
                            var kullaniciBulundu = false
                            mAuth.signInWithEmailAndPassword(eposta, sifre)
                                .addOnCompleteListener(object :
                                    OnCompleteListener<AuthResult> {
                                    override fun onComplete(p0: Task<AuthResult>) {
                                        if (p0!!.isSuccessful) {
                                            scope.launch {//performanslı bir şekilde çalışabilmesi için
                                                snackbarHostState.showSnackbar("Oturum açıldı" + mAuth.currentUser!!.uid)
                                                kullaniciBulundu = true
                                                navController.navigate(route = Screens.HomeScreen.name)
                                            }
                                        } else {
                                            if (kullaniciBulundu == false){
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Kullanıcı bulunamadı!!")
                                                }
                                            }else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Eposta veya şifre hatalı!!")
                                                    kullaniciBulundu = true
                                                }
                                            }
                                        }
                                    }
                                })
                        },
                        enabled = eposta.length >= 6 && sifre.length >= 6,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Blue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(13.dp),
                        border = BorderStroke(1.dp, Color.Blue),
                        shape = RoundedCornerShape(30)
                    ) {
                        Text(text = "Giriş Yap")
                    }
                    Spacer(modifier = Modifier.size(300.dp))
                    Box( // Ayırıcı çizgi
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
                        modifier = Modifier.fillMaxWidth()//max genişlik
                    ) {
                        Text(
                            text = "Hesabın yok mu?",
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        TextButton(
                            onClick = {
                                navController.navigate("RegisterUp")
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(text = "Kaydol")
                        }
                    }

                }
            }
        )
    }
}

fun fcmTokenSave() {
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCMToken", "Token alma başarısız", task.exception)
                return@addOnCompleteListener
            }

            // Token alındı
            val token = task.result
            Log.e("FirebaseToken", "Token: $token")
            newTokenSavetoFirebase(token)
        }
}

fun newTokenSavetoFirebase(newToken: String) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        // Kullanıcıya ait UID ile token Firebase Realtime Database'e kaydedilir
        FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(currentUser.uid)
            .child("fcm_token")
            .setValue(newToken)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("FCMToken", "Token başarıyla kaydedildi")
                } else {
                    Log.e("FCMToken", "Token kaydedilirken hata oluştu", task.exception)
                }
            }
    }
}
