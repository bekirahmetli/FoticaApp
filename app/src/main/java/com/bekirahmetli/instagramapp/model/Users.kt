package com.bekirahmetli.instagramapp.model

class Users {
    var email: String? = null
    var password: String? = null
    var user_name: String? = null
    var adi_soyadi: String? = null
    var user_id : String? = null
    var user_detail : UsersDetails? = null

    constructor()

    constructor(email: String?, password: String?, user_name: String?, adi_soyadi: String?, user_id: String?, user_detail: UsersDetails?) {
        this.email = email
        this.password = password
        this.user_name = user_name
        this.adi_soyadi = adi_soyadi
        this.user_id = user_id
        this.user_detail = user_detail
    }
}