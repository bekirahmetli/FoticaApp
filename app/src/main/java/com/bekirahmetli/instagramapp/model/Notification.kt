package com.bekirahmetli.instagramapp.model

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

class Notification {
    var notification_type: Int? = null
    var post_id : String? = null
    var time: Long? = null
    var user_id:String? = null
    var userName: String? = null
    var userPhotoURL: String? = null

    constructor()

    constructor(notification_type: Int?, post_id: String?, time: Long?, user_id: String?) {
        this.notification_type = notification_type
        this.post_id = post_id
        this.time = time
        this.user_id = user_id
    }

    constructor(notification_type: Int?, time: Long?, user_id: String?) {
        this.notification_type = notification_type
        this.time = time
        this.user_id = user_id
    }

    // Kullanıcı bilgilerini Firebase'den alacak fonksiyon
    suspend fun fetchUserInfo(database: DatabaseReference): Pair<String?, String?> {
        return if (user_id != null) {
            val userRef = database.child("users").child(user_id!!)
            try {
                val snapshot = userRef.get().await()
                val userName = snapshot.child("user_name").getValue(String::class.java)
                val userPhotoURL = snapshot.child("user_detail").child("profile_picture").getValue(String::class.java)
                Pair(userName, userPhotoURL)
            } catch (e: Exception) {
                null to null
            }
        } else {
            null to null
        }
    }

    override fun toString(): String {
        return "Notification(notification_type=$notification_type, time=$time, user_id=$user_id)"
    }


}