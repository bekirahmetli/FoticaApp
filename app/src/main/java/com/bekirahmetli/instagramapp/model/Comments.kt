package com.bekirahmetli.instagramapp.model

class Comments {
    var user_id:String?=null
    var yorum:String?=null
    var yorum_begeni:String?=null
    var yorum_tarih:String?=null

    constructor()

    constructor(user_id: String?, yorum: String?, yorum_begeni: String?, yorum_tarih: String?) {
        this.user_id = user_id
        this.yorum = yorum
        this.yorum_begeni = yorum_begeni
        this.yorum_tarih = yorum_tarih
    }

    override fun toString(): String {
        return "Comments(user_id=$user_id, yorum=$yorum, yorum_begeni=$yorum_begeni, yorum_tarih=$yorum_tarih)"
    }


}