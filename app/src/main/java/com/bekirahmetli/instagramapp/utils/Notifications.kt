package com.bekirahmetli.instagramapp.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

object Notifications {
    private var mRef = FirebaseDatabase.getInstance().reference
    private var mAuth = FirebaseAuth.getInstance()
    private var mUser = mAuth.currentUser

    val follow_request = 1
    val delete_follow_requests = 2
    val started_following = 3
    val stopped_following = 4
    val post_liked = 5
    val post_likes_deleted = 6
    val delete_follow_request_to_current_user = 7
    val started_following_the_current_user = 8

    fun notificationSave(reportingUserID:String , notificationType:Int){
        when(notificationType){

            follow_request->{
                var newNotificationID = mRef.child("current_user_notifications")
                    .child(reportingUserID).push().key
                var newNotifications = HashMap<String,Any>()
                newNotifications.put("notification_type", follow_request)
                newNotifications.put("user_id", mUser!!.uid)
                newNotifications.put("time", ServerValue.TIMESTAMP)
                mRef.child("current_user_notifications").child(reportingUserID).child(newNotificationID!!)
                    .setValue(newNotifications)
            }

            delete_follow_requests->{
                mRef.child("current_user_notifications").child(reportingUserID)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(notification in snapshot.children){
                                var readNotificationKey = notification.key
                                if(notification.child("notification_type").getValue()
                                    .toString().toInt() == follow_request &&
                                    notification.child("user_id").getValue()!!.equals(mUser!!.uid)){
                                    mRef.child("current_user_notifications").child(reportingUserID)
                                        .child(readNotificationKey!!).removeValue()
                                    break
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }

            started_following->{
                var newNotificationID = mRef.child("current_user_notifications")
                    .child(reportingUserID).push().key
                var newNotifications = HashMap<String,Any>()
                newNotifications.put("notification_type", started_following)
                newNotifications.put("user_id", mUser!!.uid)
                newNotifications.put("time", ServerValue.TIMESTAMP)
                mRef.child("current_user_notifications").child(reportingUserID).child(newNotificationID!!)
                    .setValue(newNotifications)

            }

            stopped_following->{
                mRef.child("current_user_notifications").child(reportingUserID)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(notification in snapshot.children){
                                var readNotificationKey = notification.key
                                if(notification.child("notification_type").getValue()
                                        .toString().toInt() == started_following &&
                                    notification.child("user_id").getValue()!!.equals(mUser!!.uid)){
                                    mRef.child("current_user_notifications").child(reportingUserID)
                                        .child(readNotificationKey!!).removeValue()
                                    break
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })

            }

            delete_follow_request_to_current_user->{
                mRef.child("current_user_notifications").child(mUser!!.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(notification in snapshot.children){
                                var readNotificationKey = notification.key
                                if(notification.child("notification_type").getValue()
                                        .toString().toInt() == follow_request &&
                                    notification.child("user_id").getValue()!!.equals(reportingUserID)){
                                    mRef.child("current_user_notifications").child(mUser!!.uid)
                                        .child(readNotificationKey!!).removeValue()
                                    break
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }

            started_following_the_current_user->{
                var newNotificationID = mRef.child("current_user_notifications")
                    .child(mUser!!.uid).push().key
                var newNotifications = HashMap<String,Any>()
                newNotifications.put("notification_type", started_following)
                newNotifications.put("user_id", reportingUserID)
                newNotifications.put("time", ServerValue.TIMESTAMP)
                mRef.child("current_user_notifications").child(mUser!!.uid).child(newNotificationID!!)
                    .setValue(newNotifications)

            }
        }
    }

    fun notificationSave(reportingUserID:String , notificationType:Int,postID:String){
        when(notificationType){
            post_liked->{
                var newNotificationID = mRef.child("current_user_notifications")
                    .child(reportingUserID).push().key
                var newNotifications = HashMap<String,Any>()
                newNotifications.put("notification_type", post_liked)
                newNotifications.put("user_id", mUser!!.uid)
                newNotifications.put("post_id",postID)
                newNotifications.put("time", ServerValue.TIMESTAMP)
                mRef.child("current_user_notifications").child(reportingUserID).child(newNotificationID!!)
                    .setValue(newNotifications)

            }

            post_likes_deleted->{
                mRef.child("current_user_notifications").child(reportingUserID)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(notification in snapshot.children){
                                var readNotificationKey = notification.key
                                if(notification.child("notification_type").getValue()
                                        .toString().toInt() == post_liked &&
                                    notification.child("user_id").getValue()!!.equals(mUser!!.uid)){
                                    mRef.child("current_user_notifications").child(reportingUserID)
                                        .child(readNotificationKey!!).removeValue()
                                    break
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }
        }
    }
}