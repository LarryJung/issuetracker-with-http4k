package com.larry.issuetracker.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING as m

data class Email @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Password @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Token @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Username @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Bio @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Image @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class User(
    val id: Int = 0,
    val email: Email,
    val password: Password? = null,
    var token: Token,
    val username: Username,
    val bio: Bio? = null,
    val image: Image? = null
)