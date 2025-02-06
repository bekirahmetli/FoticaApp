package com.bekirahmetli.instagramapp.model

class UserPosts {
    var userID: String?=null
    var userName: String?=null
    var userPhotoURL: String?=null
    var postID: String?=null
    var postAciklama: String?=null
    var postURL: String?=null
    var postYuklenmeTarihi : Long? = null

    constructor()

    constructor(
        userID: String?,
        userName: String?,
        userPhotoURL: String?,
        postID: String?,
        postAciklama: String?,
        postURL: String?,
        postYuklenmeTarihi: Long?
    ) {
        this.userID = userID
        this.userName = userName
        this.userPhotoURL = userPhotoURL
        this.postID = postID
        this.postAciklama = postAciklama
        this.postURL = postURL
        this.postYuklenmeTarihi = postYuklenmeTarihi
    }
}