package com.bekirahmetli.instagramapp.model


class SearchUser {
    var user_name: String? = null
    var adi_soyadi: String? = null
    var user_id: String? = null
    var user_detail: UsersDetails? = null // user_detail objesi buraya bağlanıyor

    constructor()

    constructor(
        user_name: String?,
        adi_soyadi: String?,
        user_id: String?,
        user_detail: UsersDetails?
    ) {
        this.user_name = user_name
        this.adi_soyadi = adi_soyadi
        this.user_id = user_id
        this.user_detail = user_detail
    }

    // Profil fotoğrafı URL'sini kolayca almak için bir yardımcı fonksiyon
    fun getProfilePhotoURL(): String? {
        return user_detail?.profile_picture
    }
}