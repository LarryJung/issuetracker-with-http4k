package com.larry.issuetracker.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING as m


data class Token @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

