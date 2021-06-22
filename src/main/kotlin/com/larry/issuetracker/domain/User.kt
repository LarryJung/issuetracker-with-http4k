package com.larry.issuetracker.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.larry.issuetracker.db.CommonRepository
import com.larry.issuetracker.db.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

data class User(
    val email: Email?,
    val username: Username,
    val image: Image? = null,
    val id: Int = 0
)

data class Email @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Username @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Image @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

// query
fun CommonRepository.findAllUser(): List<User> = Users.selectAll().map { it.toUser() }
fun CommonRepository.getUser(username: Username): User? =
    Users.select { Users.username eq username.value }.firstOrNull()?.toUser()

fun CommonRepository.insertUser(user: User): User =
    Users.insertAndGetId {
        it[username] = user.username.value
        it[email] = user.email?.value
        it[image] = user.image?.value
    }.let { user.copy(id = it.value) }

private fun ResultRow.toUser() = User(
    id = this[Users.id].value,
    email = this[Users.email]?.let(::Email),
    username = Username(this[Users.username]),
    image = this[Users.image]?.let(::Image)
)