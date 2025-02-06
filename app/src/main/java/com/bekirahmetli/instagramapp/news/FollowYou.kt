package com.bekirahmetli.instagramapp.news

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.bekirahmetli.instagramapp.model.Notification
import com.bekirahmetli.instagramapp.profile.ProfileImage
import com.bekirahmetli.instagramapp.utils.Notifications
import com.bekirahmetli.instagramapp.utils.PostTime
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.runBlocking

@Composable
fun FollowYou() {
    val muser = FirebaseAuth.getInstance().currentUser
    val currentUserId = muser?.uid

    if (currentUserId == null) {
        return
    }


    val notifications = remember { mutableStateListOf<Notification>() }

    LaunchedEffect(currentUserId) {
        val database = Firebase.database.reference
        val notificationsRef = database.child("current_user_notifications").child(currentUserId)

        try {
            notificationsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fetchedNotifications = snapshot.children.mapNotNull {
                        try {
                            val notification = it.getValue(Notification::class.java)
                            // Kullanıcı bilgilerini al
                            notification?.let { notif ->
                                val (userName, userPhotoURL) = runBlocking {
                                    notif.fetchUserInfo(database)
                                }
                                notif.userName = userName
                                notif.userPhotoURL = userPhotoURL
                            }
                            notification
                        } catch (e: Exception) {
                            Log.e("FollowYou", "Notification parse hatası: ${e.message}")
                            null
                        }
                    }
                    notifications.clear()
                    notifications.addAll(fetchedNotifications)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        } catch (e: Exception) {
            Log.e("FollowYou", "Veri çekme sırasında hata oluştu: ${e.message}")
        }
    }

    val sortedNotifications = notifications.sortedByDescending { it.time }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(sortedNotifications) { notification ->
            notification?.let {
                when (it.notification_type) {
                    3 -> {
                        FollowNotification(it)
                    }
                    5 -> {
                        LikeNotification(it)
                    }
                    1 -> {
                        ApprovalNotification(it)
                    }
                    else -> {
                        Log.e("FollowYou", "Bilinmeyen notification_type: ${it.notification_type}")
                    }
                }
            }
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun ApprovalNotification(notification: Notification) {
    var mRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    var mAuth:FirebaseAuth = FirebaseAuth.getInstance()
    var mUser: FirebaseUser = mAuth.currentUser!!

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ProfileImage(
                painter = rememberImagePainter(data = notification.userPhotoURL),
                imageSize = 36,
                onClick = {}
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text =  "${notification.userName ?: "Bilinmeyen Kullanıcı"} kullanıcısı sizi takip etmek istiyor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                Button(onClick = {
                    mRef.child("follow_requests").child(mUser.uid).child(notification.user_id!!)
                        .removeValue()
                    Notifications.notificationSave(
                        notification.user_id!!,
                        Notifications.delete_follow_request_to_current_user
                    )

                    mRef.child("following").child(notification.user_id!!).child(mUser.uid)
                        .setValue(mUser.uid)
                    mRef.child("follower").child(mUser.uid).child(notification.user_id!!)
                        .setValue(notification.user_id!!)

                    // Takipçi ve takip edilen verilerini güncelle
                    val currentUserRef = mRef.child("users").child(mUser.uid).child("user_detail")
                    val otherUserRef =
                        mRef.child("users").child(notification.user_id!!).child("user_detail")

                    // Takip edilenin "follower" değerini artır
                    otherUserRef.child("followed").get().addOnSuccessListener { snapshot ->
                        val currentFollowerCount =
                            snapshot.getValue(String::class.java)?.toIntOrNull() ?: 0
                        otherUserRef.child("followed")
                            .setValue((currentFollowerCount + 1).toString())
                    }

                    // Takip edenin değerini artır
                    currentUserRef.child("follower").get().addOnSuccessListener { snapshot ->
                        val currentFollowedCount =
                            snapshot.getValue(String::class.java)?.toIntOrNull() ?: 0
                        currentUserRef.child("follower")
                            .setValue((currentFollowedCount + 1).toString())
                    }


                    Notifications.notificationSave(
                        notification.user_id!!,
                        Notifications.started_following_the_current_user
                    )

                },
                    modifier = Modifier.padding(end = 4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                    Text(
                        text = "Onayla"
                    )
                }
                Button(onClick = {
                    mRef.child("follow_requests").child(mUser.uid).child(notification.user_id!!)
                        .removeValue()
                    Notifications.notificationSave(
                        notification.user_id!!,
                        Notifications.delete_follow_request_to_current_user
                    )
                },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                    Text(
                        text = "Sil"
                    )
                }
            }
        }
}

@Composable
fun LikeNotification(notification: Notification) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImage(
                painter = rememberImagePainter(data = notification.userPhotoURL),
                imageSize = 36,
                onClick = {}
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${notification.userName} fotoğrafını beğendi",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
                notification.time?.let { timeStamp ->
                    val timeAgo = PostTime().getTimeAgo(timeStamp)
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

@Composable
fun FollowNotification(notification: Notification) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImage(
                painter = rememberImagePainter(data = notification.userPhotoURL),
                imageSize = 36,
                onClick = {}
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${notification.userName} sizi takip etmeye başladı",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
                notification.time?.let { timeStamp ->
                    val timeAgo = PostTime().getTimeAgo(timeStamp)
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
