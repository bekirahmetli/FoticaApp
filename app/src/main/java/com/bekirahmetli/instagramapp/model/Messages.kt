package com.bekirahmetli.instagramapp.model

class Messages {

    var last_message: String? = null
    var seen : Boolean? = null
    var time: Long? = null
    var user_id:String? = null

    constructor()
    constructor(last_message: String?, seen: Boolean?, time: Long?, user_id: String?) {
        this.last_message = last_message
        this.seen = seen
        this.time = time
        this.user_id = user_id
    }

    override fun toString(): String {
        return "Messages(last_message=$last_message, seen=$seen, time=$time, user_id=$user_id)"
    }


}