package com.refillfinder.app.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val defaultBlock: Int = 1,
    val defaultFloor: Int = 1,
    val createdAt: Timestamp = Timestamp.now()
)
