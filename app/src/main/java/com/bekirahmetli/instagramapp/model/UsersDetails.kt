package com.bekirahmetli.instagramapp.model

class UsersDetails {
    var follower : String? = null
    var followed : String? = null
    var post : String? = null
    var biography : String? = null
    var web_site : String? = null
    var profile_picture : String? = null

    constructor()

    constructor(follower: String?, followed: String?, post: String?, biography: String?, web_site: String?,profile_picture : String?) {
        this.follower = follower
        this.followed = followed
        this.post = post
        this.biography = biography
        this.web_site = web_site
        this.profile_picture = profile_picture
    }
}