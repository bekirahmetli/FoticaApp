package com.bekirahmetli.instagramapp.model

class Message {
    var chat: String? = null
    var seen: Boolean? = null
    var time: Long? = null
    var type: String? = null
    var user_id:String? = null

    constructor()

    constructor(chat: String?, seen: Boolean?, time: Long?, type: String?, user_id: String?) {
        this.chat = chat
        this.seen = seen
        this.time = time
        this.type = type
        this.user_id = user_id
    }

    override fun toString(): String {
        return "Message(chat=$chat, seen=$seen, time=$time, type=$type, user_id=$user_id)"
    }

}