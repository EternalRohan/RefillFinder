package com.refillfinder.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.refillfinder.app.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val name: String,
    val email: String,
    val password: String = "",
    val defaultBlock: Int = 1,
    val defaultFloor: Int = 1,
    val photoUrl: String = ""
) {
    fun toDomain() = User(
        uid = uid,
        name = name,
        email = email,
        defaultBlock = defaultBlock,
        defaultFloor = defaultFloor,
        photoUrl = photoUrl
    )
}

fun User.toEntity(password: String = "") = UserEntity(
    uid = uid,
    name = name,
    email = email,
    password = password,
    defaultBlock = defaultBlock,
    defaultFloor = defaultFloor,
    photoUrl = photoUrl
)
