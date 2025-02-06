package com.bekirahmetli.instagramapp.Services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        var notificationTitle  = message.notification!!.title
        var notificationBody = message.notification!!.body
        var notificationData = message.data
        Log.e("Message","Title $notificationTitle Body: $notificationBody data:$notificationData")
    }

    override fun onNewToken(token: String) {
        var newToken = token
        newTokenSaveToFirebase(newToken)
    }

    private fun newTokenSaveToFirebase(newToken: String) {
        if(FirebaseAuth.getInstance().currentUser != null){
            FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("fcm_token").setValue(newToken)
        }
    }
}